package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.application.sqs.commands.OrderStockCancelCommand;
import br.com.bluesburguer.production.infra.sqs.SqsQueueSupport;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class OrderStockCanceledCommandPublisher extends OrderCommandPublisher<OrderStockCancelCommand> {

	public OrderStockCanceledCommandPublisher(
			@Value("${queue.order-stock-cancel-command:queue-order-stock-cancel-command.fifo}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<OrderStockCancelCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}

}
