package br.com.bluesburguer.orderingsystem.production.infra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsQueueManager implements InitializingBean {
	
	public static final String MESSAGE_DEMO_QUEUE = "SQS_DEMO_QUEUE";
	public static final String ORDER_PAID_QUEUE = "order-paid-queue";
	public static final String ORDER_IN_PRODUCTION_QUEUE = "order-in-production-queue";
	public static final String ORDER_PRODUCED_QUEUE = "order-produced-queue";
	public static final String ORDER_DELIVERING_QUEUE = "order-delivering-queue";
	public static final String ORDER_DELIVERED_QUEUE = "order-delivered-queue";
	public static final String ORDER_CANCELED_QUEUE = "order-canceled-queue";

	private final AmazonSQSAsync amazonSQSAsyncClient;
	
	@Value("${aws.sqs.visibility.timeout:60}")
	private String visibilityTimeout;
	
	@Value("${aws.sqs.delay.seconds:10}")
	private String delaySeconds;
	
	@Value("${aws.sqs.message.retention.period:86400}")
	private String messageRetentionPeriod;

	@Override
	public void afterPropertiesSet() throws Exception {
		createFifoQueues();
	}

	void createFifoQueues() {
		List.of(
				MESSAGE_DEMO_QUEUE,
				ORDER_PAID_QUEUE,
				ORDER_IN_PRODUCTION_QUEUE,
				ORDER_PRODUCED_QUEUE,
				ORDER_DELIVERING_QUEUE,
				ORDER_DELIVERED_QUEUE,
				ORDER_CANCELED_QUEUE)
		.stream()
		.forEach(originalQueueName -> {
			var createdQueue = createFifoQueue(originalQueueName);
			var deadLetterQueueUrl = createFifoDeadLetterQueue(originalQueueName);
			log.info("Queue created: {} with associated Dead Letter Queue: {}", createdQueue.getQueueUrl(),
					deadLetterQueueUrl);
		});
	}

	CreateQueueResult createQueue(String name) {
		var standardQueueRequest = new CreateQueueRequest()
				.withQueueName(name)
				.addAttributesEntry("VisibilityTimeout", visibilityTimeout)
				.addAttributesEntry("DelaySeconds", delaySeconds)
				.addAttributesEntry("MessageRetentionPeriod", messageRetentionPeriod);

		return amazonSQSAsyncClient.createQueue(standardQueueRequest);
	}

	CreateQueueResult createFifoQueue(String newQueueName) throws AmazonSQSException {
		var queueName = normalizeFifoQueueName(newQueueName);
		var fifoQueueRequest = new CreateQueueRequest()
				.withQueueName(queueName)
				.addAttributesEntry("FifoQueue", "true")
				.addAttributesEntry("VisibilityTimeout", visibilityTimeout)
				.addAttributesEntry("DelaySeconds", delaySeconds)
				.addAttributesEntry("MessageRetentionPeriod", messageRetentionPeriod)
				.addAttributesEntry("ContentBasedDeduplication", "true");

		return amazonSQSAsyncClient.createQueue(fifoQueueRequest);
	}

	String createFifoDeadLetterQueue(String baseQueueName) throws AmazonSQSException {
		var newQueueName = String.format("%s-dead-letter.fifo", baseQueueName);

		// Criar queue
		var createDeadLetterQueueRequest = new CreateQueueRequest()
				.addAttributesEntry("FifoQueue", "true")
				.withQueueName(newQueueName);
		var createdQueueResult = amazonSQSAsyncClient.createQueue(createDeadLetterQueueRequest);

		// Obter ARN da queue
		var deadLetterQueueUrl = createdQueueResult.getQueueUrl();
		var attributeRequest = new GetQueueAttributesRequest().withQueueUrl(deadLetterQueueUrl)
				.withAttributeNames(QueueAttributeName.QueueArn);

		var deadLetterQueueAttributes = amazonSQSAsyncClient.getQueueAttributes(attributeRequest);
		var deadLetterQueueARN = deadLetterQueueAttributes.getAttributes().get(QueueAttributeName.QueueArn.name());

		// Definir a fila como DEAD LETTER da fila criada
		Map<String, String> attributes = new HashMap<>();
		attributes.put(QueueAttributeName.RedrivePolicy.name(),
				"{\"maxReceiveCount\":\"5\", \"deadLetterTargetArn\":\"" + deadLetterQueueARN + "\"}");

		var queueAttributeRequest = new SetQueueAttributesRequest()
				.withQueueUrl(normalizeFifoQueueName(baseQueueName))
				.withAttributes(attributes);

		amazonSQSAsyncClient.setQueueAttributes(queueAttributeRequest);

		return deadLetterQueueUrl;
	}
	
	String normalizeFifoQueueName(String queueName) {
		return queueName.toLowerCase().endsWith(".fifo") 
				? queueName 
				: queueName + ".fifo";
	}
}
