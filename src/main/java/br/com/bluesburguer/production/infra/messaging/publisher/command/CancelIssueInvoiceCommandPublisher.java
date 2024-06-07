package br.com.bluesburguer.production.infra.messaging.publisher.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.command.CancelIssueInvoiceCommand;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderCommandPublisher;
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
