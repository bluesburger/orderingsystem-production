package br.com.bluesburguer.production.infra.sqs.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.events.OrderCreatedEvent;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class OrderCreatedEventPublisher extends OrderEventPublisher<OrderCreatedEvent> {

	public OrderCreatedEventPublisher(@Value("${queue.order.created-event:order-created-event.fifo}") String queueName) {
		super(queueName);
	}

}
