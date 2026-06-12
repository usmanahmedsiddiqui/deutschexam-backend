# Local Testing Guide

## Prerequisites

- Docker + Docker Compose installed
- `curl` and `jq` available in your terminal
- (Optional) A valid Google ID token for full auth testing

---

## 1. Start the stack

```bash
# First time only — copy the example env file
cp .env.example .env

# Build the image and start all services
docker compose up --build
```

Wait until the backend logs show:
```
Application started in X.XXX seconds.
```

Flyway runs migrations and seeds all data automatically on startup.

To watch logs in a separate terminal:
```bash
docker compose logs -f backend
```

**Services running:**
| Service | URL |
|---------|-----|
| Backend API | http://localhost:8080 |
| Adminer (DB browser) | http://localhost:8081 |
| PostgreSQL | localhost:5432 |

---

## 2. Health check

```bash
curl -s http://localhost:8080/health | jq
```

Expected:
```json
{"status":"ok"}
```

If you see `503 SERVICE_UNAVAILABLE` the DB connection failed — check `docker compose logs postgres`.

---

## 3. Reference data (no auth required)

```bash
curl -s http://localhost:8080/levels | jq
curl -s http://localhost:8080/providers | jq
curl -s http://localhost:8080/products | jq
curl -s http://localhost:8080/exams | jq
curl -s http://localhost:8080/exam-details | jq
```

Expected: 3 levels, 2 providers (telc / Goethe), 3 products (A1/A2/B1).

---

## 4. Auth endpoints

### 4a. Google sign-in — validation
```bash
# 422 VALIDATION_ERROR — blank idToken
curl -s -X POST http://localhost:8080/auth/google \
  -H "Content-Type: application/json" \
  -d '{"idToken":""}' | jq
```

### 4b. Google sign-in — invalid token
```bash
# 401 GOOGLE_TOKEN_INVALID — non-blank but fake token
curl -s -X POST http://localhost:8080/auth/google \
  -H "Content-Type: application/json" \
  -d '{"idToken":"not-a-real-google-token"}' | jq
```

### 4c. Refresh — validation
```bash
# 422 VALIDATION_ERROR — blank refresh token
curl -s -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refresh_token":""}' | jq
```

### 4d. Full auth flow (requires a real Google ID token)

Get a real `idToken` from your Android app or from the [Google OAuth Playground](https://developers.google.com/oauthplayground).

```bash
# Sign in → get JWT + refresh token
RESPONSE=$(curl -s -X POST http://localhost:8080/auth/google \
  -H "Content-Type: application/json" \
  -d '{"idToken":"<your-real-google-id-token>"}')
echo $RESPONSE | jq

# Save the tokens
JWT=$(echo $RESPONSE | jq -r '.token')
REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.refresh_token')

# Refresh → get a new JWT
curl -s -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refresh_token\":\"$REFRESH_TOKEN\"}" | jq
```

---

## 5. JWT error codes

```bash
# 401 TOKEN_MISSING — no Authorization header
curl -s http://localhost:8080/exams/some-id | jq

# 401 TOKEN_INVALID — garbage token
curl -s http://localhost:8080/exams/some-id \
  -H "Authorization: Bearer not-a-jwt" | jq
```

To test `TOKEN_EXPIRED`, generate an expired JWT at [jwt.io](https://jwt.io):
- Algorithm: HS256
- Secret: `dev-secret-change-in-production-min-32-chars`
- Payload: `{"sub":"user-1","email":"test@test.com","iss":"deutschexam-backend","exp":1000000000}`

```bash
curl -s http://localhost:8080/exams/some-id \
  -H "Authorization: Bearer <expired-jwt>" | jq
```

---

## 6. Exam access control (SEC-1 fix)

Replace `<exam-id>` with an actual exam ID from `GET /exams`.

```bash
# 404 EXAM_NOT_FOUND — non-existent exam
curl -s http://localhost:8080/exams/does-not-exist | jq

# Free exam — accessible without auth (check isFree field in GET /exams)
curl -s http://localhost:8080/exams/<free-exam-id> | jq

# Paid exam — no token → 401 TOKEN_MISSING
curl -s http://localhost:8080/exams/<paid-exam-id> | jq

# Paid exam — valid token but no purchase → 403 EXAM_NOT_OWNED
curl -s http://localhost:8080/exams/<paid-exam-id> \
  -H "Authorization: Bearer $JWT" | jq
```

---

## 7. Bug report

```bash
# 201 Created
curl -s -X POST http://localhost:8080/bugs \
  -H "Content-Type: application/json" \
  -d '{"title":"App crashes on login","description":"Tapping the login button closes the app."}' | jq

# 422 — blank title
curl -s -X POST http://localhost:8080/bugs \
  -H "Content-Type: application/json" \
  -d '{"title":"   ","description":"Some description"}' | jq

# 422 — blank description
curl -s -X POST http://localhost:8080/bugs \
  -H "Content-Type: application/json" \
  -d '{"title":"Some title","description":""}' | jq

# 422 — title too long (256 chars)
curl -s -X POST http://localhost:8080/bugs \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"$(python3 -c "print('a'*256")\",\"description\":\"desc\"}" | jq
```

---

## 8. Rate limiting

Hit `/bugs` 6 times quickly — the 6th request should return `429 RATE_LIMITED` with a `Retry-After: 60` header.

```bash
for i in {1..6}; do
  echo -n "Request $i: "
  curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/bugs \
    -H "Content-Type: application/json" \
    -d '{"title":"test","description":"test"}'
  echo
done
```

---

## 9. Database — Adminer

Open **http://localhost:8081** in your browser.

| Field | Value |
|-------|-------|
| System | PostgreSQL |
| Server | postgres |
| Username | deutschexam |
| Password | deutschexam |
| Database | deutschexam |

Check that all tables exist with seed data: `levels`, `providers`, `products`, `exam_details`, `exams`.

---

## 10. Stop the stack

```bash
docker compose down
```

To also wipe the database volume (full reset):
```bash
docker compose down -v
```

---

## Checklist

- [ ] `GET /health` → 200 `{"status":"ok"}`
- [ ] `GET /levels`, `/providers`, `/products` return seed data
- [ ] `POST /bugs` blank title → 422 `VALIDATION_ERROR`
- [ ] `POST /bugs` valid body → 201 with id
- [ ] `POST /auth/google` blank token → 422 `VALIDATION_ERROR`
- [ ] `POST /auth/google` fake token → 401 `GOOGLE_TOKEN_INVALID`
- [ ] `GET /exams/{paid-id}` no auth → 401 `TOKEN_MISSING`
- [ ] `GET /exams/{paid-id}` valid JWT, not purchased → 403 `EXAM_NOT_OWNED`
- [ ] `GET /exams/does-not-exist` → 404 `EXAM_NOT_FOUND`
- [ ] 6th `POST /bugs` in under a minute → 429 `RATE_LIMITED`
- [ ] All error responses include `"request_id"` field
