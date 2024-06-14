package fr.abes.item.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI OpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Item Api")
                        .description("Service web de l'application ITEM")
                        .version(this.getClass().getPackage().getImplementationVersion())
                        .contact(new Contact().url("https://github.com/abes-esr/item-api").name("Abes").email("scod@abes.fr")));
    }
}

