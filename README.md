# ⚡ Electricity Usage Tracker

A **console-based application developed using Core Java** to manage residents' electricity meter readings, calculate monthly electricity consumption, and generate usage reports. The application provides separate menus for operators and residents, with persistent data storage using file handling.

## 📌 Project Overview

The Electricity Usage Tracker simplifies electricity consumption tracking by maintaining opening meter readings and recording month-end readings for three consecutive months. It calculates the electricity units consumed between readings and stores resident details and usage reports in text files.

The project demonstrates practical implementation of **Object-Oriented Programming (OOP), Java Collections Framework, exception handling, input validation, and file handling**.

## ✨ Features

- **Resident Management:** Register residents with their names, unique meter numbers, and opening meter readings.
- **Monthly Meter Readings:** Record electricity meter readings for three consecutive months.
- **Usage Calculation:** Calculate electricity consumption by finding the difference between consecutive meter readings.
- **Resident Menu:** Allow residents to view their electricity usage information.
- **Duplicate Prevention:** Prevent duplicate meter numbers during registration.
- **Input Validation:** Handle invalid, empty, and inconsistent inputs.
- **File-Based Storage:** Save resident details and usage reports for future access.
- **Usage Reports:** Generate reports containing electricity consumption details.
- **Data Persistence:** Load previously stored resident information when the application starts.

## 🛠️ Technologies Used

- Java
- Object-Oriented Programming (OOP)
- Java Collections Framework
- Java I/O and NIO
- Exception Handling
- Text File Storage

## 🧠 Core Java Concepts Implemented

| Concept | Implementation |
|---|---|
| Encapsulation | Private fields and getter methods to protect data |
| Inheritance | `Operator` and `Resident` extend the common `User` class |
| Abstraction | Abstract `User` class defines common user behavior |
| Polymorphism | Method overriding for user profile behavior |
| ArrayList | Maintains a collection of registered residents |
| HashSet | Tracks meter numbers to prevent duplicates |
| Arrays | Stores the opening reading and three monthly readings |
| File Handling | Reads and writes resident details and usage reports |
| Exception Handling | Manages file-related errors and application failures |
| Input Validation | Validates resident details and meter readings |

## 📂 Project Structure

```text
Electricity-Usage-Tracker/
├── src/
│   ├── Main.java
│   ├── User.java
│   ├── Operator.java
│   ├── Resident.java
│   ├── UsageService.java
│   └── FileManager.java
├── data/
│   ├── residents.txt
│   └── report.txt
└── README.md
```

*Note: The structure above illustrates the main classes and data files. Adjust the paths to match your actual repository.*

## ⚙️ How It Works

1. Launch the application.
2. Select the Operator or Resident menu.
3. Register a resident with the required details.
4. Record monthly electricity meter readings in chronological order.
5. Calculate electricity consumption using the difference between consecutive readings.
6. View usage details and generate reports.
7. Resident data and reports are stored in text files for later use.

**Usage Formula:**

```text
Units Consumed = Current Meter Reading - Previous Meter Reading
```

For example, if the previous reading is 1,250 and the current reading is 1,430:

```text
Units Consumed = 1430 - 1250 = 180 units
```

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) installed
- An IDE such as IntelliJ IDEA, Eclipse, or VS Code, or a terminal

### Installation and Execution

1. Clone the repository:

   ```bash
   git clone <your-repository-url>
   ```

2. Navigate to the project directory:

   ```bash
   cd Electricity-Usage-Tracker
   ```

3. Compile the Java source files. If your files are in the `src` directory:

   ```bash
   javac -d out src/*.java
   ```

4. Run the application:

   ```bash
   java -cp out Main
   ```

*Ensure the source paths and package structure match your actual project before running these commands.*

## 💾 Data Storage

The application uses text files instead of a database:

- `residents.txt` — stores registered resident details and meter readings.
- `report.txt` — stores electricity usage report information.

Java file-handling APIs are used to create directories, read stored data, and write updated records. UTF-8 encoding helps maintain consistent text storage.

## 🎯 Learning Outcomes

Through this project, I gained practical experience in:

- Designing a modular Java application using OOP principles.
- Choosing suitable collections for data management.
- Implementing validation and exception handling.
- Performing file operations using Java I/O and NIO.
- Separating application menus, business logic, and file operations.
- Building a functional console application that maintains data between executions.

## 🔮 Future Enhancements

- Integrate a database such as MySQL.
- Develop a graphical user interface.
- Add electricity tariff calculations and bill generation.
- Introduce authentication and role-based access control.
- Generate monthly and yearly consumption analytics.

## 👨‍💻 Author

**Kota Aswanth Rakesh**

Core Java Project | Electricity Usage Tracker

---

*This project was developed as a practical implementation of Core Java programming concepts and application development fundamentals.*
