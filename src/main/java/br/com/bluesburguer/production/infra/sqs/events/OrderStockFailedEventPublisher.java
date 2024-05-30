package br.com.bluesburguer.production.infra.sqs.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.events.OrderStockFailedEvent;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class OrderStockFailedEventPublisher extends OrderEventPublisher<OrderStockFailedEvent> {

	public OrderStockFailedEventPublisher(@Value("${queue.order.stock-failed-event:order-stock-failed-event}") String queueName) {
		super(queueName);
	}

}