package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.application.sqs.commands.IssueInvoiceCommand;
import br.com.bluesburguer.production.infra.sqs.SqsQueueSupport;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class IssueInvoiceCommandPublisher extends OrderCommandPublisher<IssueInvoiceCommand> {

	public IssueInvoiceCommandPublisher(
			@Value("${queue.invoice-command:queue-invoice-command.fifo}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<IssueInvoiceCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}
}
