package home.test.restapi.example.moneytransfer;

import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.DEBIT_FAILED;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.INIT_BALANCE;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.STATUS;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.STATUS_ERROR;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.TRANSFER_TYPE_DEBIT;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.createAccountRetJson;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.createAccountReturnAccountId;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.getAccountStatus;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.transferAndRetJson;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.transferCashAndRetJson;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.verifyAccountDetails;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.verifyTransaction;
import static home.test.restapi.example.moneytransfer.MoneyTransferAPITestHelper.verifyTransactionFailed;
import static home.test.restapi.testtool.TestRekuestHelper.request;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import home.test.restapi.testtool.TestResponse;
import spark.Spark;

/**
 * To test the integration of various clients who will use this API
 * Ideal way to test the same is by using given, when then format in html
 * i wrote the complete infra back in 2014, could not reproduce here as lot of effort needed to build the infra
 * specially as ROBOT script is not really good and as a scrum team we decided to use robot tested written in html backed by python/jython
 * tried RestInstance library, but was not that good, lot of code still needs to be written in ROBOT script
 * 
 * Ideally would like to write some infra that runs a simple html page containing Given When then test cases
 * using java script while just loading it inside browser launched by maven, which simply produces an html report
 * dreaming!!!! huh 
 * @author sachin
 *
 *         Type ITMoneyTransferAPITest, created on 22-Sep-2019 at 7:18:12 pm
 *
 */
public class ITMoneyTransferAPITest {

	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MoneyTransferAPI.main(null);

		//just to make sure the server has been started completely
		//since we know it is a inmemory storage for sake of exercise, in product scenarios after we load the initial data
		//only after we tell the spark to start. in case spark does not provide that we need to build it on our own
		//by maintaining the different status of the microservice, this helps in managing the microservice lifecyle and their interdependencies
		//    Disconnected to database or from some microservices moneytransfer depends on (on startup)
		//    Connected to database or to some microservice like more or more payment gateway , external sms providers
		//    Downloading (in case initial data needs to be fetched from database or microservices this depends
		//    Downloaded
		//    Running, service is now ready to serve the external calls, above is just a design usually i follow in production level microservices
		Thread.sleep(5000);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Spark.stop();
	}

	
	@Test
	public void testAccountCreate() {
		Map<String, Object> json = createAccountRetJson();
		verifyAccountDetails(json);
	}

	@Test
	public void testAccountGet() {
		String accId = createAccountReturnAccountId();
		Map<String, Object> json = getAccountStatus(accId);
		verifyAccountDetails(json);
	}

	@Test
	public void testAccountGetNonExisting() {
		TestResponse response = request("GET", "/account/PA_9876", "");
		assertEquals(200, response.status);
		Map<String, Object> json = response.json();
		assertEquals("Status is not correct", STATUS_ERROR, json.get(STATUS));
	}

	@Ignore
	@Test
	public void testAccountDelete() {
		//not yet implemented
	}

	@Test
	public void testGetAccounts() {
		TestResponse response = request("GET", "/account", "");
		assertEquals(200, response.status);
		Map<String, Object> json = response.json();
		json.forEach((key,value) -> {
			System.out.println("---> KEY "+ key +" VALUE "+ value);
		});
	}

	@Test
	public void testTransactionSuccessfulAccountTransfer() {
		String accountId = createAccountReturnAccountId();
		String accountId2 = createAccountReturnAccountId();
		
		Map<String, Object> json = transferAndRetJson(accountId, 30, accountId2);
		verifyTransaction(accountId, json, accountId2,  30);
	}

	@Test
	public void testTransactionFailedInsufficientFunds() {
		String accountId = createAccountReturnAccountId();
		String accountId2 = createAccountReturnAccountId();
		
		Map<String, Object> json = transferAndRetJson(accountId, INIT_BALANCE+0.1, accountId2);
		verifyTransactionFailed(accountId, json, accountId2,  INIT_BALANCE+0.1, DEBIT_FAILED);
	}
	
	@Test
	public void testTransactionSuccessfulDebitCash() {
		String accountId = createAccountReturnAccountId();
		
		Map<String, Object> json = transferCashAndRetJson(accountId, 30, TRANSFER_TYPE_DEBIT );
		verifyTransaction(accountId, json, null,  30);
	}

	@Ignore
	@Test
	public void testTransactionSuccessfulCreditCash() {
		//not yet implemented
	}
}
