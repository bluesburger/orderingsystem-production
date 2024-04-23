package br.com.bluesburguer.orderingsystem.production.services.sqs;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.bluesburguer.orderingsystem.order.Fase;
import br.com.bluesburguer.orderingsystem.order.Status;
import br.com.bluesburguer.orderingsystem.order.Step;
import br.com.bluesburguer.orderingsystem.production.BaseIntegrationTest;

class SQSEventPublisherTest extends BaseIntegrationTest {
	
	@Autowired
	private SQSEventPublisher publisher;

	@Test
	void shouldPublishEvent() throws JsonProcessingException, InterruptedException {
		for (int i = 1; i <= 10; i++) {
			var status = new Status(Step.KITCHEN, Fase.IN_PROGRESS);
			publisher.publishEvent(status);
			TimeUnit.MILLISECONDS.sleep(100L);
		}
	}
}
