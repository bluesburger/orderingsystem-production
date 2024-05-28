package br.com.bluesburguer.production.infra.sqs;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
	
	@Autowired
    private AmazonSQS amazonSQS;

    @Autowired
    private ObjectMapper objectMapper;
	
	@Value("${cloud.aws.end-point.uri}")
    private String queueHost;
	
	@Value("${cloud.aws.accountId}")
    private String accountId;

    @Override
    public Optional<String> publish(T event) {
    	
        try {
        	var request = createRequest(event);
        	log.info("Publishing event {} in SQS queue {}", event, request.getQueueUrl());
            var result = amazonSQS.sendMessage(request);
            return Optional.ofNullable(result.getMessageId());
        } catch (JsonProcessingException e) {
        	log.error("JsonProcessingException e : {} and stacktrace : {}", e.getMessage(), e);
        } catch (Exception e) {
        	log.error("Exception ocurred while pushing event to sqs : {} and stacktrace ; {}", e.getMessage(), e);
        }
        return Optional.empty();
    }
    
    private SendMessageRequest createRequest(T event) throws JsonProcessingException {
    	if (Objects.nonNull(queueName) && queueName.toLowerCase().endsWith(".fifo")) {
	    	log.info("Defining fifo request... {}", queueName);
    		return fifoRequest(event);
    	}
    	log.info("Defining default request... {}", queueName);
    	return defaultRequest(event);
    }
    
    private SendMessageRequest defaultRequest(T event) throws JsonProcessingException {
    	return defaultRequest(objectMapper.writeValueAsString(event));
    }
    
    private SendMessageRequest defaultRequest(String messageBody) {
    	return new SendMessageRequest()
        		.withQueueUrl(buildQueueUrl())
                .withMessageBody(messageBody);
    }
    
    private SendMessageRequest fifoRequest(T event) throws JsonProcessingException {
		return defaultRequest(event)
    		.withMessageGroupId(event.getOrderId())
    		.withMessageDeduplicationId(UUID.randomUUID().toString());
    }
    
    private String buildQueueUrl() {
    	return String.join("/", this.queueHost, this.accountId, this.queueName);
    }
}
