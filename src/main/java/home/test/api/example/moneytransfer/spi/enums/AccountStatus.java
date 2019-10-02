 
package home.test.api.example.moneytransfer.spi.enums;

import home.test.api.example.moneytransfer.spi.utils.ValuedEnum;
import home.test.api.example.moneytransfer.spi.utils.ValuedEnumDeserializer;

public enum AccountStatus implements ValuedEnum<AccountStatus>{
	
	CREATED,
	ACTIVE,
	SUSPENDED,
	DELETED,
	UNKNOWN;
	
	private static final ValuedEnumDeserializer<AccountStatus> VALUED_ENUM_DESERIALIZER = new ValuedEnumDeserializer<>(AccountStatus.values());

	public static ValuedEnumDeserializer<AccountStatus> getTypeDeserializer(){
		return VALUED_ENUM_DESERIALIZER;
	}

	@Override
	public String getStringValue() {
		return this.name();
	}
}
