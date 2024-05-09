package br.com.bluesburguer.production.adapters.out.order;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;

import br.com.bluesburguer.production.adapters.in.sqs.OrderStatusUpdatedEventConsumer;
import br.com.bluesburguer.production.utils.BaseIntegrationTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class OrderClientIntegrationTests extends BaseIntegrationTest {
	
	@Autowired
	AmazonSQSAsync SQS;
	
	@BeforeEach
	void listQueues() {
		SQS.listQueues().getQueueUrls().forEach(existantQueue -> {
			log.info("Existant queue: {}", existantQueue);
		});;
	}
	
	void setUp() {
		List.of(
				OrderStatusUpdatedEventConsumer.ORDER_PAID_QUEUE,
				OrderStatusUpdatedEventConsumer.ORDER_IN_PRODUCTION_QUEUE,
				OrderStatusUpdatedEventConsumer.ORDER_PRODUCED_QUEUE,
				OrderStatusUpdatedEventConsumer.ORDER_DELIVERING_QUEUE,
				OrderStatusUpdatedEventConsumer.ORDER_DELIVERED_QUEUE,
				OrderStatusUpdatedEventConsumer.ORDER_CANCELED_QUEUE
		).forEach(queueName -> {
			SQS.purgeQueue(new PurgeQueueRequest(queueName));
		});
	}

	@Test
	void loadContext() {
		log.info("Up!!!");
	}
}
