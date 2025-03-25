package com.account.service;

import static com.account.type.TransactionResultType.F;
import static com.account.type.TransactionResultType.S;
import static com.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.account.domain.Account;
import com.account.domain.AccountUser;
import com.account.domain.Transaction;
import com.account.dto.TransactionDto;
import com.account.exception.AccountException;
import com.account.repository.AccountRepository;
import com.account.repository.AccountUserRepository;
import com.account.repository.TransactionRepository;
import com.account.type.AccountStatus;
import com.account.type.ErrorCode;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
	public static final long USE_AMOUNT = 200L;

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private AccountUserRepository accountUserRepository;

	@InjectMocks
	private TransactionService transactionService;

	@Test
	void successfulUseBalance() {
		// given
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		Account account = Account.builder().accountUser(user).accountStatus(AccountStatus.IN_USE).balance(10000L)
				.accountNumber("1000000012").build();
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user));
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account));
		given(transactionRepository.save(any())).willReturn(Transaction.builder().account(account).transactionType(USE)
				.transactionResultType(S).transactionId("transactionId").transactedAt(LocalDateTime.now()).amount(1000L)
				.balanceSnapshot(9000L).build());

		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

		TransactionDto transactionDto = transactionService.useBalance(1L, "1000000000", USE_AMOUNT);

		// then
		verify(transactionRepository, times(1)).save(captor.capture());
		assertEquals(USE_AMOUNT, captor.getValue().getAmount());
		assertEquals(9800L, captor.getValue().getBalanceSnapshot());
		assertEquals(S, transactionDto.getTransactionResultType());
		assertEquals(USE, transactionDto.getTransactionType());
		assertEquals(9000L, transactionDto.getBalanceSnapshot());
		assertEquals(1000L, transactionDto.getAmount());

	}

	@Test
	@DisplayName("해당 유저 없음 - 잔액 사용 실패")
	void useBalance_UserNotFound() {
		// given
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.empty());
		// when
		AccountException exception = assertThrows(AccountException.class,
				() -> transactionService.useBalance(1L, "1000000000", 1000L));
		// then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("해당 계좌 없음 - 잔액 사용 실패")
	void deleteAccount_AccountNotFound() {
		// given
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user));
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.empty());
		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
		// when
		AccountException exception = assertThrows(AccountException.class,
				() -> transactionService.useBalance(1L, "1000000000", 1000L));
		// then
		assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("계좌 소유주 다름 - 잔액 사용 실패")
	void deleteAccountFailed_userUnmatched() {
		// given
		AccountUser pobi = AccountUser.builder().id(12L).name("Pobi").build();
		AccountUser harry = AccountUser.builder().id(13L).name("Harry").build();
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(pobi));
		given(accountRepository.findByAccountNumber(anyString())).willReturn(
				Optional.of(Account.builder().accountUser(harry).balance(0L).accountNumber("1000000012").build()));

		// when
		AccountException exception = assertThrows(AccountException.class,
				() -> transactionService.useBalance(1L, "1234567890", 1000L));
		// then
		assertEquals(ErrorCode.USER_ACCOUNT_UNMATCHED, exception.getErrorCode());
	}

	@Test
	@DisplayName("해지 계좌는 사용할 수 없다")
	void deleteAccountFailed_alreadyUnregistered() {
		// given
		AccountUser pobi = AccountUser.builder().id(12L).name("Pobi").build();
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(pobi));
		given(accountRepository.findByAccountNumber(anyString()))
				.willReturn(Optional.of(Account.builder().accountUser(pobi).accountStatus(AccountStatus.UNREGISTERED)
						.balance(0L).accountNumber("1000000012").build()));

		// when
		AccountException exception = assertThrows(AccountException.class,
				() -> transactionService.useBalance(1L, "1234567890", 1000L));
		// then
		assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
	}

	@Test
	@DisplayName("거래 금액이 잔액보다 큼")
	void exceedAmount_fulUseBalance() {
		// given
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		Account account = Account.builder().accountUser(user).accountStatus(AccountStatus.IN_USE).balance(100L)
				.accountNumber("1000000012").build();
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user));
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account));

		// when
		// then
		AccountException exception = assertThrows(AccountException.class,
				() -> transactionService.useBalance(1L, "1234567890", 1000L));

		assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
		verify(transactionRepository, times(0)).save(any());

	}

	@Test
	@DisplayName("실패 트랜잭션 저장 성공")
	void saveFailedUseTransaction() {
		// given
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		Account account = Account.builder().accountUser(user).accountStatus(AccountStatus.IN_USE).balance(10000L)
				.accountNumber("1000000012").build();
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account));
		given(transactionRepository.save(any())).willReturn(Transaction.builder().account(account).transactionType(USE)
				.transactionResultType(S).transactionId("transactionId").transactedAt(LocalDateTime.now()).amount(1000L)
				.balanceSnapshot(9000L).build());

		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

		transactionService.saveFailedUseTransaction("1000000000", USE_AMOUNT);

		// then
		verify(transactionRepository, times(1)).save(captor.capture());
		assertEquals(USE_AMOUNT, captor.getValue().getAmount());
		assertEquals(10000L, captor.getValue().getBalanceSnapshot());
		assertEquals(F, captor.getValue().getTransactionResultType());

	}

	@Test
	void successQueryTransaction() throws Exception {
		// given
		given(transactionService.queryTransaction(anyString())).willReturn(TransactionDto.builder()
				.accountNumber("1000000000")
				.transactionType(USE)
				.transactedAt(LocalDateTime.now())
				.amount(54321L)
				.transactionId("transactionIdForCancel")
				.transactionResultType(S)
				.build());
		// when

		// then
	}
}
