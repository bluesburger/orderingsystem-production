package br.com.bluesburguer.orderingsystem.production.infra;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsQueueBean implements InitializingBean {
	
	private final AmazonSQSAsync amazonSQSAsyncClient;

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Queue created: {}", createFifoQueue().getQueueUrl());
		
	}

	public CreateQueueResult createQueue(String name) {
        CreateQueueRequest standardQueueRequest = new CreateQueueRequest()
                .withQueueName("SQS_DEMO_QUEUE")
                .addAttributesEntry("VisibilityTimeout", "60")
                .addAttributesEntry("DelaySeconds", "10")
                .addAttributesEntry("MessageRetentionPeriod", "86400");

        return amazonSQSAsyncClient.createQueue(standardQueueRequest);
    }
    
    public CreateQueueResult createFifoQueue() throws AmazonSQSException {
		CreateQueueRequest fifoQueueRequest = new CreateQueueRequest()
                .withQueueName("SQS_DEMO_QUEUE.fifo")
                .addAttributesEntry("FifoQueue", "true")
                .addAttributesEntry("VisibilityTimeout", "60")
                .addAttributesEntry("DelaySeconds", "10")
                .addAttributesEntry("ContentBasedDeduplication", "true")
                .addAttributesEntry("MessageRetentionPeriod", "86400");
		
		return amazonSQSAsyncClient.createQueue(fifoQueueRequest);
    }
}
