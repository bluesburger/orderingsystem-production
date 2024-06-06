package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.application.sqs.commands.OrderStockCommand;
import br.com.bluesburguer.production.infra.sqs.SqsQueueSupport;
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
