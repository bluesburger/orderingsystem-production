package br.com.bluesburguer.production.application.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest implements Serializable {
	
	private static final long serialVersionUID = -621830335594903665L;
	
	private List<OrderItemRequest> items = new ArrayList<>();

	private UserRequest user;
}
