package br.com.bluesburguer.production.infra.sqs.strategy;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.application.dto.OrderEventDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultQueuePublisher<T extends OrderEventDto> extends PublisherStrategy<T> {
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@Autowired
    private AmazonSQS amazonSQS;
	
	private String queueName;
	
	public DefaultQueuePublisher(String queueName) {
		this.queueName = queueName;
	}

	@Override
	public Optional<String> publish(T event) throws JsonProcessingException {
		var fullQueueUrl = buildQueueUrl(queueName);
		var jsonEvent = objectMapper.writeValueAsString(event);
		
		log.info("Publishing event {} in SQS queue {}", jsonEvent, fullQueueUrl);
		SendMessageRequest sendMessageRequest = new SendMessageRequest()
        		.withQueueUrl(fullQueueUrl)
                .withMessageBody(jsonEvent);
        var result = amazonSQS.sendMessage(sendMessageRequest);
        return Optional.ofNullable(result.getMessageId());
	}
}
