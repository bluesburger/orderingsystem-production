package br.com.bluesburguer.production.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderEvent {

	private Long id;
	private String orderId;
}
