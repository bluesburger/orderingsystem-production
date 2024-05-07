package br.com.bluesburguer.production.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({ "test", "dev" })
public class SqsQueueManager {
	
	public static final String ORDER_PAID_QUEUE = "order-paid-queue.fifo";
	public static final String ORDER_IN_PRODUCTION_QUEUE = "order-in-production-queue.fifo";
	public static final String ORDER_PRODUCED_QUEUE = "order-produced-queue.fifo";
	public static final String ORDER_DELIVERING_QUEUE = "order-delivering-queue.fifo";
	public static final String ORDER_DELIVERED_QUEUE = "order-delivered-queue.fifo";
	public static final String ORDER_CANCELED_QUEUE = "order-canceled-queue.fifo";
	
	private static final String FIFO_SUFIX = ".fifo";

	private final AmazonSQSAsync amazonSQSAsyncClient;
	
	@Value("${aws.sqs.visibility.timeout:60}")
	private String visibilityTimeout;
	
	@Value("${aws.sqs.delay.seconds:0}")
	private String delaySeconds;
	
	@Value("${aws.sqs.message.retention.period:86400}")
	private String messageRetentionPeriod;
	
	@Value("${aws.sqs.message.redrive.policy:5}")
	private Integer redrivePolicy;

	@PostConstruct
	void createFifoQueues() {
		List.of(
				ORDER_PAID_QUEUE,
				ORDER_IN_PRODUCTION_QUEUE,
				ORDER_PRODUCED_QUEUE,
				ORDER_DELIVERING_QUEUE,
				ORDER_DELIVERED_QUEUE,
				ORDER_CANCELED_QUEUE)
		.stream()
		.map(queue -> queue.replace(FIFO_SUFIX, ""))
		.forEach(originalQueueName -> {
			CreateQueueResult createdQueue = createFifoQueue(originalQueueName);
			var deadLetterQueueUrl = createFifoDeadLetterQueue(originalQueueName);
			log.info("Queue created: {} with associated Dead Letter Queue: {}", createdQueue.getQueueUrl(),
					deadLetterQueueUrl);
		});
	}
	
	@PreDestroy
	void deleteFifoQueues() {
		List.of(
				ORDER_PAID_QUEUE,
				ORDER_IN_PRODUCTION_QUEUE,
				ORDER_PRODUCED_QUEUE,
				ORDER_DELIVERING_QUEUE,
				ORDER_DELIVERED_QUEUE,
				ORDER_CANCELED_QUEUE)
		.stream()
		.map(queue -> queue.replace(FIFO_SUFIX, ""))
		.forEach(originalQueueName -> {
			deleteQueue(originalQueueName + FIFO_SUFIX);
			deleteQueue(defineDeadLetterQueueName(originalQueueName));
		});
	}
	
	void deleteQueue(String name) {
		var deleteQueueRequest = new DeleteQueueRequest()
				.withQueueUrl(name);
		amazonSQSAsyncClient.deleteQueue(deleteQueueRequest);
		log.info("Queue deleted: {}", name);
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
		var newQueueName = defineDeadLetterQueueName(baseQueueName);

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
				"{\"maxReceiveCount\":\"" + redrivePolicy + "\", \"deadLetterTargetArn\":\"" + deadLetterQueueARN + "\"}");

		var queueAttributeRequest = new SetQueueAttributesRequest()
				.withQueueUrl(normalizeFifoQueueName(baseQueueName))
				.withAttributes(attributes);

		amazonSQSAsyncClient.setQueueAttributes(queueAttributeRequest);

		return deadLetterQueueUrl;
	}
	
	String defineDeadLetterQueueName(String queueName) {
		return String.format("%s-dead-letter.fifo", queueName);
	}
	
	String normalizeFifoQueueName(String queueName) {
		return queueName.toLowerCase().endsWith(FIFO_SUFIX) 
				? queueName 
				: queueName + FIFO_SUFIX;
	}
}
