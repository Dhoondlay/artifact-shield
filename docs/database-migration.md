# 🗄️ Database Migration: From H2 to PostgreSQL

Artifact-Shield uses an embedded **H2** database by default for easy local development. For production environments, we strongly recommend migrating to a dedicated database like **PostgreSQL** to ensure data durability, concurrent access, and persistence during gateway restarts.

---

## 🚀 1. Why Migrate?
*   **Durability**: H2 in-memory or file-mode is not suitable for high-availability clusters.
*   **Concurrency**: Postgres handles thousands of concurrent audit log writes much more efficiently.
*   **Scale**: Larger rule-sets and longer audit trails are better managed by a dedicated DB engine.

---

## 🛠️ 2. Configuration Options

Artifact-Shield supports multiple database connection options. By default, it uses **H2** for low-friction setup.

### **Option A: Activate Built-in Profiles (Recommended)**
The gateway includes specialized profiles for common enterprise databases. To switch, start the application with the corresponding profile active:

**For PostgreSQL:**
```bash
java -jar artifact-shield.jar --spring.profiles.active=postgres
```

**For MySQL:**
```bash
java -jar artifact-shield.jar --spring.profiles.active=mysql
```

**For Microsoft SQL Server:**
```bash
java -jar artifact-shield.jar --spring.profiles.active=sqlserver
```

**For NoSQL (MongoDB):**
```bash
java -jar artifact-shield.jar --spring.profiles.active=mongodb
```

### **🛡️ Database Override Matrix**
When using these profiles, use these standard environment variables to override default connection settings:

| Database | Variable Name | Example Value |
| :--- | :--- | :--- |
| **Relational** | `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/shield` |
| **Relational** | `SPRING_DATASOURCE_USERNAME` | `shield_admin` |
| **Relational** | `SPRING_DATASOURCE_PASSWORD` | `P@ssw0rd123` |
| **MongoDB** | `SPRING_DATA_MONGODB_URI` | `mongodb://user:pass@host:27017/shield` |

---

## 📦 3. Built-in Drivers
Artifact-Shield comes pre-packaged with drivers for the following engines. You do **not** need to add any external JARs:
*   **H2** (Embedded)
*   **PostgreSQL**
*   **MySQL & MariaDB**
*   **Microsoft SQL Server**
*   **MongoDB** (Reactive)

---

---

## 🕵️ 4. Handling Existing Data
If you have already added custom patterns or LLM configs to the H2 database, you will need to migrate them to PostgreSQL.

### **Manual Export/Import**
1.  **Export H2**: Use the H2 Console to generate a SQL script from your existing tables: `SCRIPT TO 'backup.sql'`.
2.  **Edit SQL**: Adjust any H2-specific data types (e.g., `CLOB`) to PostgreSQL types (e.g., `TEXT`).
3.  **Import to Postgres**: Run the script against your new Postgres instance: `psql -f backup.sql`.

---

## 🌐 5. Advanced: Spring Data R2DBC (Future)
For extreme concurrency, we recommend migrating the persistence layer to **R2DBC** (Reactive Relational Database Connectivity). While the current implementation offloads blocking JPA calls to a dedicated thread pool, R2DBC would make the entire database interaction fully non-blocking on the event loop.

---
*For more help, contact your database administrator or visit the [Artifact-Shield internal wiki].*
