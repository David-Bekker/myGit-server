in myGit:
    docker-compose down -v
    docker-compose up -d
in myGit/server:
    .\mvnw.cmd spring-boot:run

clean spring:
    .\mvnw.cmd clean compile
    