package br.com.bluesburguer.production.infra.database.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

	
	// @Id
	// @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String orderId;
	
	private LocalDateTime creationDateTime;
	
	private String eventName; // FIXME: trocar para enum?
}
