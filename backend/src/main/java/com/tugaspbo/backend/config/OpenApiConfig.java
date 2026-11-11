package com.tugaspbo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI cafeOpenAPI() {
		String schemeName = "bearerAuth";
		return new OpenAPI()
				.info(new Info()
						.title("Cafe Ordering System API")
						.description("REST API for a cafe ordering system — Tugas PBO")
						.version("1.0.0"))
				.addSecurityItem(new SecurityRequirement().addList(schemeName))
				.components(new Components().addSecuritySchemes(schemeName,
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
