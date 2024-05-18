package br.com.bluesburguer.production.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler;
import org.springframework.cloud.aws.messaging.listener.support.AcknowledgmentHandlerMethodArgumentResolver;
import org.springframework.cloud.aws.messaging.listener.support.VisibilityHandlerMethodArgumentResolver;
import org.springframework.cloud.aws.messaging.support.NotificationSubjectArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.annotation.support.HeaderMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.HeadersMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.PayloadMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configurações dos beans para o SQS
 */
@Configuration
public class SQSConfig {
	
	private static final String ACKNOWLEDGMENT = "Acknowledgment";
    private static final String VISIBILITY = "Visibility";

	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${cloud.aws.endpoint.uri}")
	private String sqsUrl;

	@Value("${cloud.aws.credentials.access-key}")
	private String accessKeyId;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretAccessKey;

	@Bean
	@Primary
	AmazonSQSAsync amazonSQSAsync() {
		return AmazonSQSAsyncClientBuilder
				.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsUrl, region))
				.withCredentials(
						new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey)))
				.build();
	}
	
	/** Provides a deserialization template for incoming SQS messages */
	@Bean
	QueueMessageHandlerFactory queueMessageHandlerFactory(MessageConverter messageConverter) {

	    var factory = new QueueMessageHandlerFactory();
	    factory.setArgumentResolvers(initArgumentResolvers());
	    return factory;
	}
	
//	@Bean
//	SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSqs) {
//	    SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
//	    factory.setWaitTimeOut(5);
//	    factory.setAmazonSqs(amazonSqs);
//	    return factory;
//	}
	
	private List<HandlerMethodArgumentResolver> initArgumentResolvers() {
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();

        messageConverter.setStrictContentTypeMatch(false);
        return List.of(
                new HeaderMethodArgumentResolver(null, null), // NOSONAR
                new HeadersMethodArgumentResolver(),
                new NotificationSubjectArgumentResolver(),
                new AcknowledgmentHandlerMethodArgumentResolver(ACKNOWLEDGMENT),
                new VisibilityHandlerMethodArgumentResolver(VISIBILITY),
                new PayloadMethodArgumentResolver(messageConverter));
    }

	/** Provides a serialization template for outgoing SQS messages */
	@Bean
	@Primary
	QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSQSAsync, MessageConverter messageConverter) {
		return new QueueMessagingTemplate(amazonSQSAsync, (ResourceIdResolver) null, messageConverter);
	}
	
	/** Provides JSON converter for SQS messages */
	@Bean
	protected MessageConverter messageConverter(ObjectMapper objectMapper) {
	  var converter = new MappingJackson2MessageConverter();
	  converter.setObjectMapper(objectMapper);
	  // Serialization support:
	  converter.setSerializedPayloadClass(String.class);
	  // Deserialization support: (suppress "contentType=application/json" header requirement)
	  converter.setStrictContentTypeMatch(false);
	  return converter;
	}
}
