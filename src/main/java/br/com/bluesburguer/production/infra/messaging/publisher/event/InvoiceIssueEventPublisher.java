package br.com.bluesburguer.production.infra.messaging.publisher.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.infra.messaging.event.InvoiceIssueEvent;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderEventPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class InvoiceIssueEventPublisher extends OrderEventPublisher<InvoiceIssueEvent> {

	public InvoiceIssueEventPublisher(@Value("${queue.invoice-issued-event:invoice-issued-event.fifo}") String queueName) {
		super(queueName);
	}

}
