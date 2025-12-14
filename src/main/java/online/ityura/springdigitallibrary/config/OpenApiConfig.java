package online.ityura.springdigitallibrary.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Digital Bookstore API")
                        .version("1.0.0")
                        .description("REST API для цифрового книжного магазина. " +
                                "Поддерживает управление книгами, отзывами, рейтингами и скачивание PDF файлов.\n" +
                                "Данный проект был создан исключительно в целях самообучения при изучении Spring Boot" +
                                " в далеком 2023 году. Может содержать баги. Активно не поддерживается, однако " +
                                "работает. Может использоваться QA для тренировки тестирования бэкенда в связке с БД." +
                                "Для подключения к БД PostgreSQL используйте следующие " +
                                "параметры подключения:\n" +
                                "  IP: 176.126.103.46\n" +
                                "  Database: spring_digital_bookstore\n" +
                                "  Username: readonly_user\n" +
                                "  Password: pass_123_XYZ!\n ")
                        .contact(new Contact()
                                .name("Developer contact")
                                .url("https://github.com/01Yura"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}

