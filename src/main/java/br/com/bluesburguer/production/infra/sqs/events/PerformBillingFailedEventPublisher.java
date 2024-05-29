package br.com.bluesburguer.production.infra.sqs.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.events.PerformBillingFailedEvent;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class PerformBillingFailedEventPublisher extends OrderEventPublisher<PerformBillingFailedEvent> {

	public PerformBillingFailedEventPublisher(@Value("${queue.order.perform-billing-failed-event:queue-order-perform-billing-failed-event}") String queueName) {
		super(queueName);
	}

}
