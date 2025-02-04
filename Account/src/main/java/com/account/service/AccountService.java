package com.account.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.account.domain.Account;
import com.account.domain.AccountUser;
import com.account.dto.AccountDto;
import com.account.exception.AccountException;
import com.account.repository.AccountRepository;
import com.account.repository.AccountUserRepository;
import com.account.type.AccountStatus;
import com.account.type.ErrorCode;

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
				.orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

		String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
				.map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "").orElse("1000000000");

		
		return AccountDto.fromEntity(
				accountRepository.save(
						Account.builder()
						.accountUser(accountUser)
						.accountStatus(AccountStatus.IN_USE)
						.accountNumber(newAccountNumber)
						.balance(initialBalance)
						.registerdAt(LocalDateTime.now())
						.build()));
	}

	@Transactional
	public Account getAccount(Long id) {
		if (id < 0) {
			throw new RuntimeException("Minus");
		}
		return accountRepository.findById(id).get();
	}
}
