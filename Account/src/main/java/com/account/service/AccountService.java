package com.account.service;

import static com.account.type.AccountStatus.IN_USE;
import static com.account.type.AccountStatus.UNREGISTERED;
import static com.account.type.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.account.domain.Account;
import com.account.domain.AccountUser;
import com.account.dto.AccountDto;
import com.account.exception.AccountException;
import com.account.repository.AccountRepository;
import com.account.repository.AccountUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 계좌 생성, 조회, 삭제 등 계좌 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;
	private final AccountUserRepository accountUserRepository;

	/**
	 * 계좌를 생성합니다.
	 * <p>
	 * - 사용자 존재 여부 확인<br>
	 * - 계좌 개수 제한(10개) 검사<br>
	 * - 새로운 계좌번호 생성<br>
	 * - 계좌 저장 후 DTO 반환
	 *
	 * @param userId         계좌를 생성할 사용자 ID
	 * @param initialBalance 초기 잔액
	 * @return 생성된 계좌 정보 DTO
	 * @throws AccountException 사용자 미존재 또는 계좌 개수 초과 시 발생
	 */
	@Transactional
	public AccountDto createAccount(Long userId, Long initialBalance) {
		AccountUser accountUser = accountUserRepository.findById(userId)
				.orElseThrow(() -> new AccountException(USER_NOT_FOUND));

		validateCreateAccount(accountUser);

		String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
				.map(account -> (Integer.parseInt(account.getAccountNumber()) + 1) + "")
				.orElse("1000000000");

		return AccountDto.fromEntity(accountRepository.save(
				Account.builder()
						.accountUser(accountUser)
						.accountStatus(IN_USE)
						.accountNumber(newAccountNumber)
						.balance(initialBalance)
						.registeredAt(LocalDateTime.now())
						.build()
		));
	}

	/**
	 * 계좌 생성 시 계좌 개수 제한(10개) 조건을 검증합니다.
	 *
	 * @param accountUser 계좌를 생성하려는 사용자
	 * @throws AccountException 계좌 개수 제한 초과 시 발생
	 */
	private void validateCreateAccount(AccountUser accountUser) {
		if (accountRepository.countByAccountUser(accountUser) == 10) {
			throw new AccountException(MAX_ACCOUNT_PER_USER_10);
		}
	}

	/**
	 * ID로 계좌를 조회합니다.
	 *
	 * @param id 계좌 ID
	 * @return 계좌 Entity
	 * @throws RuntimeException ID가 음수일 경우 예외 발생
	 */
	@Transactional
	public Account getAccount(Long id) {
		if (id < 0) {
			throw new RuntimeException("Minus");
		}
		return accountRepository.findById(id).get();
	}

	/**
	 * 계좌를 삭제(해지)합니다.
	 * <p>
	 * - 사용자 및 계좌 존재 여부 확인<br>
	 * - 사용자-계좌 소유 일치 여부 확인<br>
	 * - 이미 해지된 계좌인지 여부 확인<br>
	 * - 잔액이 남아있는지 여부 확인<br>
	 * - 계좌 상태를 해지로 변경하고 해지 시간 저장
	 *
	 * @param userId        사용자 ID
	 * @param accountNumber 계좌번호
	 * @return 삭제된 계좌 정보 DTO
	 * @throws AccountException 유효성 검사 실패 시 발생
	 */
	@Transactional
	public AccountDto deleteAccount(Long userId, String accountNumber) {
		AccountUser accountUser = accountUserRepository.findById(userId)
				.orElseThrow(() -> new AccountException(USER_NOT_FOUND));

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

		validateDeleteAccount(accountUser, account);

		account.setAccountStatus(UNREGISTERED);
		account.setUnRegisteredAt(LocalDateTime.now());

		accountRepository.save(account);

		return AccountDto.fromEntity(account);
	}

	/**
	 * 계좌 삭제 전 유효성 검사를 수행합니다.
	 *
	 * @param accountUser 사용자 Entity
	 * @param account     계좌 Entity
	 * @throws AccountException 사용자 불일치, 이미 해지된 계좌, 잔액이 남아있는 경우
	 */
	private void validateDeleteAccount(AccountUser accountUser, Account account) {
		if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
			throw new AccountException(USER_ACCOUNT_UNMATCHED);
		}
		if (account.getAccountStatus() == UNREGISTERED) {
			throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
		}
		if (account.getBalance() > 0) {
			throw new AccountException(BALANCE_NOT_EMPTY);
		}
	}

	/**
	 * 사용자 ID로 해당 사용자의 모든 계좌를 조회합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 계좌 정보 DTO 리스트
	 * @throws AccountException 사용자 미존재 시 발생
	 */
	public List<AccountDto> getAccountByUserId(Long userId) {
		AccountUser accountUser = accountUserRepository.findById(userId)
				.orElseThrow(() -> new AccountException(USER_NOT_FOUND));

		List<Account> accounts = accountRepository.findByAccountUser(accountUser);

		return accounts.stream()
				.map(AccountDto::fromEntity)
				.collect(Collectors.toList());
	}
}
