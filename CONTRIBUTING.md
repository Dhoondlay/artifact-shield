# 🤝 Contributing to Artifact-Shield

Welcome! We are excited that you want to contribute to **Artifact-Shield**. As an open-source project, we rely on the community to help us build the most secure, high-performance AI gateway in the world.

---

## 🚀 Getting Started

### 1. Prerequisite Checklist
*   **Java 17** (LTS) or higher.
*   **Maven 3.8+**.
*   **IntelliJ IDEA** (Recommended) or VS Code.
*   **Docker Desktop** (For local Postgres/Splunk testing).

### 2. Fork & Clone
1.  Fork the repository on GitHub.
2.  Clone your fork locally:
    ```bash
    git clone https://github.com/your-username/artifact-shield.git
    cd artifact-shield
    ```

### 3. Setup Your Environment
Artifact-Shield uses an embedded H2 database by default for easy development.
1.  Run the application:
    ```bash
    ./mvnw spring-boot:run
    ```
2.  Access the H2 Console: `http://localhost:8080/h2-console`
    *   **JDBC URL**: `jdbc:h2:mem:shielddb`
    *   **User**: `sa`
    *   **Password**: `password`

---

## 🧪 Development Workflow

### **Coding Standards (Reactive First)**
Artifact-Shield is a **fully reactive** project. 
*   **NEVER** use `Thread.sleep()` or blocking I/O on the event loop.
*   Always use `Mono` and `Flux` for service returns.
*   If you must use a blocking library (like JPA), always offload to `Schedulers.boundedElastic()`.

### **Testing Requirements**
We maintain **99% code coverage**. 
*   Use `WebTestClient` for API tests.
*   Ensure all new `Detector` logic has corresponding unit tests in `src/test/java/io/dhoondlay/shield/detector/`.
*   Run the full suite before committing:
    ```bash
    ./mvnw test
    ```

---

## 📬 Submitting a Pull Request

1.  **Create a Branch**: Use a descriptive name: `feat/new-detector-azure` or `fix/ssl-handshake-bug`.
2.  **Commit Messages**: Follow standard conventional commits: `feat: add support for Azure OpenAI endpoints`.
3.  **Push & PR**: Push to your fork and open a PR against our `main` branch.
4.  **Describe Your Changes**: Link to issues, explain the "Why," and include any new configuration keys.

---

## 🛡️ Security Vulnerabilities
Please **do not** report security vulnerabilities in public GitHub issues. Instead, follow the instructions in our [SECURITY.md](SECURITY.md) for private disclosure.

---
*Thank you for making AI safer for everyone! — The Artifact-Shield Maintainers*
