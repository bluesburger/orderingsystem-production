package br.com.bluesburguer.orderingsystem.production.interfaces.sqs;

import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderStatusUpdatedEventConsumer {
	
//	private final OrderStatusService orderStatusService;
	
	@SqsListener("SQS_DEMO_QUEUE.fifo")
    public void handle(String message) {
    	log.info("SendMessageRequest received: {}", message);
    }

//    @SqsListener(value = "localstack-queue.fifo", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
//    public void handle(OrderStatusUpdated orderStatus, Acknowledgment ack) {
//    	log.info("SendMessageRequest received ({}): {}", orderStatus.getId(), orderStatus);
//    	ack.acknowledge();
//    }

	// handle OrdePaid (PedidoPago)
//	@SqsListener(value = "localstack-queue-order-paid.fifo", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
//    public void handle(OrderPaid event, Acknowledgment ack) {
//    	log.info("Event received: {}", event.getClass());
//    	if (orderStatusService.update(event.getOrderId(), Step.KITCHEN, Fase.PENDING)) {
//    		ack.acknowledge();
//    	}
//    }

	// handle OrderInProduction (PedidoEmProducao)
//	@SqsListener(value = "localstack-queue-order-in-production.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
//    public void handle(OrderInProduction event) {
//    	log.info("Event received: {}", event.getClass());
//    	if (orderStatusService.update(event.getOrderId(), Step.KITCHEN, Fase.IN_PROGRESS)) {
//    		ack.acknowledge();
//    	}
//    }
	
	// handle OrderProduced (PedidoProduzido)
//	@SqsListener(value = "localstack-queue-order-produced.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
//    public void handle(OrderProduced event) {
//    	log.info("Event received: {}", event.getClass());
//    	if (orderStatusService.update(event.getOrderId(), Step.DELIVERY, Fase.PENDING)) {
//    		ack.acknowledge();
//    	}
//    }
	
	// handle OrderDelivering(PedidoSendoEntregue)
//	@SqsListener(value = "localstack-queue-order-delivering.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
//    public void handle(OrderDelivering event) {
//    	log.info("Event received: {}", event.getClass());
//    	if (orderStatusService.update(event.getOrderId(), Step.DELIVERY, Fase.IN_PROGRESS)) {
//    		ack.acknowledge();
//    	}
//    }
	
	// handle OrdeDelivered (PedidoEntregue)
//	@SqsListener(value = "localstack-queue-order-delivered.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
//    public void handle(OrderDelivered event) {
//    	log.info("Event received: {}", event.getClass());
//    	if (orderStatusService.update(event.getOrderId(), Step.DELIVERY, Fase.DONE)) {
//    		ack.acknowledge();
//    	}
//    }
	
	// handle OrdeCanceled (PedidoCancelado)
//	@SqsListener(value = "localstack-queue-order-canceled.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
//    public void handle(OrderCanceled event) {
//    	log.info("Event received: {}", event.getClass());
//    	if (orderStatusService.update(event.getOrderId(), event.getStep(), Fase.CANCELED)) {
//    		ack.acknowledge();
//    	}
//    }
}
