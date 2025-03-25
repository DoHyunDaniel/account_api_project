package com.account.service;

import static com.account.type.TransactionResultType.F;
import static com.account.type.TransactionResultType.S;
import static com.account.type.TransactionType.USE;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

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
import com.account.type.TransactionResultType;
import com.account.type.TransactionType;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final AccountUserRepository accountUserRepository;
	private final AccountRepository accountRepository;
	/*
	 * 사용자 없는 경우, 사용자 아이디와 계좌 소유주가 다른 경우, 계좌가 이미 해지 상태인 경우, 거래금액이 잔액보다 큰 경우, 거래금액이
	 * 너무 작거나 큰 경우 실패 응답
	 */

	@Transactional
	public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {

		AccountUser user = accountUserRepository.findById(userId)
				.orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

		validateUseBalance(user, account, amount);

		account.useBalance(amount);

		return TransactionDto.fromEntity(saveAndGetTransaction(USE, S, account, amount));
	}

	private void validateUseBalance(AccountUser user, Account account, Long amount) {
		if (user.getId() != account.getAccountUser().getId()) {
			throw new AccountException(ErrorCode.USER_ACCOUNT_UNMATCHED);
		}
		if (account.getAccountStatus() != AccountStatus.IN_USE) {
			throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
		}
		if (account.getBalance() < amount) {
			throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);

		}
	}

	@Transactional
	public void saveFailedUseTransaction(String accountNumber, Long amount) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

		saveAndGetTransaction(USE, F, account, amount);
	}

	private Transaction saveAndGetTransaction(TransactionType transactionType,
			TransactionResultType transactionResultType, Account account, Long amount) {
		return transactionRepository.save(Transaction.builder().transactionType(transactionType)
				.transactionResultType(transactionResultType).account(account).amount(amount)
				.balanceSnapshot(account.getBalance()).transactionId(UUID.randomUUID().toString().replace("-", ""))
				.transactedAt(LocalDateTime.now()).build());
	}

	@Transactional
	public TransactionDto cancelBalance(String transactionId, String accountNumber, Long amount) {

		Transaction transaction = transactionRepository.findByTransactionId(transactionId)
				.orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

		validateCancelBalance(transaction, account, amount);

		return TransactionDto.fromEntity(saveAndGetTransaction(TransactionType.CANCEL, S, account, amount));
	}

	private void validateCancelBalance(Transaction transaction, Account account, Long amount) {
		if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
			throw new AccountException(ErrorCode.TRANSACTION_ACCOUNT_UNMATCHED);
		}
		if (!Objects.equals(transaction.getAmount(), amount)) {
			throw new AccountException(ErrorCode.CANCEL_MUST_FULLY);
		}
		if (transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))) {
			throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
		}
	}

	@Transactional
	public void saveFailedCancelTransaction(String accountNumber, Long amount) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

		saveAndGetTransaction(TransactionType.CANCEL, S, account, amount);

	}

	public TransactionDto queryTransaction(String transactionId) {
		return TransactionDto.fromEntity(transactionRepository.findByTransactionId(transactionId)
				.orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND)));
	}
}
