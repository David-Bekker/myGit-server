package com.myGit.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {

public static void main(String[] args) {
    // This tells Spring's ASM scanner to chill out about Java 25
    System.setProperty("spring.classformat.ignore", "true"); 
    SpringApplication.run(ServerApplication.class, args);
}

}
