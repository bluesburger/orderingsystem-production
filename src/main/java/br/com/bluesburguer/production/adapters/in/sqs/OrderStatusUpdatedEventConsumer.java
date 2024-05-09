package br.com.bluesburguer.production.adapters.in.sqs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(
        name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true"
)
public class OrderStatusUpdatedEventConsumer {
	
	private final OrderPort orderPort;
	
	private final ObjectMapper mapper;

	@SqsListener(value = "${queue.order.paid:order-paid.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderPaid(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue order-paid: {}", event);
		
		OrderPaid order = mapper.readValue(event, OrderPaid.class);
		if (update(order, Step.KITCHEN, Fase.PENDING)) {
    		ack.acknowledge();
    	}
    }

	@SqsListener(value = "${queue.order.in-production:order-in-production.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderInProduction(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue order-in-production: {}", event);
    	
    	OrderInProduction order = mapper.readValue(event, OrderInProduction.class);
    	if (update(order, Step.KITCHEN, Fase.IN_PROGRESS)) {
    		ack.acknowledge();
    	}
    }

	@SqsListener(value = "${queue.order.produced:order-produced.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderProduced(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue order-produced: {}", event);
		OrderProduced order = mapper.readValue(event, OrderProduced.class);
		if (update(order, Step.DELIVERY, Fase.PENDING)) {
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = "${queue.order.delivering:order-delivering.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderDelivering(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue order-delivering: {}", event);
		
		OrderDelivering order = mapper.readValue(event, OrderDelivering.class);
		if (update(order, Step.DELIVERY, Fase.IN_PROGRESS)) {
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = "${queue.order.delivered:order-delivered.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderDelivered(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue order-delivered: {}", event);
		OrderDelivered order = mapper.readValue(event, OrderDelivered.class);
    	if (update(order, Step.DELIVERY, Fase.DONE)) {
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = "${queue.order.canceled:order-canceled.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handleOrderCanceled(String event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue order-canceled: {}", event);
		
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
