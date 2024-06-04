package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.commands.CancelBillingCommand;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class CancelBillingCommandPublisher extends OrderCommandPublisher<CancelBillingCommand> {

	public CancelBillingCommandPublisher(
			@Value("${queue.cancel-billing-command:queue-cancel-billing-command.fifo}") String queueName) {
		super(queueName);
	}
}
