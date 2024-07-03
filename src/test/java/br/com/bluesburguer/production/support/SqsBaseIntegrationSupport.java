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
			"b4072b6f-625c-480f-b10e-50b105f89c1e.fifo",
			"da33790f-8a8c-4fcd-9c99-0e432779ecba.fifo",
			"8c519ad1-b7ff-4906-83bf-8d91bb843544.fifo",
			"66287c0d-23c2-4b24-bd2f-24901ff7e9b0.fifo",
			"7c38af72-7ced-4a21-9155-140031f5fddf.fifo",
			"ab1f64bd-fcf4-476f-b72a-4b1c17e32720.fifo",
			"c4056b28-bf22-433d-af2f-904effda192f.fifo",
			"4ef91acd-56b0-4156-9a54-6a5ed909e065.fifo",
			"408e914d-e282-48aa-8c28-73e23f0e036a.fifo",
			"a91c726a-26bc-40ca-84e6-89e37527eee0.fifo"
	);
	
	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("cloud.aws.sqs.listener.auto-startup", () -> "true");
		registry.add("cloud.aws.region.static", localstackInDockerNetwork.getRegion()::toString);
	    registry.add("cloud.aws.credentials.access-key", localstackInDockerNetwork.getAccessKey()::toString);
	    registry.add("cloud.aws.credentials.secret-key", localstackInDockerNetwork.getSecretKey()::toString);
	    registry.add("cloud.aws.end-point.uri", localstackInDockerNetwork.getEndpointOverride(Service.SQS)::toString);
	    registry.add("cloud.aws.endpoint.uri", localstackInDockerNetwork.getEndpointOverride(Service.SQS)::toString);
	    registry.add("cloud.aws.accountId", () -> "000000000000");
	    
	    registry.add("queue.order.created-event", () -> TEST_QUEUES.get(0));
	    registry.add("queue.order.ordered-event", () -> TEST_QUEUES.get(1));
	    registry.add("queue.bill.performed-event", () -> TEST_QUEUES.get(2));
	    registry.add("queue.invoice-issued-event", () -> TEST_QUEUES.get(3));
	    registry.add("queue.order.scheduled-event", () -> TEST_QUEUES.get(4));
	    registry.add("queue.order.stock-failed-event", () -> TEST_QUEUES.get(5));
	    registry.add("queue.order.perform-billing-failed-event", () -> TEST_QUEUES.get(6));
	    registry.add("queue.issue.invoice-failed-event", () -> TEST_QUEUES.get(7));
	    
	    registry.add("queue.order-stock-cancel-command", () -> TEST_QUEUES.get(8));
	    registry.add("queue.order-stock-command", () -> TEST_QUEUES.get(9));
	    registry.add("queue.invoice-command", () -> TEST_QUEUES.get(10));
	    registry.add("queue.perform-billing-command", () -> TEST_QUEUES.get(11));
	    registry.add("queue.schedule-order-command", () -> TEST_QUEUES.get(12));
	    
	    registry.add("queue.perform-billing-cancel-command", () -> TEST_QUEUES.get(13));
	    registry.add("queue.cancel-order-command", () -> TEST_QUEUES.get(14));
	    registry.add("queue.cancel-billing-command", () -> TEST_QUEUES.get(15));
	    registry.add("queue.cancel-issue-invoice-command", () -> TEST_QUEUES.get(16));
	    registry.add("queue.cancel-order-stock-command", () -> TEST_QUEUES.get(17));
	    registry.add("queue.order-confirmed-command", () -> TEST_QUEUES.get(18));
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
	}

}
