package br.com.bluesburguer.production.infra.sqs.strategy;

import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.application.dto.OrderEventDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FifoQueuePublisher<T extends OrderEventDto> extends PublisherStrategy<T> {

	@Autowired
    private ObjectMapper objectMapper;
	
	@Autowired
    private AmazonSQS amazonSQS;
	
	private String queueName;
	
	public FifoQueuePublisher(String queueName) {
		this.queueName = queueName;
	}

	@Override
	public Optional<String> publish(OrderEventDto event) throws JsonProcessingException {
		var fullQueueUrl = buildQueueUrl(queueName);
		var jsonEvent = objectMapper.writeValueAsString(event);
		
		log.info("Publishing event {} in SQS queue {}", jsonEvent, fullQueueUrl);
		SendMessageRequest sendMessageRequest = new SendMessageRequest()
        		.withQueueUrl(buildQueueUrl(queueName))
                .withMessageBody(jsonEvent)
                .withMessageGroupId(RandomStringUtils.randomAlphanumeric(10))
                .withMessageDeduplicationId(RandomStringUtils.randomAlphanumeric(10));
        var result = amazonSQS.sendMessage(sendMessageRequest);
        return Optional.ofNullable(result.getMessageId());
	}
}
