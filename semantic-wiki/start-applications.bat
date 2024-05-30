@echo off

REM Start Fuseki Server
start cmd /k "cd .. && cd fuseki && fuseki-server"

REM Start the Spring Boot application
start cmd /k "cd embapi && mvn clean install && mvn spring-boot:run"

REM Start the React application
start cmd /k "cd web-view && npm install && npm start"

echo "Applications have been started."


