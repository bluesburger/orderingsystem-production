package br.com.bluesburguer.production.infra.sqs.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;

import br.com.bluesburguer.production.application.sqs.commands.ScheduleOrderCommand;
import br.com.bluesburguer.production.infra.sqs.SqsQueueSupport;
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
