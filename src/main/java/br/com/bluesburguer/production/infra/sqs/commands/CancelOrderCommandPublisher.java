package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.application.sqs.commands.CancelOrderCommand;
import br.com.bluesburguer.production.infra.sqs.SqsQueueSupport;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class CancelOrderCommandPublisher extends OrderCommandPublisher<CancelOrderCommand> {

	public CancelOrderCommandPublisher(
			@Value("${queue.cancel-order-command:queue-cancel-order-command.fifo}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<CancelOrderCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}
}
