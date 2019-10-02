package home.test.api.example.moneytransfer.service.internal;

import java.util.Collection;

import home.test.api.example.moneytransfer.entities.Account;
import home.test.api.example.moneytransfer.entities.AccountEntry;
import home.test.api.example.moneytransfer.spi.AccountService;
import home.test.api.example.moneytransfer.spi.exceptions.AccountException;

/**
 * This interface was created to hide following interfaces from restful api router completely
 *just an internal slice to be used by TransactionService only 
 * @author sachin
 *
 */
public interface AccountServiceInternal extends AccountService {

	public AccountEntry debitAccount(String accountId, double amount, String transactionReferenceId, String rekuestId, String cpAccountId)
			throws AccountException;

	public AccountEntry creditAccount(String accountId, double amount, String transactionReferenceId, String rekuestId, String cpAccountId)
			throws AccountException;

	Account getAccountInstance(String accountId) ;
	
	public Collection<AccountEntry> getAccountEntries(String accntId);
}
