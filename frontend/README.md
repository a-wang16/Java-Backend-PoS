To install the local jar make sure you are in the frontend directory and run the command

```bash
./mvnw install:install-file -Dfile=postgresql-42.2.8.jar -DgroupId=org.postgresql -DartifactId=postgresql -Dversion=42.2.8 -Dpackaging=jar
```

Also, try this maybe

```bash
mvn clean install -U
```

In order to connect to database, make sure you have `config.properties` file under `frontend/src/main/resources`.

Install maven through https://maven.apache.org/download.cgi and follow the instructions to put it under your `PATH` variable (https://maven.apache.org/install.html)

Once maven is installed, simply run the following command:

```bash
mvn clean javafx:run
```