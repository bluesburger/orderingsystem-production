package br.com.bluesburguer.orderingsystem.production.interfaces;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import br.com.bluesburguer.orderingsystem.order.domain.Fase;
import br.com.bluesburguer.orderingsystem.order.domain.Step;
import br.com.bluesburguer.orderingsystem.production.interfaces.dto.OrderDto;

@FeignClient(name = "bluesburguer-order")
public interface OrderClient {

	@GetMapping("/api/order/{orderId}")
	OrderDto getById(@PathVariable("orderId") Long orderId);
	
	@PutMapping("/api/order/{orderId}/{step}/{fase}")
	OrderDto updateStepAndFase(@PathVariable Long orderId, @PathVariable Step step, @PathVariable Fase fase);
}
