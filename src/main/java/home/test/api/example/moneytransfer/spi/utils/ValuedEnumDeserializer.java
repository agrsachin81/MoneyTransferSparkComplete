
package home.test.api.example.moneytransfer.spi.utils;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public final class  ValuedEnumDeserializer <T extends ValuedEnum<T>> implements JsonDeserializer<T> {
	
	private T[] allValues;

	/*
	 * Mandatory to pass all available values of the enum, tried to write an CRTP based automatic 
	 * but seems not able to do it
	 */
	public ValuedEnumDeserializer(T[] allValues) {
		this.allValues= allValues;
	}
	
	@Override
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		for (T value : allValues) {
			if (value.getStringValue().equals(json.getAsString()))
				return value;
		}
		return null;
	}
	
}
