package br.com.bluesburguer.production.infra.messaging.publisher.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.infra.messaging.event.OrderCreatedEvent;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderEventPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class OrderCreatedEventPublisher extends OrderEventPublisher<OrderCreatedEvent> {

	public OrderCreatedEventPublisher(@Value("${queue.order.created-event:order-created-event.fifo}") String queueName) {
		super(queueName);
	}

}
