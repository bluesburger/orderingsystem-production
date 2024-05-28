package br.com.bluesburguer.production.infra.adapters.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.adapters.order.dto.OrderDto;
import br.com.bluesburguer.production.infra.adapters.order.dto.OrderRequest;
import feign.Response;

@FeignClient(name = "bluesburguer-order")
public interface OrderClient {
	
	@PostMapping("/api/order")
	Response createNewOrder(@RequestBody OrderRequest orderRequest);

	@GetMapping("/api/order/{orderId}")
	OrderDto getById(@PathVariable("orderId") String orderId);
	
	@PutMapping("/api/order/{orderId}/{step}/{fase}")
	OrderDto updateStepAndFase(@PathVariable String orderId, @PathVariable Step step, @PathVariable Fase fase);
}
