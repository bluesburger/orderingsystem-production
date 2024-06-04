package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.commands.CancelOrderStockCommand;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class CancelOrderStockCommandPublisher extends OrderCommandPublisher<CancelOrderStockCommand> {

	public CancelOrderStockCommandPublisher(
			@Value("${queue.cancel-order-stock-command:queue-cancel-order-command.fifo}") String queueName) {
		super(queueName);
	}
}
