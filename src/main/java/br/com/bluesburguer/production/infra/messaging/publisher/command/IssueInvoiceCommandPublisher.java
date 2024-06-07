package br.com.bluesburguer.production.infra.messaging.publisher.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.command.IssueInvoiceCommand;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderCommandPublisher;
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
