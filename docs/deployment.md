# 🚀 Deployment Guide

Once you have tested **Artifact-Shield** locally, you will want to deploy it to a staging or production environment. Artifact-Shield is built as an executable Spring Boot fat JAR, making it trivial to run on any cloud platform or orchestration engine.

---

## 🐳 1. Docker Deployment (Recommended)
Docker is the preferred way to run Artifact-Shield in production, ensuring a consistent and isolated runtime environment.

### **Building the Image**
A standard `Dockerfile` is included in the root of the repository.

```bash
# First, ensure the jar is built
./mvnw clean package -DskipTests

# Build the Docker image
docker build -t dhoondlay/artifact-shield:1.2.0 .
```

### **Running the Container**
When running in production, you should map the `/app/data` directory to a persistent volume so that your H2 database configurations and audit logs survive container restarts.

```bash
docker run -d \
  --name artifact-shield \
  -p 8080:8080 \
  -v shield-data:/app/data \
  -e SHIELD_SECURITY_ENABLED=true \
  dhoondlay/artifact-shield:1.2.0
```

*Note: Any `application.yml` property can be overridden via environment variables (e.g., `shield.security.enabled` becomes `SHIELD_SECURITY_ENABLED`).*

---

## 📦 2. Docker Compose
If you want to run the gateway alongside a production PostgreSQL database, the included `docker-compose.yml` makes this seamless.

```yaml
version: '3.8'
services:
  artifact-shield:
    image: dhoondlay/artifact-shield:1.2.0
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/shield
      - SPRING_DATASOURCE_USERNAME=shield_user
      - SPRING_DATASOURCE_PASSWORD=shield_pass
    depends_on:
      - db

  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=shield
      - POSTGRES_USER=shield_user
      - POSTGRES_PASSWORD=shield_pass
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

### **Start the Stack**
```bash
docker-compose up -d
```

---

## ☕ 3. Bare Metal (JAR Execution)
If you prefer running services outside of containers (e.g., on a dedicated Linux VM using `systemd`):

1.  **Transfer** the compiled `target/artifact-shield.jar` to your server.
2.  **Run** the jar file, overriding the active Spring profile or configurations if necessary:

```bash
java -Xmx2G -jar artifact-shield.jar \
  --spring.profiles.active=prod \
  --shield.block-critical-risk=true
```

*(We highly recommend using `-Xmx2G` or higher to provide the JVM enough heap space for intense regex matching).*

---

## ☁️ 4. Kubernetes (K8s) Strategy
For hyper-scale enterprise deployments:

*   **Liveness / Readiness**: Map your probes to `http://<pod-ip>:8080/actuator/health`.
*   **Ingress**: Place the gateway behind an Ingress controller, routing `/v1/shield/**` traffic directly to the pods.
*   **Database**: Connect the pods to a managed AWS RDS or Azure Postgres instance via Kubernetes `Secrets`.

---
*For more help, contact your DevOps team or visit the [Artifact-Shield internal wiki].*
