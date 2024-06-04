package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.commands.OrderConfirmedCommand;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class OrderConfirmedCommandPublisher extends OrderCommandPublisher<OrderConfirmedCommand> {

	public OrderConfirmedCommandPublisher(
			@Value("${queue.order-confirmed-command:queue-order-confirmed-command.fifo}") String queueName) {
		super(queueName);
	}

}
