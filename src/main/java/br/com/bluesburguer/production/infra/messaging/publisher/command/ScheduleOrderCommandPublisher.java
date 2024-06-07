package br.com.bluesburguer.production.infra.messaging.publisher.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.infra.messaging.SqsQueueSupport;
import br.com.bluesburguer.production.infra.messaging.command.ScheduleOrderCommand;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderCommandPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class ScheduleOrderCommandPublisher extends OrderCommandPublisher<ScheduleOrderCommand> {

	public ScheduleOrderCommandPublisher(
			@Value("${queue.schedule-order-command:queue-schedule-order-command.fifo}") String queueName,
			AmazonSQS amazonSQS,
			SqsQueueSupport<ScheduleOrderCommand> sqsQueueSupport) {
		super(queueName, amazonSQS, sqsQueueSupport);
	}

}
