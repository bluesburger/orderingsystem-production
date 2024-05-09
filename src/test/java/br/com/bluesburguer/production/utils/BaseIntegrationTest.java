package br.com.bluesburguer.production.utils;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
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
				"spring.cloud.consul.discovery.enabled=false",
				"cloud.aws.region.use-default-aws-region-chain=true",
				"cloud.aws.stack.auto=false",
				"cloud.aws.region.auto=false",
				"cloud.aws.stack=false",
				"cloud.aws.sqs.listener.auto-startup=true"
		},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({ "test" })
@ContextConfiguration(classes = OrderingsystemProductionApplication.class)
@Import(SqsTestConfig.class)
@Testcontainers
@DirtiesContext
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
//			.withEnv("HOSTNAME_EXTERNAL", "localstack")
			.withEnv("SQS_ENDPOINT_STRATEGY", "domain")
			.withEnv("LOCALSTACK_HOST", "127.0.0.1")
		    .withNetwork(network)
		    .withNetworkAliases("notthis", "localstack")
			.withServices(Service.SQS);
	
	static final String BUCKET_NAME = UUID.randomUUID().toString();
	
	protected static List<String> TEST_QUEUES = List.of(
			"d29e1ed0-fc99-48d9-81da-6c092651e5e0", 
			"48905674-4e2e-4503-a49d-15c7306f0fe1", 
			"d98c39f7-5118-43ba-afc9-027aa2809d53", 
			"99eb38e9-f131-43c8-bfb3-daa3386acc65", 
			"025ad62b-5216-4896-ae15-0449792aac6d", 
			"55855949-9c22-4ae4-bfdf-bef1510b16cd"
	);
	
	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("cloud.aws.region.static", localstackInDockerNetwork.getRegion()::toString);
	    registry.add("cloud.aws.credentials.access-key", localstackInDockerNetwork.getAccessKey()::toString);
	    registry.add("cloud.aws.credentials.secret-key", localstackInDockerNetwork.getSecretKey()::toString);
	    registry.add("cloud.aws.end-point.uri", localstackInDockerNetwork.getEndpointOverride(Service.SQS)::toString);
	    
	    registry.add("queue.order.paid", () -> TEST_QUEUES.get(0));
	    registry.add("queue.order.in-production", () -> TEST_QUEUES.get(1));
	    registry.add("queue.order.produced", () -> TEST_QUEUES.get(2));
	    registry.add("queue.order.delivering", () -> TEST_QUEUES.get(3));
	    registry.add("queue.order.delivered", () -> TEST_QUEUES.get(4));
	    registry.add("queue.order.canceled", () -> TEST_QUEUES.get(5));
	}

	@BeforeAll
	static void beforeAll() throws IOException, InterruptedException {
		TEST_QUEUES.forEach(queueName -> {
			try {
				log.info("Creating queue {}", queueName);
				localstackInDockerNetwork.execInContainer(
						"awslocal",
						"sqs",
						"create-queue",
						"--queue-name",
						queueName
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
