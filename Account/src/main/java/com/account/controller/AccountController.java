package com.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.account.domain.Account;
import com.account.dto.AccountDto;
import com.account.dto.CreateAccount;
import com.account.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	@PostMapping("/account")
	public CreateAccount.Response createAccount(
			@RequestBody @Valid CreateAccount.Request request
			) {

		return CreateAccount.Response.from(
				accountService.createAccount(
				request.getUserId(), 
				request.getInitialBalance()
				)
		);
	}

	@GetMapping("/account/{id}")
	public Account getAccount(@PathVariable Long id) {
		return accountService.getAccount(id);
	}
}
