package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.commands.PerformBillingCommand;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class PerformBillingCommandPublisher extends OrderCommandPublisher<PerformBillingCommand> {

	public PerformBillingCommandPublisher(@Value("${queue.perform-billing-command:queue-perform-billing-command}") String queueName) {
		super(queueName);
	}

}
