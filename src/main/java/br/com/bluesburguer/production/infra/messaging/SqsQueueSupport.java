package br.com.bluesburguer.production.infra.messaging;

import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SqsQueueSupport<T> {
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@Value("${cloud.aws.end-point.uri}")
    private String queueHost;
	
	@Value("${cloud.aws.accountId}")
    private String accountId;

	public SendMessageRequest createRequest(T event, String queueName, String messageGroupId) throws JsonProcessingException {
    	if (Objects.nonNull(queueName) && queueName.toLowerCase().endsWith(".fifo")) {
	    	log.debug("Defining fifo request... {}", queueName);
    		return fifoRequest(event, queueName, messageGroupId);
    	}
    	log.debug("Defining default request... {}", queueName);
    	return defaultRequest(event, queueName);
    }
	
	public String buildQueueUrl(String queueName) {
    	return String.join("/", this.queueHost, this.accountId, queueName);
    }
    
    private SendMessageRequest defaultRequest(T event, String queueName) throws JsonProcessingException {
    	return defaultRequest(objectMapper.writeValueAsString(event), queueName);
    }
    
    private SendMessageRequest defaultRequest(String messageBody, String queueName) {
    	return new SendMessageRequest()
        		.withQueueUrl(buildQueueUrl(queueName))
                .withMessageBody(messageBody);
    }
    
    private SendMessageRequest fifoRequest(T event, String queueName, String messageGroupId) throws JsonProcessingException {
		return defaultRequest(event, queueName)
    		.withMessageGroupId(messageGroupId)
    		.withMessageDeduplicationId(UUID.randomUUID().toString());
    }
}
