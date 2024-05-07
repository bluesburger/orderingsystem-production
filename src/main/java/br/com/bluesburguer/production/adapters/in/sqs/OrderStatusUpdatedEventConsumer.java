package br.com.bluesburguer.production.adapters.in.sqs;

import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderCanceled;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderDelivered;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderDelivering;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderInProduction;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderPaid;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderProduced;
import br.com.bluesburguer.production.configuration.SqsQueueManager;
import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;
import br.com.bluesburguer.production.ports.OrderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusUpdatedEventConsumer {
	
	private final OrderPort orderPort;

	@SqsListener(value = SqsQueueManager.ORDER_PAID_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderPaid event, Acknowledgment ack) {
		log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_PAID_QUEUE, event);
		if (update(event.getOrderId(), Step.KITCHEN, Fase.PENDING)) {
    		ack.acknowledge();
    	}
    }

	@SqsListener(value = SqsQueueManager.ORDER_IN_PRODUCTION_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderInProduction event, Acknowledgment ack) {
    	log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_IN_PRODUCTION_QUEUE, event);
    	if (update(event.getOrderId(), Step.KITCHEN, Fase.IN_PROGRESS)) {
    		ack.acknowledge();
    	}
    }

	@SqsListener(value = SqsQueueManager.ORDER_PRODUCED_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderProduced event, Acknowledgment ack) {
		log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_PRODUCED_QUEUE, event);
		if (update(event.getOrderId(), Step.DELIVERY, Fase.PENDING)) {
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = SqsQueueManager.ORDER_DELIVERING_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderDelivering event, Acknowledgment ack) {
		log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_DELIVERING_QUEUE, event);
		if (update(event.getOrderId(), Step.DELIVERY, Fase.IN_PROGRESS)) {
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = SqsQueueManager.ORDER_DELIVERED_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderDelivered event, Acknowledgment ack) {
		log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_DELIVERED_QUEUE, event);
    	if (update(event.getOrderId(), Step.DELIVERY, Fase.DONE)) {
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = SqsQueueManager.ORDER_CANCELED_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderCanceled event, Acknowledgment ack) {
		log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_CANCELED_QUEUE, event);
    	if (update(event.getOrderId(), event.getStep(), Fase.CANCELED)) {
    		ack.acknowledge();
    	}
    }
	
	private boolean update(long orderId, Step step, Fase fase) {
		if (orderPort.update(orderId, step, fase)) {
    		log.info("Order status updated to step {} and fase {}", step, fase);
    		return true;
    	}
		return false;
	}
}
