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

/**
 * 거래(잔액 사용, 취소, 조회) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final AccountUserRepository accountUserRepository;
	private final AccountRepository accountRepository;

	/**
	 * 잔액을 사용하는 거래를 처리합니다.
	 * <p>
	 * - 사용자 및 계좌 유효성 검증<br>
	 * - 잔액 차감<br>
	 * - 거래 정보 저장 및 응답 반환
	 *
	 * @param userId        사용자 ID
	 * @param accountNumber 계좌번호
	 * @param amount        사용 금액
	 * @return 거래 정보 DTO
	 * @throws AccountException 검증 실패 시 예외 발생
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

	/**
	 * 잔액 사용 전 유효성 검사를 수행합니다.
	 *
	 * @param user   사용자
	 * @param account 계좌
	 * @param amount 사용 금액
	 * @throws AccountException 검증 실패 시 예외 발생
	 */
	private void validateUseBalance(AccountUser user, Account account, Long amount) {
		if (!Objects.equals(user.getId(), account.getAccountUser().getId())) {
			throw new AccountException(ErrorCode.USER_ACCOUNT_UNMATCHED);
		}
		if (account.getAccountStatus() != AccountStatus.IN_USE) {
			throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
		}
		if (account.getBalance() < amount) {
			throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
		}
	}

	/**
	 * 잔액 사용 실패 시 실패 거래를 기록합니다.
	 *
	 * @param accountNumber 계좌번호
	 * @param amount        시도한 금액
	 */
	@Transactional
	public void saveFailedUseTransaction(String accountNumber, Long amount) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

		saveAndGetTransaction(USE, F, account, amount);
	}

	/**
	 * 거래 정보를 생성하고 저장합니다.
	 *
	 * @param transactionType       거래 유형 (USE, CANCEL 등)
	 * @param transactionResultType 거래 결과 (S, F)
	 * @param account               대상 계좌
	 * @param amount                거래 금액
	 * @return 저장된 거래 엔티티
	 */
	private Transaction saveAndGetTransaction(TransactionType transactionType,
			TransactionResultType transactionResultType, Account account, Long amount) {

		return transactionRepository.save(Transaction.builder()
				.transactionType(transactionType)
				.transactionResultType(transactionResultType)
				.account(account)
				.amount(amount)
				.balanceSnapshot(account.getBalance())
				.transactionId(UUID.randomUUID().toString().replace("-", ""))
				.transactedAt(LocalDateTime.now())
				.build());
	}

	/**
	 * 잔액 사용을 취소합니다.
	 *
	 * @param transactionId 원거래 ID
	 * @param accountNumber 계좌번호
	 * @param amount        취소 금액
	 * @return 취소 거래 정보 DTO
	 * @throws AccountException 검증 실패 시 예외 발생
	 */
	@Transactional
	public TransactionDto cancelBalance(String transactionId, String accountNumber, Long amount) {

		Transaction transaction = transactionRepository.findByTransactionId(transactionId)
				.orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

		validateCancelBalance(transaction, account, amount);

		return TransactionDto.fromEntity(
				saveAndGetTransaction(TransactionType.CANCEL, S, account, amount));
	}

	/**
	 * 잔액 취소 전 유효성 검사를 수행합니다.
	 *
	 * @param transaction 원거래 정보
	 * @param account     대상 계좌
	 * @param amount      취소 요청 금액
	 * @throws AccountException 거래-계좌 불일치, 금액 불일치, 1년 초과된 거래 등
	 */
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

	/**
	 * 잔액 취소 실패 시 실패 거래를 기록합니다.
	 *
	 * @param accountNumber 계좌번호
	 * @param amount        시도한 금액
	 */
	@Transactional
	public void saveFailedCancelTransaction(String accountNumber, Long amount) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

		saveAndGetTransaction(TransactionType.CANCEL, F, account, amount);
	}

	/**
	 * 거래 ID를 통해 거래 내역을 조회합니다.
	 *
	 * @param transactionId 거래 ID
	 * @return 거래 정보 DTO
	 * @throws AccountException 거래가 존재하지 않을 경우
	 */
	public TransactionDto queryTransaction(String transactionId) {
		return TransactionDto.fromEntity(
				transactionRepository.findByTransactionId(transactionId)
						.orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND)));
	}
}
