package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.commands.IssueInvoiceCommand;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class IssueInvoiceCommandPublisher extends OrderCommandPublisher<IssueInvoiceCommand> {

	public IssueInvoiceCommandPublisher(@Value("${queue.invoice-command:queue-invoice-command}") String queueName) {
		super(queueName);
	}
}