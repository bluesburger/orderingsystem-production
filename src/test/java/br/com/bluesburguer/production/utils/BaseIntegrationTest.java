package br.com.bluesburguer.production.utils;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import br.com.bluesburguer.production.OrderingsystemProductionApplication;
import br.com.bluesburguer.production.adapters.in.sqs.OrderStatusUpdatedEventConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(
		classes = { OrderingsystemProductionApplication.class },
		properties = { 
				"spring.main.allow-bean-definition-overriding=true",
//				"server.port=0", 
				"spring.cloud.bus.enabled=false",
				"spring.cloud.consul.enabled=false", 
				"spring.cloud.consul.discovery.enabled=false"
		},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({ "test" })
@ContextConfiguration(classes = OrderingsystemProductionApplication.class)
@Testcontainers
public abstract class BaseIntegrationTest {
	
	@ClassRule
	public static final DockerImageName LOCALSTACK_IMG_NAME = 
			DockerImageName.parse("localstack/localstack:3.0");
	
	private static Network network = Network.newNetwork();
	
	@Container
	static LocalStackContainer localstackInDockerNetwork = new LocalStackContainer(
			LOCALSTACK_IMG_NAME
		)
			.withEnv("DEBUG", "1")
			.withEnv("HOSTNAME_EXTERNAL", "localstack")
		    .withNetwork(network)
		    .withNetworkAliases("notthis", "localstack")
			.withServices(Service.SQS);
	
	static final String BUCKET_NAME = UUID.randomUUID().toString();
	
	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.cloud.aws.region.static", localstackInDockerNetwork.getRegion()::toString);
	    registry.add("spring.cloud.aws.credentials.access-key", localstackInDockerNetwork.getAccessKey()::toString);
	    registry.add("spring.cloud.aws.credentials.secret-key", localstackInDockerNetwork.getSecretKey()::toString);
	    registry.add("spring.cloud.aws.sqs.endpoint", localstackInDockerNetwork.getEndpointOverride(Service.SQS)::toString);
	}

	@BeforeAll
	static void beforeAll() throws IOException, InterruptedException {
		List.of(
				OrderStatusUpdatedEventConsumer.ORDER_PAID_QUEUE,
				OrderStatusUpdatedEventConsumer.ORDER_IN_PRODUCTION_QUEUE,
				OrderStatusUpdatedEventConsumer.ORDER_PRODUCED_QUEUE,
				OrderStatusUpdatedEventConsumer.ORDER_DELIVERING_QUEUE,
				OrderStatusUpdatedEventConsumer.ORDER_DELIVERED_QUEUE,
				OrderStatusUpdatedEventConsumer.ORDER_CANCELED_QUEUE
		).forEach(queueName -> {
			try {
				log.info("Creating queue {}", queueName);
				localstackInDockerNetwork.execInContainer(
						"awslocal",
						"sqs",
						"create-queue",
						"--queue-name",
						queueName,
						"--attributes",
						"\"FifoQueue=true\""
				);
			} catch (UnsupportedOperationException | IOException | InterruptedException e) {
				log.error("Falha ao tentar criar queue {}", queueName, e);
			}
		});
		
		try {
			log.info("Listando queues");
			localstackInDockerNetwork.execInContainer(
					"awslocal",
					"sqs",
					"list-queues"
			);
		} catch (UnsupportedOperationException | IOException | InterruptedException e) {
			log.error("Falha ao tentar listar queues", e);
		}
	}

}
