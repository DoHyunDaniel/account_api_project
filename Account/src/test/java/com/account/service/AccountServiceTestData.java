package com.account.service;

import com.account.repository.AccountRepository;
import com.account.repository.AccountUserRepository;

public class AccountServiceTestData {
	@Mock
	public AccountRepository accountRepository;
	@Mock
	public AccountUserRepository accountUserRepository;
	@InjectMocks
	public AccountService accountService;

	public AccountServiceTestData() {
	}
}