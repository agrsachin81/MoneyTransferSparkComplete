
package home.test.api.example.moneytransfer.service.impl.mem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import home.test.api.example.moneytransfer.entities.AccountEntry;
import home.test.api.example.moneytransfer.entities.Transaction;
import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;
import home.test.api.example.moneytransfer.spi.TransactionService;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;
import home.test.api.example.moneytransfer.spi.enums.TransactionType;
import home.test.api.example.moneytransfer.spi.exceptions.AccountException;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionRekuest;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;
import home.test.api.example.moneytransfer.util.TransactionBuilder;
import home.test.api.example.moneytransfer.util.TransactionResultImpl;

public class InMemoryTransactionService implements TransactionService {

	private static final String TO_REVERT_THE_TRANSACTION_MANUALLY_BY_CUSTOMER_CARE = "] to revert the transaction manually by CustomerCare";

	private static final String ERROR_OCCURRED_WHILE_CREDITING_ACCOUNT_PLEASE_USE_DEBITED_ACCOUNT_ENTRY_ID = "Error occurred while crediting account, please use Debited AccountEntry Id[";

	private static final String ERROR_OCCURRED_WHILE_CREDITING_ACCOUNT_DEBIT_SUCCESSFULLY_REVERTED = "Error occurred while crediting account, debit successfully reverted.";

	private static final String THE_DETAILS_DO_NOT_MATCH = "The details do not match ";

	private static final String ACCOUNT_ID_NOT_FOUND = " AccountId not found";

	private static final String TRANSACTION_SUCCESSFULL = " Transaction Successfull.";

	private static final String ACCOUNTID_MUST_HAVE_A_VALID_VALUE = "Accountid must have a valid value";

	private static final String NON_CASH_TRANSACTIONS_MUST_HAVE_A_NON_EMPTY_COUNTERPARTY_I_E_CP_ACCOUNT = "Non cash transactions must have a non-empty Counterparty, i.e. cpAccount ";

	private final AccountServiceInternal service;

	// supposedly in memory
	// We can manage a very large cache using only a persistence mechanism, and caching
	// using LRU cache, not doing it in test project
	Map<String, Transaction> transactions = new HashMap<>();

	public InMemoryTransactionService(AccountServiceInternal service) {
		this.service = service;
	}

	@Override
	public TransactionResult transfer(TransactionRekuest transaction, String originatingAccntId) {
		TransactionBuilder builder = TransactionBuilder.createBuilder(transaction);

		//logging is not done intentionally in the test project
		TransactionType transactionType = transaction.getTransactionType();
				
		TransactionResult validationResult = validateTransactionRekuest(transaction, originatingAccntId, builder);
		
		if(validationResult.getStatus()!= StatusResponse.SUCCESS) {
			return validationResult;
		}
		
		boolean updateCpAccount = false;
		String debitedAccountId = null;
		String creditedAccountId = null;
		boolean isDebit = false;
		boolean isCash = false;
		switch (transactionType) {
		case DEBIT_ACCOUNT:
			debitedAccountId = originatingAccntId;
			creditedAccountId = transaction.getCpAccountId().get();
			updateCpAccount = true;
			isDebit = true;
			break;
		case DEBIT_CASH:
			debitedAccountId = originatingAccntId;
			updateCpAccount = false;
			creditedAccountId = null;
			isDebit = true;
			isCash = true;
			break;
		case CREDIT_ACCOUNT:
			creditedAccountId = originatingAccntId;
			debitedAccountId = transaction.getCpAccountId().get();
			updateCpAccount = true;
			break;
		case CREDIT_CASH:
			creditedAccountId = originatingAccntId;
			updateCpAccount = false;
			debitedAccountId = null;
			isCash = true;
			break;
		default:
			break;
		}
		AccountEntry debitAccountEntry = null;

		if (debitedAccountId != null) {
			try {
				debitAccountEntry = this.service.debitAccount(debitedAccountId, transaction.getAmount(),
						builder.getTransactionReferenceId(), transaction.getTransactionRekuestId(), creditedAccountId);
			} catch (AccountException e) {
				transactions.put(builder.getTransactionReferenceId(), builder.createTransaction(debitedAccountId,
						creditedAccountId, isCash, TransactionStatus.DEBIT_FAILED));
				return builder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.DEBIT_FAILED,
						e.getMessage(), originatingAccntId, true, isCash);
			}
		}

