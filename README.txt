EVENT RESERVATION - JAVA REGISTER / LOGIN / LOGOUT STARTER

Requirements:
- IntelliJ IDEA
- JDK 17 or newer
- Your existing database file named exactly: EventReservation.db

IMPORTANT DATABASE LOCATION:
Put EventReservation.db in the project's ROOT folder, beside pom.xml.
The connection uses: jdbc:sqlite:EventReservation.db
If the database is stored somewhere else, edit DB_URL in:
src/main/java/com/eventreservation/dao/DatabaseConnection.java

Database compatibility:
This project expects a SQLite table named "user" with columns:
user_id, full_name, username, password, user_role, account_status.
If the table does not exist, the app creates it. If your existing table has
different column names, update the SQL in UserDao.java to match your table.
The app does not delete or reset your existing database data.

Run in IntelliJ:
1. Extract this ZIP.
2. Copy your EventReservation.db into the EventReservationJava folder (beside pom.xml).
3. Open the folder in IntelliJ IDEA and choose "Open as Project".
4. Wait for Maven to download the SQLite JDBC dependency.
5. Set Project SDK to JDK 17 or newer.
6. Run src/main/java/com/eventreservation/Main.java, or use Maven:
   mvn clean compile exec:java

Features:
- Register with role STUDENT or TEACHER.
- Validates required fields and username uniqueness.
- Password must be at least 8 characters.
- New accounts get PENDING status automatically.
- Login checks the username and password against SQLite.
- Pending status is shown after login; this starter does not include an approval screen.
- Logout clears the current session and returns to the main menu.
- Exit closes the application.

Security note:
For a classroom starter, this version stores the password in the requested `password`
column to match the existing schema. For a real application, use password hashing
and never store plain-text passwords.
