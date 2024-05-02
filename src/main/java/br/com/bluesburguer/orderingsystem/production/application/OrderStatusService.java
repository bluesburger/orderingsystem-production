package br.com.bluesburguer.orderingsystem.production.application;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.orderingsystem.order.domain.Fase;
import br.com.bluesburguer.orderingsystem.order.domain.Step;
import br.com.bluesburguer.orderingsystem.production.interfaces.OrderClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderStatusService {
	
	private final OrderClient orderClient;

	public boolean update(Long orderId, Step newStep, Fase newFase) {
		var updatedOrder = orderClient.updateStepAndFase(orderId, newStep, newFase);
		return ObjectUtils.anyNull(updatedOrder);
	}
}
