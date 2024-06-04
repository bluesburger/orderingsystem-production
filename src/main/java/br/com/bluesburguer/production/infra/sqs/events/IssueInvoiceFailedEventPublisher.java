package br.com.bluesburguer.production.infra.sqs.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.events.IssueInvoiceFailedEvent;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class IssueInvoiceFailedEventPublisher extends OrderEventPublisher<IssueInvoiceFailedEvent> {

	public IssueInvoiceFailedEventPublisher(@Value("${queue.issue.invoice-failed-event:queue-order-issue-invoice-failed-event.fifo}") String queueName) {
		super(queueName);
	}

}
