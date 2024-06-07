package br.com.bluesburguer.production.infra.messaging.publisher.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.command.PerformBillingCommand;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderCommandPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class PerformBillingCommandPublisher extends OrderCommandPublisher<PerformBillingCommand> {

	public PerformBillingCommandPublisher(
			@Value("${queue.perform-billing-command:queue-perform-billing-command.fifo}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<PerformBillingCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}

}
