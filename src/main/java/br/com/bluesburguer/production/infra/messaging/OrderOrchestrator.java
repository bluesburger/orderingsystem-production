package br.com.bluesburguer.production.infra.messaging;

import java.util.HashMap;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.domain.usecase.UpdateOrderUseCase;
import br.com.bluesburguer.production.infra.messaging.processor.MessageQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOrchestrator extends RouteBuilder {

	private final UpdateOrderUseCase updateOrderUseCase;
	
//	private final SqsClient sqsClient;
	
	@Value("${cloud.aws.endpoint.uri}")
	private String sqsUrl;
	
	@Value("${cloud.aws.account-id}")
	private String accountId;
	
	private String defineUri(String queueName) {
		return String.format(new StringBuilder("aws2-sqs://%s?queueUrl=%s/%s/%s?")
				.append("?amazonSQSClient=#myClient")
				.append("&autoCreateQueue=true")
				.append("&useDefaultCredentialsProvider=true")
				.append("&useProfileCredentialsProvider=true")
				.append("&useSessionCredentials=false")
//				.append("&delay=500000")
				.toString(), queueName, sqsUrl, accountId, queueName);
	}
	
	@Override
	public void configure() throws Exception {
//		CamelContext context = new DefaultCamelContext();
//		context.getRegistry().bind("myClient", sqsClient);
		
		from(defineUri("orquestracao_pedidos"))
	        .log(LoggingLevel.WARN, "Novo Pedido Recebido")
	        .bean(MessageQueue.class, "fromJsonToMap(${body})")
	        .convertBodyTo(String.class)
	        .to(defineUri("orquestracao_pagamentos"));
	
	
		from(defineUri("orquestracao_pedidos_saga_reply"))
	        .routeId("Rota orquestracao_pedidos_saga_reply")
	        .log(LoggingLevel.WARN, "${body}")
	        .choice()
	            .when(body().contains("atualizacaoPagamento"))
	               .to(defineUri("orquestracao_entregas"))
	            .when(body().contains("\"statusEntrega\":\"CONFIRMADA\""))
	                    .log(LoggingLevel.WARN, "entrega confirmada")
	                    .bean(OrderOrchestrator.class,"confirmarPedido(${body})")
	                    .log(LoggingLevel.WARN, "Pedido confirmado");
		
		from(defineUri("orquestracao_pagamentos"))
	        .log(LoggingLevel.WARN, "Novo Pagamento Recebido");
	}
	
	public void confirmarPedido(String json) {
        HashMap<String, String> map = MessageQueue.fromJsonToMap(json);
        updateOrderUseCase.update(map.get("orderId"), Step.valueOf(map.get("newStep")), Fase.valueOf(map.get("newFase")));

    }
}
