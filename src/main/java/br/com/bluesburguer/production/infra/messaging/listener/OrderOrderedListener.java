package br.com.bluesburguer.production.infra.messaging.listener;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.domain.repository.IOrderCommandPublisher;
import br.com.bluesburguer.production.domain.usecase.UpdateOrderUseCase;
import br.com.bluesburguer.production.infra.database.EventDatabaseAdapter;
import br.com.bluesburguer.production.infra.messaging.command.PerformBillingCommand;
import br.com.bluesburguer.production.infra.messaging.event.OrderOrderedEvent;
import lombok.EqualsAndHashCode;

@Service
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true")
public class OrderOrderedListener extends MessageListener<OrderOrderedEvent> {
	
	private final IOrderCommandPublisher<PerformBillingCommand> performBillingCommandPublisher;
	
	public OrderOrderedListener(IOrderCommandPublisher<PerformBillingCommand> performBillingCommandPublisher,
			UpdateOrderUseCase orderPort, EventDatabaseAdapter eventDatabaseAdapter) {
		super(orderPort, eventDatabaseAdapter);
		this.performBillingCommandPublisher = performBillingCommandPublisher;
	}

	@Override
	@SqsListener(value = "${queue.order.ordered-event:order-ordered-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	void handle(@Payload OrderOrderedEvent event, Acknowledgment ack) {
		if (execute(event, Step.DELIVERY, Fase.CREATED)) {
			var command = new PerformBillingCommand(event.getOrderId());
			if (performBillingCommandPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

}