		if (creditedAccountId != null) {
			try {
				this.service.creditAccount(creditedAccountId, transaction.getAmount(),
						builder.getTransactionReferenceId(), transaction.getTransactionRekuestId(), debitedAccountId);
			} catch (AccountException e) {

				if (updateCpAccount) {
					try {
						AccountEntry revertAccountEntry = this.service.creditAccount(debitedAccountId,
								transaction.getAmount(), builder.getTransactionReferenceId(),
								debitAccountEntry.getEntryId() + "_REV", creditedAccountId);

						transactions.put(builder.getTransactionReferenceId(),
								builder.createTransaction(debitedAccountId, creditedAccountId, false,
										TransactionStatus.CREDIT_FAILED_DEBIT_REVERTED));
						return builder.createTransactionResult(StatusResponse.ERROR,
								TransactionStatus.CREDIT_FAILED_DEBIT_REVERTED,
								ERROR_OCCURRED_WHILE_CREDITING_ACCOUNT_DEBIT_SUCCESSFULLY_REVERTED,
								originatingAccntId, false, false);
						// Some error occurred while crediting counterparty account, revert Successfull
					} catch (AccountException ex) {
						// FATAL
						// need to add transaction id and so that client can manually manage the account
						// revert was unsuccessfull, we can even implement a retry mechanism to revert
						// a retry mechanism can be asynchronous
						// or add a service which rechecks the status of the transaction or retries to
						// revert based on the actual interface provide by banks or internal commit service
						
						transactions.put(builder.getTransactionReferenceId(),
								builder.createTransaction(debitedAccountId, creditedAccountId, false,
										TransactionStatus.CREDIT_FAILED_DEBIT_NOT_REVERTED));
						return builder.createTransactionResult(StatusResponse.ERROR,
								TransactionStatus.CREDIT_FAILED_DEBIT_NOT_REVERTED,
								ERROR_OCCURRED_WHILE_CREDITING_ACCOUNT_PLEASE_USE_DEBITED_ACCOUNT_ENTRY_ID
										+ debitAccountEntry.getEntryId()
										+ TO_REVERT_THE_TRANSACTION_MANUALLY_BY_CUSTOMER_CARE,
								originatingAccntId, false, false);
					}
				} else {
					transactions.put(builder.getTransactionReferenceId(), builder.createTransaction(debitedAccountId,
							creditedAccountId, true, TransactionStatus.CREDIT_FAILED));
					return builder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.CREDIT_FAILED,
							e.getMessage(), originatingAccntId, false, true);
				}
				// need to dump the information whether the account can not be debited back with
				// TransactionId
			}
		}

		transactions.put(builder.getTransactionReferenceId(),
				builder.createTransaction(debitedAccountId, creditedAccountId, isCash, TransactionStatus.DONE));
		return builder.createTransactionResult(StatusResponse.SUCCESS, TransactionStatus.DONE,
				TRANSACTION_SUCCESSFULL, originatingAccntId, isDebit, isCash);
	}

	private TransactionResult validateTransactionRekuest(TransactionRekuest transaction, String originatingAccntId,
			TransactionBuilder builder) {
		if(originatingAccntId==null || originatingAccntId.trim().length()==0) {
			return builder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.INVALID_INPUT,
					ACCOUNTID_MUST_HAVE_A_VALID_VALUE, originatingAccntId, true, false);
		}
		
		if(!transaction.isCash() && !transaction.getCpAccountId().isPresent()) {
			return builder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.INVALID_INPUT,
					NON_CASH_TRANSACTIONS_MUST_HAVE_A_NON_EMPTY_COUNTERPARTY_I_E_CP_ACCOUNT, originatingAccntId, true, false);
		}
		return TransactionResultImpl.VALIDATION_SUCCESS;
	}

	@Override
	public Collection<TransactionResult> getTransactions(String accntId, int lastn) {
		TransactionBuilder builder = TransactionBuilder.createBuilder();

		if (!service.accountExist(accntId)) {
			return Collections.singleton(
					builder.createAbortedTransactionResult(StatusResponse.ERROR, accntId, ACCOUNT_ID_NOT_FOUND));
		}

		Collection<AccountEntry> entries = service.getAccountEntries(accntId);
		return builder.createTransactionResult(StatusResponse.SUCCESS, entries, accntId);
	}

	@Override
	public TransactionResult getTransaction(String accountId, String transactionid) {
		TransactionBuilder builder = TransactionBuilder.createBuilder();
		if (transactions.containsKey(transactionid) && service.accountExist(accountId)) {
			return builder.createTransactionResult(accountId, StatusResponse.SUCCESS, transactions.get(transactionid));
		}
		return builder.createAbortedTransactionResult(StatusResponse.ERROR, accountId, THE_DETAILS_DO_NOT_MATCH);
	}
}
