package br.com.bluesburguer.production.infra.sqs;

import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.application.dto.OrderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class OrderEventPublisherImpl<T extends OrderEventDto> implements OrderEventPublisher<T> {
	
	private final String queueName;
	
	/*
	@Autowired
	private QueueMessagingTemplate messagingTemplate;
	
	@Override
    public Optional<String> publish(T event) {
		var messageId = UUID.randomUUID().toString();
		
		log.info("Notifying queue {} with id {}", this.queueName, messageId);
	    messagingTemplate.convertAndSend(this.queueName, event, m -> {
	    	m.getHeaders().put("MessageGroupId", messageId);
	    	return m;
	    });
	    return Optional.of("");
	}
	*/
	
	@Value("${cloud.aws.end-point.uri}")
    private String queueHost;
	
	@Value("${cloud.aws.accountId}")
    private String accountId;

	@Autowired
    private AmazonSQS amazonSQS;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Optional<String> publish(T event) {
    	var fullQueueUrl = buildQueueUrl();
    	log.info("Publishing event {} in SQS queue {}", event, fullQueueUrl);
    	
        SendMessageRequest sendMessageRequest = null;
        try {
        	var groupId = alphanumericId();
    		var deduplicationId = alphanumericId();
    		
            sendMessageRequest = new SendMessageRequest()
            		.withQueueUrl(fullQueueUrl)
                    .withMessageBody(objectMapper.writeValueAsString(event))
                    .withMessageGroupId(groupId)
                    .withMessageDeduplicationId(deduplicationId);
            var result = amazonSQS.sendMessage(sendMessageRequest);
            var messageId = result.getMessageId();
            return Optional.ofNullable(messageId);
        } catch (JsonProcessingException e) {
        	log.error("JsonProcessingException e : {} and stacktrace : {}", e.getMessage(), e);
        } catch (Exception e) {
        	log.error("Exception ocurred while pushing event to sqs : {} and stacktrace ; {}", e.getMessage(), e);
        }
        return Optional.empty();
    }
    
    private String buildQueueUrl() {
    	return String.join("/", this.queueHost, this.accountId, this.queueName);
    }
    
    private String alphanumericId() {
		return RandomStringUtils.randomAlphanumeric(10);
	}
}
