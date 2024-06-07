package br.com.bluesburguer.production.infra.messaging.publisher.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.infra.messaging.event.OrderStockFailedEvent;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderEventPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class OrderStockFailedEventPublisher extends OrderEventPublisher<OrderStockFailedEvent> {

	public OrderStockFailedEventPublisher(@Value("${queue.order.stock-failed-event:order-stock-failed-event.fifo}") String queueName) {
		super(queueName);
	}

}
