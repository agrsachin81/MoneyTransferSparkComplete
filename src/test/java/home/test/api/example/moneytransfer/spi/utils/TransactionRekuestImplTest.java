
package home.test.api.example.moneytransfer.spi.utils;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import home.test.api.example.moneytransfer.spi.interfaces.TransactionRekuest;
import home.test.api.example.moneytransfer.util.GsonHelper;

/**
 * Just to test whether after each deserialization the object must never return null 
 * this should even work after changing JSON deserializer/deserializer
 * Dev must use the same JSON deserializer object as used by the restful api router 
 * @author sachin
 *
 */
public class TransactionRekuestImplTest {

	private static Gson json=null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		json = GsonHelper.createJsonSerializerDeserializer();
	}

	@Test
	public void testJsonSerializationNotNullTransfer() {

		String jsonStr = "{amount:" + 900.0
		+ ",cpAccountId:" + "someAccid" + ",transactionRekuestId:trasnRekId_9 }";

		TransactionRekuestImpl objFromJson = json.fromJson(jsonStr, TransactionRekuestImpl.class);
		
		String missing = findNullObjectProps(objFromJson, TransactionRekuest.class);

		assertEquals("Null props are  " + missing, 0, missing.length());
	}
	
	@Test
	public void testJsonSerializationNotNullCash() {

		String jsonStr = "{amount:" + 900.0
		+ ",transactionRekuestId:trasnRekId_9,transactionType:debit}";

		TransactionRekuestImpl objFromJson = json.fromJson(jsonStr, TransactionRekuestImpl.class);
		
		String missing = findNullObjectProps(objFromJson, TransactionRekuest.class);

		assertEquals("Null props are  " + missing, 0, missing.length());
	}
	
	public static String findNullObjectProps(TransactionRekuestImpl obj, Class<?> class1) {
		String missing= "";
		for (Method method : class1.getMethods()) {
			String name = method.getName();
			String jsonName = null;
			if (name.startsWith("get")) {
				jsonName = name.substring(3);
			} else if (name.startsWith("is")) {
				jsonName = name.substring(2);
			}
			
			Object retObject =null;
			try {
				retObject = method.invoke(obj, null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}

			if (retObject == null) {
					missing += jsonName + ", ";
			}
		}
		return missing;
	}

}
