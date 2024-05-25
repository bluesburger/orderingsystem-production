package br.com.bluesburguer.production.domain.service;

import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.ports.OrderPort;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.adapters.order.OrderClient;
import br.com.bluesburguer.production.infra.adapters.order.dto.OrderDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderPort {
	
	private final OrderClient orderClient;
	
	@Override
	public OrderDto getById(String orderId) {
		return orderClient.getById(orderId);
	}

	@Override
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
