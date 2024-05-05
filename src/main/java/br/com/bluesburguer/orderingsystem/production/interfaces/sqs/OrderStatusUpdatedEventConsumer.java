package br.com.bluesburguer.orderingsystem.production.interfaces.sqs;

import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.orderingsystem.order.domain.Fase;
import br.com.bluesburguer.orderingsystem.order.domain.Step;
import br.com.bluesburguer.orderingsystem.production.application.OrderStatusService;
import br.com.bluesburguer.orderingsystem.production.domain.OrderCanceled;
import br.com.bluesburguer.orderingsystem.production.domain.OrderDelivered;
import br.com.bluesburguer.orderingsystem.production.domain.OrderDelivering;
import br.com.bluesburguer.orderingsystem.production.domain.OrderInProduction;
import br.com.bluesburguer.orderingsystem.production.domain.OrderPaid;
import br.com.bluesburguer.orderingsystem.production.domain.OrderProduced;
import br.com.bluesburguer.orderingsystem.production.infra.SqsQueueManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO: tentar juntar todos os processamentos na mesma fila, deve fazer mais sentido sequencial
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusUpdatedEventConsumer {
	
	private final OrderStatusService orderStatusService;

	@SqsListener(value = SqsQueueManager.ORDER_PAID_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderPaid event, Acknowledgment ack) {
		log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_PAID_QUEUE, event);
		var newStep = Step.KITCHEN;
		var newFase = Fase.PENDING;
    	if (orderStatusService.update(event.getOrderId(), newStep, newFase)) {
    		log.info("Order status updated to step {} and fase {}", newStep, newFase);
    		ack.acknowledge();
    	}
    }

	@SqsListener(value = SqsQueueManager.ORDER_IN_PRODUCTION_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderInProduction event, Acknowledgment ack) {
		var newStep = Step.KITCHEN;
		var newFase = Fase.IN_PROGRESS;
    	log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_IN_PRODUCTION_QUEUE, event);
    	if (orderStatusService.update(event.getOrderId(), newStep, newFase)) {
    		log.info("Order status updated to step {} and fase {}", newStep, newFase);
    		ack.acknowledge();
    	}
    }

	@SqsListener(value = SqsQueueManager.ORDER_PRODUCED_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderProduced event, Acknowledgment ack) {
		log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_PRODUCED_QUEUE, event);
		var newStep = Step.DELIVERY;
		var newFase = Fase.PENDING;
    	if (orderStatusService.update(event.getOrderId(), newStep, newFase)) {
    		log.info("Order status updated to step {} and fase {}", newStep, newFase);
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = SqsQueueManager.ORDER_DELIVERING_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderDelivering event, Acknowledgment ack) {
		log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_DELIVERING_QUEUE, event);
		var newStep = Step.DELIVERY;
		var newFase = Fase.IN_PROGRESS;
    	if (orderStatusService.update(event.getOrderId(), newStep, newFase)) {
    		log.info("Order status updated to step {} and fase {}", newStep, newFase);
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = SqsQueueManager.ORDER_DELIVERED_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderDelivered event, Acknowledgment ack) {
		log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_DELIVERED_QUEUE, event);
		var newStep = Step.DELIVERY;
		var newFase = Fase.DONE;
    	if (orderStatusService.update(event.getOrderId(), newStep, newFase)) {
    		log.info("Order status updated to step {} and fase {}", newStep, newFase);
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = SqsQueueManager.ORDER_CANCELED_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderCanceled event, Acknowledgment ack) {
		log.info("Event received on queue {}: {}", SqsQueueManager.ORDER_CANCELED_QUEUE, event);
		var newStep = event.getStep();
		var newFase = Fase.CANCELED;
    	if (orderStatusService.update(event.getOrderId(), newStep, newFase)) {
    		log.info("Order status updated to step {} and fase {}", newStep, newFase);
    		ack.acknowledge();
    	}
    }
}
