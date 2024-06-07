package br.com.bluesburguer.production.infra.messaging.publisher.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.infra.messaging.event.PerformBillingFailedEvent;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderEventPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class PerformBillingFailedEventPublisher extends OrderEventPublisher<PerformBillingFailedEvent> {

	public PerformBillingFailedEventPublisher(@Value("${queue.order.perform-billing-failed-event:perform-billing-failed-event.fifo}") String queueName) {
		super(queueName);
	}

}
