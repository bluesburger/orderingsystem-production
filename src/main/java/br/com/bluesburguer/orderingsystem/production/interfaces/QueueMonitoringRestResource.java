package br.com.bluesburguer.orderingsystem.production.interfaces;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/queue/attributes")
@RequiredArgsConstructor
public class QueueMonitoringRestResource {

	private final AmazonSQSAsync amazonSQSAsyncClient;

	@GetMapping("/{queueUrl}")
	public Map<String, String> getQueueStatus(@PathVariable String queueUrl) {
		var getQueueAttributesRequestForMonitoring = new GetQueueAttributesRequest().withQueueUrl(queueUrl);

		var attributesResponse = amazonSQSAsyncClient.getQueueAttributes(getQueueAttributesRequestForMonitoring);
		return attributesResponse.getAttributes();
	}
}
