package br.com.bluesburguer.production.infra.sqs.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.events.InvoiceIssueEvent;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class InvoiceIssueEventPublisher extends OrderEventPublisher<InvoiceIssueEvent> {

	public InvoiceIssueEventPublisher(@Value("${queue.invoice-issued-event:invoice-issued-event}") String queueName) {
		super(queueName);
	}

}
