package br.com.bluesburguer.orderingsystem.production.infra.sqs;

import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

import br.com.bluesburguer.orderingsystem.production.application.OrderStatusService;
import br.com.bluesburguer.orderingsystem.production.domain.OrderStatusUpdated;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderStatusUpdatedEventConsumer {
	 
	// private final SimpleAsyncTaskExecutor messageListenerExecutor;
	
	private final OrderStatusService orderStatusService;
	
	public OrderStatusUpdatedEventConsumer(OrderStatusService orderStatusService) {
		this.orderStatusService = orderStatusService;
	}

    @SqsListener(value = "${cloud.aws.queue.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderStatusUpdated orderStatus, Acknowledgment ack) {
    	log.debug("SendMessageRequest received ({}): {}", orderStatus.getId(), orderStatus);
    	if (orderStatusService.update(orderStatus)) {
    		ack.acknowledge();
    	}
    }
    
    /*
    public void handleDeprecated(OrderStatusUpdated orderStatus, Acknowledgment ack) {
    	messageListenerExecutor.submit(() -> {
            try {
            	log.debug("SendMessageRequest received ({}): {}", orderStatus.getId(), orderStatus);
            	if (orderStatusService.update(orderStatus)) {
            		ack.acknowledge();
            	}
            } catch (Exception e) { 
                log.error("Ocorreu um erro ao tentar processar o evento {}", orderStatus, e);
            }
    	});
    }
    */
}