package fr.abes.item.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
public class Application {

    private static Environment env;

    public Application(Environment environment) {
        env = environment;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setAdditionalProfiles("batch"); // Activer le profil "batch"
        app.run(args);
        System.out.println("App launched." +
                " | Env:" + Arrays.toString(env.getActiveProfiles()) +
                " | Port:" + env.getProperty("server.port") +
                " | Module:" + "batch"

        );
        System.out.println("Connected to." +
                " | BDD:" + env.getProperty("spring.datasource.item.jdbcurl") +
                " | XMLBase:" + env.getProperty("spring.datasource.basexml.jdbcurl")

        );
        System.out.println("Level of log." +
                " | App: " + env.getProperty("logging.level.fr.abes") +
                " | Spring: " + env.getProperty("logging.level.root")
        );
    }
}
