package br.com.bluesburguer.production.infra.messaging.publisher.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.command.OrderConfirmedCommand;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderCommandPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class OrderConfirmedCommandPublisher extends OrderCommandPublisher<OrderConfirmedCommand> {

	public OrderConfirmedCommandPublisher(
			@Value("${queue.order-confirmed-command:queue-order-confirmed-command.fifo}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<OrderConfirmedCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}

}
