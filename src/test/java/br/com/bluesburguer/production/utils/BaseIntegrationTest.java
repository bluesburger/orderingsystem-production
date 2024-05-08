package br.com.bluesburguer.production.utils;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import br.com.bluesburguer.production.OrderingsystemProductionApplication;

@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(
		classes = { OrderingsystemProductionApplication.class },
		properties = { "spring.main.allow-bean-definition-overriding=true" },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({ "test" })
public class BaseIntegrationTest {

}
