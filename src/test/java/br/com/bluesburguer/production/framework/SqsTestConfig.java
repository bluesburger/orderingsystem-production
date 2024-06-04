package br.com.bluesburguer.production.framework;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer;
import org.springframework.cloud.aws.messaging.support.destination.DynamicQueueUrlDestinationResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.core.CachingDestinationResolverProxy;
import org.springframework.messaging.core.DestinationResolver;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@TestConfiguration
public class SqsTestConfig {
	
	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${cloud.aws.endpoint.uri}")
	private String sqsUrl;

	@Value("${cloud.aws.credentials.access-key}")
	private String accessKeyId;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretAccessKey;
	
	@Bean
	AmazonSQS amazonSQS() {
		return AmazonSQSClientBuilder
				.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsUrl, region))
				.withCredentials(
						new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey)))
				.build();
	}

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

    /**
     * Configures the SimpleMessageListenerContainer to auto-create a SQS Queue in case it does not exist.
     *
     * This is necessary because if the queue does not exist during startup the SimpleMessageListenerContainer
     * stops working with the following warning message:
     *
     *  > WARN [main] i.a.c.m.l.SimpleMessageListenerContainer:
     *  >    Ignoring queue with name 'customersCreatedQueue': The queue does not exist.;
     *  >    nested exception is com.amazonaws.services.sqs.model.QueueDoesNotExistException: The specified queue
     *  >    does not exist for this wsdl version.
     */
    @Bean
    BeanPostProcessor simpleMessageListenerContainerPostProcessor(DestinationResolver<String> destinationResolver) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof SimpleMessageListenerContainer container) {
                    container.setDestinationResolver(destinationResolver);
                    container.setWaitTimeOut(5); // evict Timeout Error
                }
                return bean;
            }
        };
    }

    /**
     * Creates a DynamicQueueUrlDestinationResolver capable of auto-creating
     * a SQS queue in case it does not exist
     */
    @Bean
    DestinationResolver<String> autoCreateQueueDestinationResolver(
            AmazonSQSAsync sqs,
            @Autowired(required = false) ResourceIdResolver resourceIdResolver) {

        DynamicQueueUrlDestinationResolver autoCreateQueueResolver
                = new DynamicQueueUrlDestinationResolver(sqs, resourceIdResolver);
        autoCreateQueueResolver.setAutoCreate(true);

        return new CachingDestinationResolverProxy<>(autoCreateQueueResolver);
    }

}
