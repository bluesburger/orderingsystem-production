package br.com.bluesburguer.production.infra.messaging.publisher.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.command.OrderStockCommand;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderCommandPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class OrderStockCommandPublisher extends OrderCommandPublisher<OrderStockCommand> {

	public OrderStockCommandPublisher(
			@Value("${queue.order-stock-command:queue-order-stock-command.fifo}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<OrderStockCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}

}
