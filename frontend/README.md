To install the local jar make sure you are in the frontend directory and run the command

```bash
./mvnw install:install-file -Dfile=postgresql-42.2.8.jar -DgroupId=org.postgresql -DartifactId=postgresql -Dversion=42.2.8 -Dpackaging=jar
```