package br.com.bluesburguer.production.infra.sqs.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.events.OrderOrderedEvent;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class OrderOrderedEventPublisher extends OrderEventPublisher<OrderOrderedEvent> {

	public OrderOrderedEventPublisher(@Value("${queue.order.ordered-event:order-ordered-event}") String queueName) {
		super(queueName);
	}

}
