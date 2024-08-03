package br.com.bluesburguer.production.infra.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.bluesburguer.production.application.dto.PaymentRequest;
import feign.Response;

// @FeignClient(name = "bluesburguer-payment")
public interface PaymentClient {
	
	@PostMapping("/api/v1/payment")
	Response makePayment(@RequestBody PaymentRequest paymentRequest);
}
