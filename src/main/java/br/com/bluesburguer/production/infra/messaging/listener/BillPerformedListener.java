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
import br.com.bluesburguer.production.infra.messaging.command.IssueInvoiceCommand;
import br.com.bluesburguer.production.infra.messaging.event.BillPerformedEvent;
import lombok.EqualsAndHashCode;

@Service
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true")
public class BillPerformedListener extends MessageListener<BillPerformedEvent> {
	
	private final IOrderCommandPublisher<IssueInvoiceCommand> issueInvoiceCommandPublisher;
	
	protected BillPerformedListener(IOrderCommandPublisher<IssueInvoiceCommand> issueInvoiceCommandPublisher,
			UpdateOrderUseCase orderPort, EventDatabaseAdapter eventDatabaseAdapter) {
		super(orderPort, eventDatabaseAdapter);
		this.issueInvoiceCommandPublisher = issueInvoiceCommandPublisher;
	}

	@Override
	@SqsListener(value = "${queue.bill.performed-event:bill-performed-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	void handle(@Payload BillPerformedEvent event, Acknowledgment ack) {
		if (execute(event, Step.CHARGE, Fase.CONFIRMED)) {
			var command = new IssueInvoiceCommand(event.getOrderId());
			if (issueInvoiceCommandPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

}
