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
import br.com.bluesburguer.production.infra.messaging.command.OrderConfirmedCommand;
import br.com.bluesburguer.production.infra.messaging.event.OrderScheduledEvent;
import lombok.EqualsAndHashCode;

@Service
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true")
public class OrderScheduledListener extends MessageListener<OrderScheduledEvent> {
	
	private final IOrderCommandPublisher<OrderConfirmedCommand> orderConfirmedCommandPublisher;
	
	public OrderScheduledListener(IOrderCommandPublisher<OrderConfirmedCommand> orderConfirmedCommandPublisher,
			UpdateOrderUseCase orderPort, EventDatabaseAdapter eventDatabaseAdapter) {
		super(orderPort, eventDatabaseAdapter);
		this.orderConfirmedCommandPublisher = orderConfirmedCommandPublisher;
	}

	@Override
	@SqsListener(value = "${queue.order.scheduled-event:order-scheduled-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload OrderScheduledEvent event, Acknowledgment ack) {
		if (execute(event, Step.DELIVERY, Fase.CONFIRMED)) {
			var command = new OrderConfirmedCommand(event.getOrderId());
			if (orderConfirmedCommandPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

}
