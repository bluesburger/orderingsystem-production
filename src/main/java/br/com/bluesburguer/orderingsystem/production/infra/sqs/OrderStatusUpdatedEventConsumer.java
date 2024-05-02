package br.com.bluesburguer.orderingsystem.production.infra.sqs;

import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

import br.com.bluesburguer.orderingsystem.order.domain.Fase;
import br.com.bluesburguer.orderingsystem.order.domain.Step;
import br.com.bluesburguer.orderingsystem.production.application.OrderStatusService;
import br.com.bluesburguer.orderingsystem.production.domain.OrderCanceled;
import br.com.bluesburguer.orderingsystem.production.domain.OrderDelivered;
import br.com.bluesburguer.orderingsystem.production.domain.OrderDelivering;
import br.com.bluesburguer.orderingsystem.production.domain.OrderInProduction;
import br.com.bluesburguer.orderingsystem.production.domain.OrderPaid;
import br.com.bluesburguer.orderingsystem.production.domain.OrderProduced;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderStatusUpdatedEventConsumer {
	 
	// private final SimpleAsyncTaskExecutor messageListenerExecutor;
	
	private final OrderStatusService orderStatusService;
	
	public OrderStatusUpdatedEventConsumer(OrderStatusService orderStatusService) {
		this.orderStatusService = orderStatusService;
	}

	/*
    @SqsListener(value = "${cloud.aws.queue.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderStatusUpdated orderStatus, Acknowledgment ack) {
    	log.info("SendMessageRequest received ({}): {}", orderStatus.getId(), orderStatus);
    	if (orderStatusService.update(orderStatus)) {
    		ack.acknowledge();
    	}
    }
    */
	
	// handle OrdePaid (PedidoPago)
	@SqsListener(value = "${cloud.aws.queue.order.paid.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderPaid event, Acknowledgment ack) {
    	log.info("Event received: {}", event.getClass());
    	if (orderStatusService.update(event.getOrderId(), Step.KITCHEN, Fase.PENDING)) {
    		ack.acknowledge();
    	}
    }
	
	// handle OrderInProduction (PedidoEmProducao)
	@SqsListener(value = "${cloud.aws.queue.order.in_production.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderInProduction event, Acknowledgment ack) {
    	log.info("Event received: {}", event.getClass());
    	if (orderStatusService.update(event.getOrderId(), Step.KITCHEN, Fase.IN_PROGRESS)) {
    		ack.acknowledge();
    	}
    }
	
	// handle OrderProduced (PedidoProduzido)
	@SqsListener(value = "${cloud.aws.queue.order.produced.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderProduced event, Acknowledgment ack) {
    	log.info("Event received: {}", event.getClass());
    	if (orderStatusService.update(event.getOrderId(), Step.DELIVERY, Fase.PENDING)) {
    		ack.acknowledge();
    	}
    }
	
	// handle OrderDelivering(PedidoSendoEntregue)
	@SqsListener(value = "${cloud.aws.queue.order.delivering.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderDelivering event, Acknowledgment ack) {
    	log.info("Event received: {}", event.getClass());
    	if (orderStatusService.update(event.getOrderId(), Step.DELIVERY, Fase.IN_PROGRESS)) {
    		ack.acknowledge();
    	}
    }
	
	// handle OrdeDelivered (PedidoEntregue)
	@SqsListener(value = "${cloud.aws.queue.order.delivered.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderDelivered event, Acknowledgment ack) {
    	log.info("Event received: {}", event.getClass());
    	if (orderStatusService.update(event.getOrderId(), Step.DELIVERY, Fase.DONE)) {
    		ack.acknowledge();
    	}
    }
	
	// handle OrdeCanceled (PedidoCancelado)
	@SqsListener(value = "${cloud.aws.queue.order.canceled.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderCanceled event, Acknowledgment ack) {
    	log.info("Event received: {}", event.getClass());
    	if (orderStatusService.update(event.getOrderId(), event.getStep(), Fase.CANCELED)) {
    		ack.acknowledge();
    	}
    }
}