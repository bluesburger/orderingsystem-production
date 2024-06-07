package br.com.bluesburguer.production.infra.messaging.publisher;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.bluesburguer.production.infra.messaging.OrderEvent;
import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.publisher.event.IOrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class OrderEventPublisher<T extends OrderEvent> implements IOrderEventPublisher<T> {
	
	private final String queueName;
	
	@Autowired
    private AmazonSQS amazonSQS;
	
	@Autowired
	private SqsQueueSupport<T> sqsQueueSupport;

    @Override
    public Optional<String> publish(T event) {
    	
        try {
        	var request = sqsQueueSupport.createRequest(event, queueName, event.getOrderId());
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
}
