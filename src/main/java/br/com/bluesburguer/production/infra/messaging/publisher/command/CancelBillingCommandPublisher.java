package br.com.bluesburguer.production.infra.messaging.publisher.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.command.CancelBillingCommand;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderCommandPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class CancelBillingCommandPublisher extends OrderCommandPublisher<CancelBillingCommand> {

	public CancelBillingCommandPublisher(
			@Value("${queue.cancel-billing-command:queue-cancel-billing-command.fifo}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<CancelBillingCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}
}
