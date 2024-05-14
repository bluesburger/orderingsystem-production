package br.com.bluesburguer.production.adapters.in.sqs.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.bluesburguer.production.core.domain.Step;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCanceled extends OrderEvent {

	private static final long serialVersionUID = 7702500048926979660L;

	@NonNull
	private Step step;
	
	@JsonCreator
	public OrderCanceled(@NonNull @JsonProperty("orderId") String orderId, @NonNull @JsonProperty("step") Step step) {
		super(orderId);
		this.step = step;
	}
}
