package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.application.sqs.commands.CancelIssueInvoiceCommand;
import br.com.bluesburguer.production.infra.sqs.SqsQueueSupport;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class CancelIssueInvoiceCommandPublisher extends OrderCommandPublisher<CancelIssueInvoiceCommand> {

	public CancelIssueInvoiceCommandPublisher(
			@Value("${queue.cancel-issue-invoice-command:queue-cancel-issue-invoice-command.fifo}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<CancelIssueInvoiceCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}
}
