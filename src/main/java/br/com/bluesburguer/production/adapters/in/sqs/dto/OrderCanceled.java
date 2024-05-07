package br.com.bluesburguer.production.adapters.in.sqs.dto;

import br.com.bluesburguer.production.core.domain.Step;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class OrderCanceled extends OrderEvent {

	private static final long serialVersionUID = 7702500048926979660L;

	@NonNull
	private Step step;
}
