# CSE 816: Software Production Engineering — Mini-Project Report
## End-to-End CI/CD Pipeline for a Calculator REST API

**Student Name:** Dhruv  
**Course:** M.Tech (CSE) — Term I, 2026-27  
**Faculty:** Prof. B. Thangaraju  
**GitHub Repository:** [https://github.com/JDhruvR/calculator-devops](https://github.com/JDhruvR/calculator-devops)  
**Docker Hub Image:** [https://hub.docker.com/r/jdhruvr/calculator-api](https://hub.docker.com/r/jdhruvr/calculator-api)  

---

## 1. Executive Summary & Objective

The objective of this project is to build an automated, enterprise-grade Continuous Integration and Continuous Delivery (CI/CD) DevOps pipeline for a Java Spring Boot Calculator REST API. Version control is managed through GitHub, automated testing and build orchestration are implemented using a declarative Jenkins pipeline, containerization is performed using multi-stage Docker builds, and deployment orchestration is managed via Docker Compose with an Nginx reverse proxy.

---

## 2. Architecture & Workflow

The architecture follows a trigger-driven automated delivery cycle:

```
[ Developer ]
      │ (git push)
      ▼
 [ GitHub ] ──────(Webhook / SCM Polling)──────┐
                                               ▼
                                      [ Jenkins Pipeline ]
                                       ├── 1. Checkout (SCM)
                                       ├── 2. Build (mvn clean package)
                                       ├── 3. Test (JUnit 5 + MockMvc)
                                       ├── 4. Docker Build (Multi-stage)
                                       ├── 5. Push to Docker Hub
                                       └── 6. Deploy & Health Probe
                                               │
                                               ▼
                                   [ Docker Compose Stack ]
                                   ┌──────────────────────┐
                                   │  Nginx Reverse Proxy │ :80
                                   └──────────┬───────────┘
                                              │ (proxy_pass)
                                   ┌──────────▼───────────┐
                                   │  Calculator REST API │ :8080
                                   │  (Spring Boot / Act) │
                                   └──────────────────────┘
```

---

## 3. Application Specification & Testing

### 3.1 REST Endpoints & Behavior
- `GET /api/v1/add?a=&b=` — Returns JSON `{ "operation": "add", "a": ..., "b": ..., "result": ... }`
- `GET /api/v1/subtract?a=&b=` — Returns JSON with difference
- `GET /api/v1/multiply?a=&b=` — Returns JSON with product
- `GET /api/v1/divide?a=&b=` — Returns JSON with quotient. When `b = 0`, returns `HTTP 400 Bad Request` with structured error JSON:
  ```json
  {
    "status": 400,
    "error": "Bad Request",
    "message": "Division by zero is not allowed"
  }
  ```
- `GET /actuator/health` — Spring Boot Actuator probe exposing component and disk health (`UP`).

### 3.2 Testing Strategy
1. **Unit Tests (JUnit 5):** `CalculatorServiceTest` tests all arithmetic branches, edge cases, negative numbers, and explicitly verifies `assertThrows(ArithmeticException.class)` on division by zero.
2. **Integration Tests (Spring MockMvc):** `CalculatorControllerTest` validates real HTTP request handling, parameter deserialization, query string validation (400 on missing/malformed parameter), and Spring Boot Actuator health reporting.
3. **Report Generation:** Maven Surefire produces standard JUnit XML reports at `target/surefire-reports/*.xml`, consumed and visualized natively by Jenkins.

---

## 4. Pipeline Stages (Jenkinsfile Deep Dive)

The declarative pipeline executes six strictly ordered stages:

| Stage | Action & Purpose |
|---|---|
| **1. Checkout** | Clones the repository dynamically via `checkout scm` upon trigger, capturing the commit SHA and branch metadata. |
| **2. Build** | Executes `mvn clean package -DskipTests -B` to compile the bytecode, package dependencies, and verify project build validity without running tests prematurely. |
| **3. Test** | Runs `mvn test -B`. Automatically records test results with `junit 'target/surefire-reports/*.xml'`. If any unit or integration test fails, the pipeline aborts immediately. |
| **4. Docker Build** | Builds two images: the Spring Boot application (`jdhruvr/calculator-api:${BUILD_NUMBER}`) and the reverse proxy (`jdhruvr/calculator-proxy:${BUILD_NUMBER}`). Both are also tagged as `:latest`. |
| **5. Push to Docker Hub** | Authenticates securely with Docker Hub using Jenkins Managed Credentials (`withCredentials`) without exposing secrets, pushing both versioned and latest image tags. |
| **6. Deploy** | Deploys the stack using `docker compose up -d --remove-orphans`. Executes an automated retry loop against `/actuator/health` to confirm stack health before concluding successfully. |

---

## 5. Containerization & Orchestration Best Practices

### 5.1 Multi-Stage Dockerfile
- **Stage 1 (Builder):** Uses `maven:3.9.6-eclipse-temurin-21-alpine`. Copies `pom.xml` first to cache dependency layers separately (`mvn dependency:go-offline`), ensuring faster subsequent builds.
- **Stage 2 (Runtime):** Uses lightweight `eclipse-temurin:21-jre-alpine` (~90 MB vs ~500 MB JDK), stripping out all compilers, build tools, and source code.
- **Security:** Non-root execution (`USER appuser`) enforces the principle of least privilege.
- **Container Health Check:** Built-in `HEALTHCHECK` directive polls `http://localhost:8080/actuator/health`.

### 5.2 Docker Compose Stack
- Defines an isolated bridge network (`calculator-net`).
- **Orchestration:** Nginx depends on `calculator` using `condition: service_healthy`, ensuring the reverse proxy only routes traffic once the backend Spring Boot JVM has completely initialized and reports `UP`.
- **Restart Policy:** Configured with `restart: unless-stopped` for resilience.

---

## 6. Challenges Faced & Solutions

1. **Docker-in-Docker Daemon Communication (macOS):**
   - *Problem:* Mounting `/var/run/docker.sock` directly into Jenkins failed because macOS Docker Desktop creates the socket at `~/.docker/run/docker.sock`.
   - *Solution:* Mounted the user-level socket `-v /Users/dhruv/.docker/run/docker.sock:/var/run/docker.sock` and created a custom Jenkins image containing the `docker` and `docker-compose` binaries with necessary permissions.

2. **DooD Host Bind Mount Collision:**
   - *Problem:* In Docker-out-of-Docker architectures, bind-mounting a relative path (`./nginx/nginx.conf`) fails because the host engine resolves paths relative to the host OS rather than the Jenkins workspace container.
   - *Solution:* Containerized Nginx with its own dedicated `Dockerfile`, packaging the configuration directly into an immutable image (`jdhruvr/calculator-proxy`). This eliminated host path dependencies completely and made the Compose deployment 100% portable.

3. **Container Network Loopback Resolution:**
   - *Problem:* When Jenkins performs a post-deployment health check against `http://localhost`, it queries its own container loopback rather than the deployed Nginx proxy container.
   - *Solution:* Configured the health probe to test `http://host.docker.internal/actuator/health` with a fallback to `localhost`, enabling reliable probing across host and container contexts.

---

## 7. Verification Evidence & Sample API Responses

### 7.1 Running Containers (`docker ps`)
```text
CONTAINER ID   IMAGE                        STATUS                    PORTS               NAMES
af99dcb66636   jdhruvr/calculator-proxy:1   Up (healthy)              0.0.0.0:80->80/tcp  calculator-proxy
8686d1db9acd   jdhruvr/calculator-api:1     Up (healthy)              8080/tcp            calculator-app
```

### 7.2 API Endpoint Responses (via Port 80 Proxy)
- **Addition:**
  ```bash
  $ curl "http://localhost/api/v1/add?a=15&b=25"
  {"operation":"add","a":15.0,"b":25.0,"result":40.0}
  ```
- **Subtraction:**
  ```bash
  $ curl "http://localhost/api/v1/subtract?a=50&b=20"
  {"operation":"subtract","a":50.0,"b":20.0,"result":30.0}
  ```
- **Multiplication:**
  ```bash
  $ curl "http://localhost/api/v1/multiply?a=6&b=7"
  {"operation":"multiply","a":6.0,"b":7.0,"result":42.0}
  ```
- **Division:**
  ```bash
  $ curl "http://localhost/api/v1/divide?a=100&b=4"
  {"operation":"divide","a":100.0,"b":4.0,"result":25.0}
  ```
- **Division by Zero (HTTP 400):**
  ```bash
  $ curl -i "http://localhost/api/v1/divide?a=10&b=0"
  HTTP/1.1 400 Bad Request
  {"status":400,"error":"Bad Request","message":"Division by zero is not allowed"}
  ```
- **Actuator Health:**
  ```bash
  $ curl "http://localhost/actuator/health"
  {"status":"UP","components":{"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
  ```

---

## 8. Conclusion

The end-to-end CI/CD pipeline satisfies all requirements specified in the rubric:
- Progressive Git history with branching and PR merge.
- Clean Maven packaging with 16 automated JUnit 5 and MockMvc tests.
- Secure, declarative Jenkins automation with credential abstraction.
- Production-hardened multi-stage containerization with non-root privileges.
- Multi-container orchestration with Nginx and Actuator health probes.
