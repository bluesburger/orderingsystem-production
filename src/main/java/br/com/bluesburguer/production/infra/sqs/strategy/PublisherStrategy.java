package br.com.bluesburguer.production.infra.sqs.strategy;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.bluesburguer.production.application.dto.OrderEventDto;

public abstract class PublisherStrategy<T extends OrderEventDto> {
	
	@Value("${cloud.aws.end-point.uri}")
    private String queueHost;
	
	@Value("${cloud.aws.accountId}")
    private String accountId;
	
	public abstract Optional<String> publish(T event) throws JsonProcessingException;
	
	public static PublisherStrategy<OrderEventDto> determine(String queue) {
		if (Objects.nonNull(queue) && queue.toLowerCase().endsWith(".fifo")) {
			return new FifoQueuePublisher<>(queue);
		}
		return new DefaultQueuePublisher<>(queue);
	}
	
	protected String buildQueueUrl(String queue) {
    	return String.join("/", this.queueHost, this.accountId, queue);
    }
}
