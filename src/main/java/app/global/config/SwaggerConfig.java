package app.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("order API")
				.description("order API 문서")
				.version("v1.0.0"))
			.components(new Components()
				.addSecuritySchemes("bearer-jwt",
					new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.in(SecurityScheme.In.HEADER)
						.name("Authorization")))
			.addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
	}
}

