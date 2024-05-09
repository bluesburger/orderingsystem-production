package br.com.bluesburguer.production.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import br.com.bluesburguer.production.OrderingsystemProductionApplication;

@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(
		classes = { OrderingsystemProductionApplication.class },
		properties = { 
				"spring.main.allow-bean-definition-overriding=true",
				"spring.cloud.bus.enabled=false",
				"spring.cloud.consul.enabled=false", 
				"spring.cloud.consul.discovery.enabled=false",
				"cloud.aws.region.use-default-aws-region-chain=true",
				"cloud.aws.stack.auto=false",
				"cloud.aws.region.auto=false",
				"cloud.aws.stack=false",
				"cloud.aws.sqs.listener.auto-startup=false"
		},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({ "test" })
@ContextConfiguration(classes = OrderingsystemProductionApplication.class)
public abstract class ApplicationIntegrationSupport {

}
