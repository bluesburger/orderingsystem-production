package br.com.bluesburguer.orderingsystem.production.domain;

import br.com.bluesburguer.orderingsystem.order.domain.Step;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class OrderCanceled extends OrderEvent {

	private static final long serialVersionUID = 7702500048926979660L;

	@NonNull
	private Step step;
}
