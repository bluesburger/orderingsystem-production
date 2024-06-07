package br.com.bluesburguer.production.infra.messaging.publisher.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.command.PerformBillingCancelCommand;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderCommandPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class PerformBillingCancelCommandPublisher extends OrderCommandPublisher<PerformBillingCancelCommand> {

	public PerformBillingCancelCommandPublisher(
			@Value("${queue.perform-billing-cancel-command:queue-perform-billing-cancel-command}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<PerformBillingCancelCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}

}
