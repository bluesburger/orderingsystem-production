package br.com.bluesburguer.production.adapters.in.order.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class OrderItemDto {

	@NotNull
	@NonNull
	private Long id;
	
	@NotNull
	@NonNull
	private Integer quantity;
	
	private Double itemValue;
}
