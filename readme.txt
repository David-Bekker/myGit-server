in myGit:
    docker-compose down -v
    docker-compose up -d
in myGit/server:
    .\mvnw.cmd spring-boot:run

clean spring:
    .\mvnw.cmd clean compile
run spring:
    .\mvnw.cmd spring-boot:run
    
check db:
    docker exec -it mygit-db-1 psql -U david_admin -d github_clone
    SELECT * FROM users;
    SELECT * FROM repositories;
    \dt
    INSERT INTO repositories (description, language, name, owner, stars) VALUES ('My first repo', 'Text', 'myrepo', 'sagi', 0);