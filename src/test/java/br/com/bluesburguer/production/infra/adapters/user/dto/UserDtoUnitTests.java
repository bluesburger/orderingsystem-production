package br.com.bluesburguer.production.infra.adapters.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class UserDtoUnitTests {
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	void shouldConstructFromJsonWith_Id() throws JsonProcessingException {
		String json = "{\"id\": 1}";
		UserDto dto = mapper.readValue(json, UserDto.class);
		assertThat(dto).isNotNull()
			.hasFieldOrPropertyWithValue("id", 1L)
			.hasToString("UserDto(id=1, cpf=null, email=null)");
	}

	@Test
	void shouldConstructFromJsonWith_Cpf() throws JsonProcessingException {
		String json = "{\"cpf\": \"00000000000\"}";
		UserDto dto = mapper.readValue(json, UserDto.class);
		assertThat(dto).isNotNull()
			.hasFieldOrPropertyWithValue("cpf", "00000000000")
			.hasToString("UserDto(id=null, cpf=00000000000, email=null)");
	}
	
	@Test
	void shouldConstructFromJsonWith_Email() throws JsonProcessingException {
		String json = "{\"email\": \"email@server.com\"}";
		UserDto dto = mapper.readValue(json, UserDto.class);
		assertThat(dto).isNotNull()
			.hasFieldOrPropertyWithValue("email", "email@server.com")
			.hasToString("UserDto(id=null, cpf=null, email=email@server.com)");
	}
	
	@Test
	void shouldConstructFromJsonWith_CpfAndEmail() throws JsonProcessingException {
		String json = "{\"cpf\": \"00000000000\", \"email\": \"email@server.com\"}";
		UserDto dto = mapper.readValue(json, UserDto.class);
		assertThat(dto).isNotNull()
			.hasFieldOrPropertyWithValue("cpf", "00000000000")
			.hasFieldOrPropertyWithValue("email", "email@server.com")
			.hasToString("UserDto(id=null, cpf=00000000000, email=email@server.com)");
	}
	
	@Test
	void shouldConstructFromJsonWith_IdAndCpfAndEmail() throws JsonProcessingException {
		String json = "{\"id\": 1, \"cpf\": \"00000000000\", \"email\": \"email@server.com\"}";
		UserDto dto = mapper.readValue(json, UserDto.class);
		assertThat(dto).isNotNull()
			.hasFieldOrPropertyWithValue("id", 1L)
			.hasFieldOrPropertyWithValue("cpf", "00000000000")
			.hasFieldOrPropertyWithValue("email", "email@server.com")
			.hasToString("UserDto(id=1, cpf=00000000000, email=email@server.com)");
	}
}
