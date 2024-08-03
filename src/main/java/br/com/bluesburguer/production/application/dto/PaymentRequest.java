package br.com.bluesburguer.production.application.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest implements Serializable {
	
	private static final long serialVersionUID = -621830335594903665L;
	
	private Data data;
	
	
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Data {
		
		@JsonProperty("order_id")
		private String orderId;
		
		@JsonProperty("order_status")
		private String orderStatus;
		
		@JsonProperty("order_created_time")
		private String orderCreatedTime;
		
		@JsonProperty("user_id")
		private String userId;
		
		@JsonProperty("payment_method")
		private String paymentMethod;
	}

}
