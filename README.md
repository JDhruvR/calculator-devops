# Calculator REST API

A production-style Calculator REST API built with **Spring Boot**, wrapped in a complete **CI/CD pipeline** using Jenkins, Docker, and Docker Compose.

> SPE Mini-Project | M.Tech (CSE) | Term I, 2026-27

---

## Architecture

```
Developer → GitHub → Jenkins Pipeline → Docker Hub
                          ↓
                   Docker Compose
                    ┌─────────────┐
              :80   │    Nginx    │  (reverse proxy)
                    │   Proxy     │
                    └──────┬──────┘
                           │ proxy_pass
                    ┌──────▼──────┐
                    │  Calculator │  :8080 (Spring Boot)
                    │     API     │
                    └─────────────┘
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |
| Docker | 24+ |
| Docker Compose | v2 |

---

## Local Development

### 1. Build

```bash
mvn clean package -DskipTests
```

### 2. Run Tests

```bash
mvn test
```

JUnit XML reports are written to `target/surefire-reports/`.

### 3. Run Locally (without Docker)

```bash
java -jar target/calculator-0.0.1-SNAPSHOT.jar
```

API is available at `http://localhost:8080`.

---

## Docker

### Build Image

```bash
docker build -t calculator-api:local .
```

### Run Container

```bash
docker run -d -p 8080:8080 --name calculator calculator-api:local
```

---

## Docker Compose (Full Stack)

```bash
docker compose up -d
```

Starts:
- **calculator-app** — Spring Boot API on internal port 8080
- **calculator-proxy** — Nginx reverse proxy on `localhost:80`

Tear down:

```bash
docker compose down
```

---

## API Reference

All endpoints accept query parameters `a` and `b` (numeric). Responses are JSON.

### Add

```bash
curl "http://localhost/api/v1/add?a=2&b=3"
# {"operation":"add","a":2.0,"b":3.0,"result":5.0}
```

### Subtract

```bash
curl "http://localhost/api/v1/subtract?a=10&b=3"
# {"operation":"subtract","a":10.0,"b":3.0,"result":7.0}
```

### Multiply

```bash
curl "http://localhost/api/v1/multiply?a=4&b=5"
# {"operation":"multiply","a":4.0,"b":5.0,"result":20.0}
```

### Divide

```bash
curl "http://localhost/api/v1/divide?a=10&b=4"
# {"operation":"divide","a":10.0,"b":4.0,"result":2.5}
```

### Divide by Zero → HTTP 400

```bash
curl "http://localhost/api/v1/divide?a=5&b=0"
# {"status":400,"error":"Bad Request","message":"Division by zero is not allowed"}
```

### Health Check

```bash
curl "http://localhost/actuator/health"
# {"status":"UP",...}
```

---

## CI/CD Pipeline (Jenkins)

The `Jenkinsfile` defines a **declarative pipeline** with 6 stages:

| Stage | What it does |
|-------|-------------|
| Checkout | Pulls latest code from GitHub |
| Build | `mvn clean package -DskipTests` |
| Test | `mvn test` + publishes JUnit reports |
| Docker Build | Builds image tagged with build number |
| Push | Pushes to Docker Hub via stored credentials |
| Deploy | `docker compose up -d` + health check |

The pipeline triggers automatically on every GitHub push via webhook.

---

## Project Structure

```
calculator-devops/
├── src/
│   ├── main/java/com/spe/calculator/
│   │   ├── CalculatorApplication.java
│   │   ├── controller/CalculatorController.java
│   │   ├── service/CalculatorService.java
│   │   ├── dto/CalculatorResponse.java
│   │   ├── dto/ErrorResponse.java
│   │   └── exception/GlobalExceptionHandler.java
│   └── test/java/com/spe/calculator/
│       ├── service/CalculatorServiceTest.java
│       └── controller/CalculatorControllerTest.java
├── nginx/nginx.conf
├── pom.xml
├── Dockerfile
├── .dockerignore
├── docker-compose.yml
├── Jenkinsfile
├── .gitignore
└── README.md
```


## Live Deployment Status

Pipeline fully automated — any `git push` to `main` triggers Jenkins to build, test, containerize, push to Docker Hub, and deploy automatically. No manual steps required.
