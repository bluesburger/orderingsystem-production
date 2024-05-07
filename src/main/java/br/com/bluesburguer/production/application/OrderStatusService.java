package br.com.bluesburguer.production.application;

import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.interfaces.OrderClient;
import br.com.bluesburguer.production.order.domain.Fase;
import br.com.bluesburguer.production.order.domain.Step;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderStatusService {
	
	private final OrderClient orderClient;

	public boolean update(Long orderId, Step newStep, Fase newFase) {
		var updatedOrder = orderClient.updateStepAndFase(orderId, newStep, newFase);
		return updatedOrder != null 
				&& updatedOrder.getId() != null 
				&& newStep.equals(updatedOrder.getStep())
				&& newFase.equals(updatedOrder.getFase());
	}
}
