package br.com.bluesburguer.production.core.service;

import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.adapters.in.order.dto.OrderDto;
import br.com.bluesburguer.production.adapters.out.order.OrderClient;
import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;
import br.com.bluesburguer.production.ports.OrderPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderPort {
	
	private final OrderClient orderClient;

	public boolean update(String orderId, Step newStep, Fase newFase) {
		var updatedOrder = orderClient.updateStepAndFase(orderId, newStep, newFase);
		return validateUpdateResponse(updatedOrder, newStep, newFase);
	}
	
	private boolean validateUpdateResponse(OrderDto updatedOrder, Step newStep, Fase newFase) {
		return updatedOrder != null 
				&& updatedOrder.getId() != null 
				&& newStep.equals(updatedOrder.getStep())
				&& newFase.equals(updatedOrder.getFase());
	}
}
