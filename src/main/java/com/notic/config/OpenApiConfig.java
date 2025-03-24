package com.notic.config;


import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notic")
                        .version("1.0")
                        .description("Notic api documentation")
                );
    }

}
