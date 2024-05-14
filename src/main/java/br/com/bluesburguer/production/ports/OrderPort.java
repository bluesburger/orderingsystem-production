package br.com.bluesburguer.production.ports;

import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;

public interface OrderPort {

	boolean update(String orderId, Step newStep, Fase newFase);
}
