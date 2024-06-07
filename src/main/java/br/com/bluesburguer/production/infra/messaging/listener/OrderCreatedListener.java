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
import br.com.bluesburguer.production.infra.messaging.command.OrderStockCommand;
import br.com.bluesburguer.production.infra.messaging.event.OrderCreatedEvent;
import lombok.EqualsAndHashCode;

@Service
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true")
public class OrderCreatedListener extends MessageListener<OrderCreatedEvent> {
	
	private final IOrderCommandPublisher<OrderStockCommand> orderStockCommandPublisher;	

	public OrderCreatedListener(IOrderCommandPublisher<OrderStockCommand> orderStockCommandPublisher,
			UpdateOrderUseCase orderPort, EventDatabaseAdapter eventDatabaseAdapter) {
		super(orderPort, eventDatabaseAdapter);
		this.orderStockCommandPublisher = orderStockCommandPublisher;
	}

	@Override
	@SqsListener(value = "${queue.order.created-event:order-created-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	void handle(@Payload OrderCreatedEvent event, Acknowledgment ack) {
		if (execute(event, Step.ORDER, Fase.CREATED)) {
			var command = new OrderStockCommand(event.getOrderId());
			if (orderStockCommandPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

}
