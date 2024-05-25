package br.com.bluesburguer.production.application.ports;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.adapters.order.dto.OrderDto;

public interface OrderPort {
	
	OrderDto getById(String orderId);

	boolean update(String orderId, Step newStep, Fase newFase);
}
