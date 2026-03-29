# 🛠️ Installation Guide

Welcome to **Artifact-Shield**! This guide will walk you through setting up the Reactive AI Security Gateway on your local machine for development or evaluation.

---

## 📋 Prerequisites
Before you begin, ensure you have the following installed:
*   **Java 17 (or higher)**: The core language for the Spring Boot application.
*   **Git**: For cloning the repository.
*   *(Optional but recommended)* **Maven 3.8+**: While the repo includes a Maven Wrapper (`mvnw`), having Maven installed globally can be helpful.

---

## 📥 1. Clone the Repository
First, clone the source code to your local machine:

```bash
git clone https://github.com/dhoondlay/artifact-shield.git
cd artifact-shield
```

---

## 🏗️ 2. Build the Application
Artifact-Shield uses Maven to manage dependencies and build the artifact. Run the following command to compile the code and run the unit tests.

### **Windows**
```cmd
mvnw.cmd clean install
```

### **macOS / Linux**
```bash
./mvnw clean install
```
*(Note: This process will automatically download the necessary Spring Boot and WebFlux dependencies).*

---

## 🚀 3. Run the Gateway Locally
Once the build is successful, you can start the application using the Spring Boot plugin. By default, it uses the embedded **H2 Database** for immediate, configuration-free startup.

### **Windows**
```cmd
mvnw.cmd spring-boot:run
```

### **macOS / Linux**
```bash
./mvnw spring-boot:run
```

### **Verify Success**
You should see the Spring ASCII art and a log message indicating the Netty server has started:
```text
Netty started on port 8080 (http)
```

---

## 🎮 4. Access the Dashboard & API
The gateway is now running! 
*   **Admin Dashboard**: Open your browser to [http://localhost:8080](http://localhost:8080) to view live metrics and active patterns.
*   **Swagger API Docs**: Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) to interactively test the REST API.
*   **H2 Database Console**: Open [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:file:./data/shield_db`, User: `sa`, Password: `password`).

---
### ➡️ Next Steps
Now that the gateway is running, head over to the **[Configuration Reference](/configuration-reference)** to learn how to customize properties, or review the **[API Reference](/api-reference)** to send your first sanitization payload!
