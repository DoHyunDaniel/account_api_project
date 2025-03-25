package com.account.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.account.domain.Account;
import com.account.dto.AccountInfo;
import com.account.dto.CreateAccount;
import com.account.dto.DeleteAccount;
import com.account.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	@PostMapping("/account")
	public CreateAccount.Response createAccount(@RequestBody @Valid CreateAccount.Request request) {

		return CreateAccount.Response
				.from(accountService.createAccount(request.getUserId(), request.getInitialBalance()));
	}

	@DeleteMapping("/account")
	public DeleteAccount.Response deleteAccount(@RequestBody @Valid DeleteAccount.Request request) {

		return DeleteAccount.Response
				.from(accountService.deleteAccount(request.getUserId(), request.getAccountNumber()));
	}

	@GetMapping("/account")
	public List<AccountInfo> getAccountByUserId(@RequestParam("user_id") Long userId) {
		return accountService
				.getAccountByUserId(userId).stream().map(accountDto -> AccountInfo.builder()
						.accountNumber(accountDto.getAccountNumber()).balance(accountDto.getBalance()).build())
				.collect(Collectors.toList());

	}

//	@GetMapping("/get-lock")
//	public String getLock() {
//		return redisTestService.getLock();
//	}

	@GetMapping("/account/{id}")
	public Account getAccount(@PathVariable("id") Long id) {
		return accountService.getAccount(id);
	}
}
