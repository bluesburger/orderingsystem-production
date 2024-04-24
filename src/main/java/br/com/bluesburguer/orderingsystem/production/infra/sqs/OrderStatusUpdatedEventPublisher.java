package br.com.bluesburguer.orderingsystem.production.infra.sqs;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.orderingsystem.order.domain.Status;
import br.com.bluesburguer.orderingsystem.production.domain.OrderStatusUpdated;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderStatusUpdatedEventPublisher {
    
    @Value("${cloud.aws.queue.uri}")
    private String queueUri;

    @Autowired
    private AmazonSQS amazonSQS;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishEvent(Status status) {
        var event = new OrderStatusUpdated(UUID.randomUUID(), status);
        
        SendMessageRequest sendMessageRequest = null;
        try {
        	var groupId = alphanumericId();
    		var deduplicationId = alphanumericId();
    		
            sendMessageRequest = new SendMessageRequest().withQueueUrl(queueUri + "/000000000000/sample-queue.fifo")
                    .withMessageBody(objectMapper.writeValueAsString(event))
                    .withMessageGroupId(groupId)
                    .withMessageDeduplicationId(deduplicationId);
            amazonSQS.sendMessage(sendMessageRequest);
            log.info("Event has been published in SQS with id {}", event.getId());
        } catch (JsonProcessingException e) {
        	log.error("JsonProcessingException e : {} and stacktrace : {}", e.getMessage(), e);
        } catch (Exception e) {
        	log.error("Exception ocurred while pushing event to sqs : {} and stacktrace ; {}", e.getMessage(), e);
        }
    }
    
    private String alphanumericId() {
		return RandomStringUtils.randomAlphanumeric(10);
	}
}
