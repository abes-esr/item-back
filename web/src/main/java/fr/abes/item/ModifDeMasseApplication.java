package fr.abes.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;


@SpringBootApplication
public class ModifDeMasseApplication extends SpringBootServletInitializer {
    private static Environment env;

    public ModifDeMasseApplication(Environment environment) {
        env = environment;
    }

	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ModifDeMasseApplication.class);
    }

    public static void main(String[] args) {
	    SpringApplication.run(ModifDeMasseApplication.class, args);
        System.out.println("App launched." +
                " | Env:" + Arrays.toString(env.getActiveProfiles()) +
                " | Port:" + env.getProperty("server.port") +
                " | Module:" + "web"

        );
        System.out.println("Connected to." +
                " | BDD:" + env.getProperty("spring.datasource.item.jdbcurl") +
                " | XMLBase:" + env.getProperty("spring.datasource.basexml.jdbcurl")

        );
        System.out.println("Level of log." +
                " | App: " + env.getProperty("logging.level.fr.abes") +
                " | Tomcat: " + env.getProperty("logging.level.tomcat") +
                " | Web: " + env.getProperty("logging.level.web") +
                " | Sql: " + env.getProperty("logging.level.sql") +
                " | Spring: " + env.getProperty("logging.level.spring")
        );
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> simpleCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // *** URL below needs to match the Vue client URL and port ***

        // Config for allowing CORS for all (local, dev, test, prod, external ...)
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

}