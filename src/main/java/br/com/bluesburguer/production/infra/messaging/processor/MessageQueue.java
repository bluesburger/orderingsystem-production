package br.com.bluesburguer.production.infra.messaging.processor;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class MessageQueue implements Serializable {
	
	private String orderId;
	
	private String newFase;
	
	private String newStep;

	private static final long serialVersionUID = -9155588834028449746L;

	public static HashMap<String, String> fromJsonToMap(String json){
        Type mapType = new TypeToken<HashMap<String, String>>(){}.getType();
        return new Gson().fromJson(json, mapType);
    }

    public static String toJson(HashMap<String, String> mensagemFila){
        return new Gson().toJson(mensagemFila);
    }
}
