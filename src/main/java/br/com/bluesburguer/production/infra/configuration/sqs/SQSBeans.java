package br.com.bluesburguer.production.infra.configuration.sqs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@Configuration
public class SQSBeans {

	@Bean
	AmazonSQS amazonSqs() {
		return AmazonSQSClientBuilder.defaultClient();
	}
}
