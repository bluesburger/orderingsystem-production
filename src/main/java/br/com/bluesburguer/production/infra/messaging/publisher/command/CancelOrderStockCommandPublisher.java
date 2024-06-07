package br.com.bluesburguer.production.infra.messaging.publisher.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.command.CancelOrderStockCommand;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderCommandPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class CancelOrderStockCommandPublisher extends OrderCommandPublisher<CancelOrderStockCommand> {

	public CancelOrderStockCommandPublisher(
			@Value("${queue.cancel-order-stock-command:queue-cancel-order-command.fifo}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<CancelOrderStockCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}
}
