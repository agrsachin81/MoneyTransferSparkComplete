
package home.test.api.example.moneytransfer.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import home.test.api.example.moneytransfer.mock.MockAccountServiceInternal;
import home.test.api.example.moneytransfer.service.impl.MoneyTransferInMemoryServiceFactory;
import home.test.api.example.moneytransfer.service.impl.mem.AccountServiceFactory;
import home.test.api.example.moneytransfer.service.impl.mem.TransactionServiceFactory;
import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;

/**
 * Test by mocking the internal dependencies.
 * @author sachin
 *
 */
public class MoneyTransferAbstractServiceFactoryTest {

	MoneyTransferAbstractServiceFactory factory;

	@Before
	public void setUp() throws Exception {
		// setup abstract factory by providing mock factories to create AccountService
		// and TransactionService
		// then test whether onlu one object is created by
		// MoneyTransferInMemoryServiceFactory

		factory = new MoneyTransferInMemoryServiceFactory(new AccountServiceFactory() {

			@Override
			public AccountServiceInternal createAccountServiceInternal() {
				return new MockAccountServiceInternal();
			}
		}, new TransactionServiceFactory() {

			@Override
			public TransactionService createTransactionService(AccountServiceInternal accountService) {
				TransactionService tranService = Mockito.mock(TransactionService.class);
				return tranService;
			}
		});
	}

	@Test
	public void testGetAccountService() {
		AccountService accService = factory.getAccountService();

		assertNotNull("must return an object", accService);
		assertNotNull("must return an object on repeated invocation", factory.getAccountService());

		assertEquals("Must always return the same object", accService, factory.getAccountService());
	}

	@Test
	public void testGetTransactionService() {
		TransactionService transService = factory.getTransactionService();

		assertNotNull("must return an object", transService);
		assertNotNull("must return an object on repeated invocation", factory.getTransactionService());

		assertEquals("Must always return the same object", transService, factory.getTransactionService());
	}

}
