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
import br.com.bluesburguer.production.infra.messaging.command.ScheduleOrderCommand;
import br.com.bluesburguer.production.infra.messaging.event.InvoiceIssueEvent;
import lombok.EqualsAndHashCode;

@Service
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true")
public class InvoiceIssueListener extends MessageListener<InvoiceIssueEvent> {
	
	private final IOrderCommandPublisher<ScheduleOrderCommand> scheduleOrderCommandPublisher;
	
	public InvoiceIssueListener(IOrderCommandPublisher<ScheduleOrderCommand> scheduleOrderCommandPublisher,
			UpdateOrderUseCase orderPort, EventDatabaseAdapter eventDatabaseAdapter) {
		super(orderPort, eventDatabaseAdapter);
		this.scheduleOrderCommandPublisher = scheduleOrderCommandPublisher;
	}

	@Override
	@SqsListener(value = "${queue.invoice-issued-event:invoice-issued-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload InvoiceIssueEvent event, Acknowledgment ack) {
		if (execute(event, Step.INVOICE, Fase.CONFIRMED)) {
			var command = new ScheduleOrderCommand(event.getOrderId());
			if (scheduleOrderCommandPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

}
