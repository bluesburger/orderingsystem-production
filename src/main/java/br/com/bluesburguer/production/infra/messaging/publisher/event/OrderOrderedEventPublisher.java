package br.com.bluesburguer.production.infra.messaging.publisher.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.infra.messaging.event.OrderOrderedEvent;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderEventPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class OrderOrderedEventPublisher extends OrderEventPublisher<OrderOrderedEvent> {

	public OrderOrderedEventPublisher(@Value("${queue.order.ordered-event:order-ordered-event.fifo}") String queueName) {
		super(queueName);
	}

}
