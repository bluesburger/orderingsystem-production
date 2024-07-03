package br.com.bluesburguer.production.domain.usecase;

import br.com.bluesburguer.production.application.dto.OrderDto;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;

public interface UpdateOrderUseCase {
	
	OrderDto getById(String orderId);

	boolean update(String orderId, Step newStep, Fase newFase);
}
