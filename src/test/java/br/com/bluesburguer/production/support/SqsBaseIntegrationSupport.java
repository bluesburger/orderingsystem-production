package br.com.bluesburguer.production.support;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import br.com.bluesburguer.production.framework.SqsTestConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Import(SqsTestConfig.class)
@Testcontainers
@DirtiesContext
public abstract class SqsBaseIntegrationSupport extends ApplicationIntegrationSupport {
	
	@ClassRule
	public static final DockerImageName LOCALSTACK_IMG_NAME = 
			DockerImageName.parse("localstack/localstack:3.0");
	
	private static Network network = Network.newNetwork();
	
	@Container
	static LocalStackContainer localstackInDockerNetwork = new LocalStackContainer(
			LOCALSTACK_IMG_NAME
		)
			.withEnv("DEBUG", "1")
			.withEnv("SQS_ENDPOINT_STRATEGY", "domain")
			.withEnv("LOCALSTACK_HOST", "127.0.0.1")
		    .withNetwork(network)
		    .withNetworkAliases("notthis", "localstack")
			.withServices(Service.SQS);
	
	static final String BUCKET_NAME = UUID.randomUUID().toString();
	
	protected static List<String> TEST_QUEUES = List.of(
			"d29e1ed0-fc99-48d9-81da-6c092651e5e0.fifo", 
			"48905674-4e2e-4503-a49d-15c7306f0fe1.fifo", 
			"d98c39f7-5118-43ba-afc9-027aa2809d53.fifo", 
			"99eb38e9-f131-43c8-bfb3-daa3386acc65.fifo", 
			"025ad62b-5216-4896-ae15-0449792aac6d.fifo", 
			"55855949-9c22-4ae4-bfdf-bef1510b16cd.fifo",
			"dc9ffbcb-2152-4c7b-9b55-5397f87a069f.fifo",
			"db2a26e7-d550-4c5e-ac11-ac7d20e90d80.fifo",
			"cb0cab3e-2390-4df2-a8b8-c8b6b6c6c6a6.fifo",
			"b4072b6f-625c-480f-b10e-50b105f89c1e.fifo"
	);
	
	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("cloud.aws.sqs.listener.auto-startup", () -> "true");
		registry.add("cloud.aws.region.static", localstackInDockerNetwork.getRegion()::toString);
	    registry.add("cloud.aws.credentials.access-key", localstackInDockerNetwork.getAccessKey()::toString);
	    registry.add("cloud.aws.credentials.secret-key", localstackInDockerNetwork.getSecretKey()::toString);
	    registry.add("cloud.aws.end-point.uri", localstackInDockerNetwork.getEndpointOverride(Service.SQS)::toString);
	    registry.add("cloud.aws.accountId", () -> "000000000000");
	    
	    registry.add("queue.order.registered", () -> TEST_QUEUES.get(0));
	    registry.add("queue.order.confirmed", () -> TEST_QUEUES.get(1));
	    registry.add("queue.order.canceled", () -> TEST_QUEUES.get(2));
	    
	    registry.add("queue.order.paid", () -> TEST_QUEUES.get(3));
	    registry.add("queue.order.failed-on-payment", () -> TEST_QUEUES.get(4));
	    registry.add("queue.order.scheduled", () -> TEST_QUEUES.get(5));
	    
	    registry.add("queue.order.failed-delivery", () -> TEST_QUEUES.get(6));
	    registry.add("queue.order.performed-delivery", () -> TEST_QUEUES.get(7));
	    registry.add("queue.order.invoice-issued", () -> TEST_QUEUES.get(8));
	    registry.add("queue.order.invoice-failed-issued", () -> TEST_QUEUES.get(9));	
	}

	@BeforeAll
	static void beforeAll() throws IOException, InterruptedException {
		for(String queueName : TEST_QUEUES) {
			log.info("Creating queue {}", queueName);
			localstackInDockerNetwork.execInContainer(
					"awslocal",
					"sqs",
					"create-queue",
					"--queue-name",
					queueName,
					"--attributes",
					"FifoQueue=true"
			);
		}
		
		log.info("Listando queues");
		localstackInDockerNetwork.execInContainer(
				"awslocal",
				"sqs",
				"list-queues"
		);
	}

}
