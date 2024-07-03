package br.com.bluesburguer.production.framework;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
		info = @Info(
				title = "Ordering System Production",
				version = "v2.0"
		)
)
@Configuration
public class OpenApiConfig {

}
