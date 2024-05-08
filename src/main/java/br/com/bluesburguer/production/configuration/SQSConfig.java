package br.com.bluesburguer.production.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler;
import org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

@Configuration
public class SQSConfig {

	@Value("${cloud.aws.endpoint.uri}")
	private String host;

	@Value("${cloud.aws.credentials.access-key:key}")
	private String accessKeyId;

	@Value("${cloud.aws.credentials.secret-key:value}")
	private String secretAccessKey;

	@Value("${cloud.aws.region.static:us-east-1}")
	private String region;

	// Configurações dos beans para o SNS e SQS
	@Bean
	@Primary
	NotificationMessagingTemplate notificationMessagingTemplate(AmazonSNS amazonSNS) {
		return new NotificationMessagingTemplate(amazonSNS);
	}

	@Bean
	@Primary
	AmazonSQSAsync amazonSQSAsync() {
		return AmazonSQSAsyncClientBuilder.standard().withEndpointConfiguration(getEndpointConfiguration())
				.withCredentials(getCredentialsProvider()).build();
	}

	@Bean
	@Primary
	AmazonSNS amazonSNSAsync() {
		return AmazonSNSAsyncClientBuilder.standard().withEndpointConfiguration(getEndpointConfiguration())
				.withCredentials(getCredentialsProvider()).build();
	}

	@Bean
	@Primary
	QueueMessagingTemplate queueMessagingTemplate() {
		return new QueueMessagingTemplate(amazonSQSAsync());
	}

	/** Esse bean é responsável por ouvir a fila e PRECISA ser definido. */
	@Bean
	QueueMessageHandler queueMessageHandler() {
		var queueMessageHandlerFactory = new QueueMessageHandlerFactory();
		queueMessageHandlerFactory.setAmazonSqs(amazonSQSAsync());
		return queueMessageHandlerFactory.createQueueMessageHandler();
	}

	/** Esse bean é responsável por ouvir a fila e PRECISA ser definido. */
	@Bean
	SimpleMessageListenerContainer simpleMessageListenerContainer() {
		var simpleListenerContainer = new SimpleMessageListenerContainer();
		simpleListenerContainer.setAmazonSqs(amazonSQSAsync());
		simpleListenerContainer.setMessageHandler(queueMessageHandler());
		return simpleListenerContainer;
	}

	private EndpointConfiguration getEndpointConfiguration() {
		return new EndpointConfiguration(host, region);
	}

	public AWSStaticCredentialsProvider getCredentialsProvider() {
		return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey));
	}
}
