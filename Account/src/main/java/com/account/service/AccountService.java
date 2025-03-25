package com.account.service;

import static com.account.type.AccountStatus.IN_USE;
import static com.account.type.AccountStatus.UNREGISTERED;
import static com.account.type.ErrorCode.ACCOUNT_ALREADY_UNREGISTERED;
import static com.account.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.account.type.ErrorCode.BALANCE_NOT_EMPTY;
import static com.account.type.ErrorCode.MAX_ACCOUNT_PER_USER_10;
import static com.account.type.ErrorCode.USER_ACCOUNT_UNMATCHED;
import static com.account.type.ErrorCode.USER_NOT_FOUND;

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

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;

	private final AccountUserRepository accountUserRepository;

	// 사용자가 있는지 조회
	// 계좌의 번호를 생성하고
	// 계좌를 저장하고, 그 정보를 넘긴다.
	@Transactional
	public AccountDto createAccount(Long userId, Long initialBalance) {
		AccountUser accountUser = accountUserRepository.findById(userId)
				.orElseThrow(() -> new AccountException(USER_NOT_FOUND));

		validateCreateAccount(accountUser);

		String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
				.map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "").orElse("1000000000");

		return AccountDto.fromEntity(accountRepository
				.save(Account.builder().accountUser(accountUser).accountStatus(IN_USE).accountNumber(newAccountNumber)
						.balance(initialBalance).registeredAt(LocalDateTime.now()).build()));
	}

	private void validateCreateAccount(AccountUser accountUser) {
		if (accountRepository.countByAccountUser(accountUser) == 10) {
			throw new AccountException(MAX_ACCOUNT_PER_USER_10);
		}
	}

	@Transactional
	public Account getAccount(Long id) {
		if (id < 0) {
			throw new RuntimeException("Minus");
		}
		return accountRepository.findById(id).get();
	}

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

	public List<AccountDto> getAccountByUserId(Long userId) {
		// TODO Auto-generated method stub
		AccountUser accountUser = accountUserRepository.findById(userId)
				.orElseThrow(() -> new AccountException(USER_NOT_FOUND));
		List<Account> accounts = accountRepository.findByAccountUser(accountUser);

		return accounts.stream().map(account -> AccountDto.fromEntity(account)).collect(Collectors.toList());
	}

}
