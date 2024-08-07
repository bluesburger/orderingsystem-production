package br.com.bluesburguer.production.domain.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class Status implements Serializable {
	private static final long serialVersionUID = -1837328577857983314L;

	@NonNull
	private Step step;
	
	@NonNull
	private Fase fase;
}
