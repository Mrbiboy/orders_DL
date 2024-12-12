# orders_DL
# Order Processor Project

## Overview

This Java application processes customer orders stored in a JSON file and interacts with a MySQL database. The system performs the following tasks every hour:

1. Reads orders from an `input.json` file.
2. Checks if the customer for each order exists in the `customer` table in the database.
3. If the customer exists:
   - The order is added to the `order` table in the database.
   - The order is written to an `output.json` file.
4. If the customer does not exist:
   - The order is written to an `error.json` file.
   - The order is removed from `input.json`.

The application is scheduled to run every hour using `ScheduledExecutorService`.

---

## Prerequisites

### Software Requirements

- Java 8 or higher
- MySQL Database
- Maven (for managing dependencies)
- IntelliJ IDEA or any Java IDE

### Dependencies

The project uses the following dependencies:

- [Gson](https://github.com/google/gson) for JSON parsing and writing
- MySQL JDBC Driver for database connection

Add these dependencies in `pom.xml` (if using Maven):

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

---

## Setup Instructions

### 1. Database Configuration

#### Create Database

```sql
CREATE DATABASE orders;
USE orders;
```

#### Create `customer` Table

```sql
CREATE TABLE customer (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL
);
```

#### Create `order` Table

```sql
CREATE TABLE `order` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10, 2) NOT NULL,
    customer_id INT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customer(id)
);
```

#### Insert Sample Data

```sql
INSERT INTO customer (name, email, phone) VALUES
('Alice', 'alice@example.com', '1234567890'),
('Bob', 'bob@example.com', '0987654321');
```

### 2. Project Structure

Ensure the following directory structure:

```
src/
├── main/
    ├── java/
        ├── com/
            ├── example/
                ├── orders/
                    ├── OrderProcessor.java
                    ├── ParseJsonThread.java
  ├── data/
        ├── input.json
        ├── output.json
        ├── error.json
```

### 3. Input Files

#### `input.json`

Create the `input.json` file in `src/resources/data/` with the following structure:

```json
[
    {
        "customer_email": "alice@example.com",
        "amount": 120.50
    },
    {
        "customer_email": "unknown@example.com",
        "amount": 75.00
    }
]
```

---

## Execution

1. Compile and run the application in your Java IDE.
2. The system will:
   - Process orders in `input.json`.
   - Add valid orders to the database and `output.json`.
   - Move invalid orders to `error.json`.
3. The `input.json` file will be updated to remove processed orders.

---

## Key Features

- Scheduled execution every hour using `ScheduledExecutorService`.
- Threaded JSON parsing using `ParseJsonThread`.
- MySQL integration for order and customer management.
- JSON-based input and output for easy data handling.

---

## Logs and Debugging

- Logs are printed to the console to indicate the status of operations (e.g., parsing, database updates).
- Check `error.json` for invalid orders.

---

## Future Improvements

- Add logging to a file for better traceability.
- Enhance error handling for database connectivity issues.
- Implement a REST API for dynamic order input and output.

---

## License

This project is licensed under the MIT License. Feel free to use and modify it.

