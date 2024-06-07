package br.com.bluesburguer.production.infra.messaging.publisher.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.command.CancelOrderCommand;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderCommandPublisher;
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
