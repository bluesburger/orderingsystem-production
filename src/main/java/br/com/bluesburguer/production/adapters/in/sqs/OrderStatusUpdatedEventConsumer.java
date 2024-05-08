package br.com.bluesburguer.production.adapters.in.sqs;

import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderCanceled;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderDelivered;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderDelivering;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderEvent;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderInProduction;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderPaid;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderProduced;
import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;
import br.com.bluesburguer.production.ports.OrderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusUpdatedEventConsumer {
	
	public static final String ORDER_PAID_QUEUE = "order-paid.fifo";
	public static final String ORDER_IN_PRODUCTION_QUEUE = "order-in-production.fifo";
	public static final String ORDER_PRODUCED_QUEUE = "order-produced.fifo";
	public static final String ORDER_DELIVERING_QUEUE = "order-delivering.fifo";
	public static final String ORDER_DELIVERED_QUEUE = "order-delivered.fifo";
	public static final String ORDER_CANCELED_QUEUE = "order-canceled.fifo";
	
	private final OrderPort orderPort;
	
	private final ObjectMapper mapper;

	@SqsListener(value = ORDER_PAID_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderPaid(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue {}: {}: {}", ORDER_PAID_QUEUE, event);
		
		OrderPaid order = mapper.readValue(event, OrderPaid.class);
		if (update(order, Step.KITCHEN, Fase.PENDING)) {
    		ack.acknowledge();
    	}
    }

	@SqsListener(value = ORDER_IN_PRODUCTION_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderInProduction(String event, Acknowledgment ack) throws JsonProcessingException {
    	log.info("Event received on queue {}: {}", ORDER_IN_PRODUCTION_QUEUE, event);
    	
    	OrderInProduction order = mapper.readValue(event, OrderInProduction.class);
    	if (update(order, Step.KITCHEN, Fase.IN_PROGRESS)) {
    		ack.acknowledge();
    	}
    }

	@SqsListener(value = ORDER_PRODUCED_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderProduced(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue {}: {}", ORDER_PRODUCED_QUEUE, event);
		OrderProduced order = mapper.readValue(event, OrderProduced.class);
		if (update(order, Step.DELIVERY, Fase.PENDING)) {
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = ORDER_DELIVERING_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderDelivering(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue {}: {}", ORDER_DELIVERING_QUEUE, event);
		
		OrderDelivering order = mapper.readValue(event, OrderDelivering.class);
		if (update(order, Step.DELIVERY, Fase.IN_PROGRESS)) {
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = ORDER_DELIVERED_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderDelivered(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue {}: {}", ORDER_DELIVERED_QUEUE, event);
		OrderDelivered order = mapper.readValue(event, OrderDelivered.class);
    	if (update(order, Step.DELIVERY, Fase.DONE)) {
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = ORDER_CANCELED_QUEUE, deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderCanceled(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue {}: {}", ORDER_CANCELED_QUEUE, event);
		
		OrderCanceled order = mapper.readValue(event, OrderCanceled.class);
    	if (update(order, order.getStep(), Fase.CANCELED)) {
    		ack.acknowledge();
    	}
    }
	
	private boolean update(OrderEvent order, Step step, Fase fase) {
		if (orderPort.update(order.getOrderId(), step, fase)) {
    		log.info("Order status updated to step {} and fase {}", step, fase);
    		return true;
    	}
		return false;
	}
}
