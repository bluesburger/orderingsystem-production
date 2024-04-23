package br.com.bluesburguer.orderingsystem.production.services.sqs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SQSConsumer {
	
	@Autowired 
	private AsyncTaskExecutor messageListenerExecutor;

    @SqsListener(value = "${cloud.aws.queue.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(OrderStatusUpdated orderStatus, Acknowledgment ack) {
    	messageListenerExecutor.submit(() -> {
            try {
            	log.info("SendMessageRequest received : {}", orderStatus.getId());
            	ack.acknowledge(); 
            } catch (Exception e) { 
                log.error("Ocorreu um erro ao tentar processar o evento {}", orderStatus, e);
            }
    	});
    }
}