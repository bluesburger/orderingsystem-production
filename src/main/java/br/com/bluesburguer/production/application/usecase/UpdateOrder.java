package br.com.bluesburguer.production.application.usecase;

import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.dto.OrderDto;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.domain.usecase.UpdateOrderUseCase;
import br.com.bluesburguer.production.infra.adapter.OrderClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateOrder implements UpdateOrderUseCase {
	
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
