in myGit:
    docker compose down -v
    docker compose up
in myGit/server:
    .\mvnw.cmd spring-boot:run