# How to run the Online Examination System on Windows

Follow the steps in order. Each step takes a few minutes.
If something goes wrong, the **Problems** section at the end covers the common ones.

---

## Step 1 — Install Java (JDK 21)

1. Go to **https://adoptium.net/temurin/releases/?version=21**
2. Choose **Operating System: Windows**, **Architecture: x64**, **Package Type: JDK**
3. Download the **.msi** file and run it.
4. During the install, when it asks about **"Set JAVA_HOME variable"** and
   **"Add to PATH"**, click the dropdown and choose **"Will be installed on local hard drive"**
   for both. This matters — skipping it is the most common cause of problems later.

**Check it worked.** Open **Command Prompt** (press Windows key, type `cmd`, press Enter) and run:

```
java -version
```

You should see something starting with `openjdk version "21`.

---

## Step 2 — Install MySQL 8.0

1. Go to **https://dev.mysql.com/downloads/installer/**
2. Download **mysql-installer-community** (the larger file, about 300 MB).
3. Run it and choose the **Developer Default** setup type.
4. Keep clicking Next / Execute through the installation.
5. When it asks for a **Root Password**, type:

```
root
```

   Use exactly that word. The project is already set up for it, so you will not have to
   change any code. (If you choose a different password, see Step 5.)

6. Finish the installation. **MySQL Workbench** is installed along with it — you will use it
   in Step 4.

---

## Step 3 — Install Maven

1. Go to **https://maven.apache.org/download.cgi**
2. Download **apache-maven-3.9.x-bin.zip** (under "Binary zip archive").
3. Right-click the downloaded zip → **Extract All** → extract to `C:\`
   You should end up with a folder like `C:\apache-maven-3.9.9`
4. Press the Windows key, type **environment variables**, and open
   **"Edit the system environment variables"**.
5. Click **Environment Variables...**
6. In the lower box (**System variables**), select **Path** and click **Edit**.
7. Click **New** and paste the bin folder path:

```
C:\apache-maven-3.9.9\bin
```

   (change the version number to match the folder you actually extracted)
8. Click OK on all three windows.
9. **Close Command Prompt and open a new one** — the PATH only updates in new windows.

**Check it worked.** In the new Command Prompt:

```
mvn -version
```

You should see `Apache Maven 3.9...`

---

## Step 4 — Download the project and create the database

### 4a. Download the project

Go to **https://github.com/yuvipol08/Online-Examination-System-Java**

Click the green **Code** button → **Download ZIP**.

Extract the ZIP. Move the extracted folder to somewhere simple, for example:

```
C:\Projects\Online-Examination-System-Java
```

Avoid folders with spaces in the name (like Desktop\My Project) — it makes the commands harder.

### 4b. Create the database

1. Open **MySQL Workbench** (it was installed in Step 2).
2. Click the connection tile named **Local instance MySQL80**. Enter the password `root`.
3. In the menu, click **File → Open SQL Script...**
4. Browse to your project folder and open:

```
C:\Projects\Online-Examination-System-Java\database\online_exam_db.sql
```

5. Click the **lightning bolt icon** (⚡) in the toolbar to run the whole script.
6. On the left panel, click the refresh arrow next to **SCHEMAS**. You should now see
   **online_exam_db** with six tables inside it: `answers`, `exams`, `questions`,
   `results`, `subjects`, `users`.

---

## Step 5 — Only if you used a different MySQL password

**Skip this step if you set the MySQL root password to `root` in Step 2.**

If you used a different password, open this file in Notepad:

```
C:\Projects\Online-Examination-System-Java\src\main\java\com\onlineexam\db\DBConnection.java
```

Find this line near the top:

```java
private static final String PASSWORD = "root";
```

Change `root` to your password, and save the file.

---

## Step 6 — Build and run

Open **Command Prompt** and run these two commands one after the other.

**Go to the project folder:**

```
cd C:\Projects\Online-Examination-System-Java
```

**Build it** (the first time this takes 2–3 minutes because it downloads the MySQL driver —
you need internet for this step):

```
mvn clean package
```

Wait for **BUILD SUCCESS**.

**Run it:**

```
java -jar target\online-examination-system.jar
```

The login window opens.

---

## Step 7 — Log in

| Who | Email | Password |
|---|---|---|
| Administrator | `admin@exam.com` | `admin123` |
| Student | `student@exam.com` | `stud123` |
| Student | `ankita@exam.com` | `ankita123` |

Log in as the **administrator** to see the subjects, exams, questions, students and results.

Log in as a **student** to write an exam: click **Available Exams**, select a row, click
**Start Exam**, answer the questions and click **Submit Exam**. The marks and the full
answer sheet appear straight away.

---

## Running it again later

Once it is set up, you only need this each time:

1. Make sure MySQL is running (it starts automatically with Windows by default).
2. Open Command Prompt and run:

```
cd C:\Projects\Online-Examination-System-Java
java -jar target\online-examination-system.jar
```

You do **not** need to run `mvn clean package` again unless you change the code.

---

## Problems

### "Could not connect to the MySQL database"

The application shows this message and closes. It means one of three things:

- **MySQL is not running.** Press Windows key → type `services.msc` → Enter. Find **MySQL80**
  in the list. If the Status column is blank, right-click it and choose **Start**.
- **The password is wrong.** See Step 5.
- **The database was never created.** Redo Step 4b.

### "Unknown database 'online_exam_db'"

The SQL script did not run. Redo Step 4b and make sure you clicked the lightning bolt and
saw green ticks in the Output panel at the bottom of Workbench.

### `'mvn' is not recognized as an internal or external command`

Maven is not on the PATH. Redo Step 3, and make sure you **opened a new Command Prompt**
afterwards — an old window keeps the old PATH.

### `'java' is not recognized...`

Same problem with Java. Reinstall the JDK from Step 1 and make sure you turned on
**"Add to PATH"** during the install.

### `BUILD FAILURE` with `Fatal error compiling: invalid target release: 21`

An older Java is installed. Run `java -version` — if it says 17, 11 or 8, install JDK 21
from Step 1.

### The build hangs or fails at "Downloading from central"

You have no internet connection, or a firewall is blocking it. Maven has to download the
MySQL driver the first time. Connect to the internet and run `mvn clean package` again.

### The window is too big or cut off on my screen

It sizes itself to fit the screen, but if the display scaling is unusual, right-click the
window title bar and choose Maximize.

---

## If you prefer to use an IDE instead of the command line

You can skip Step 3 (Maven) entirely:

1. Install **IntelliJ IDEA Community Edition** (free) from
   **https://www.jetbrains.com/idea/download/** — scroll down to *Community Edition*.
2. Open IntelliJ → **Open** → select the folder `C:\Projects\Online-Examination-System-Java`
3. Wait for the bottom status bar to finish "Resolving Maven dependencies".
4. In the Project panel on the left, open
   `src` → `main` → `java` → `com.onlineexam` → **MainApp**
5. Right-click **MainApp** → **Run 'MainApp.main()'**

You still need Steps 1, 2 and 4.
