package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.commands.CancelIssueInvoiceCommand;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class CancelIssueInvoiceCommandPublisher extends OrderCommandPublisher<CancelIssueInvoiceCommand> {

	public CancelIssueInvoiceCommandPublisher(
			@Value("${queue.cancel-issue-invoice-command:queue-cancel-issue-invoice-command.fifo}") String queueName) {
		super(queueName);
	}
}
