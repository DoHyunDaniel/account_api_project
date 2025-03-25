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

/**
 * 계좌 관련 기능을 처리하는 REST 컨트롤러입니다.
 * 계좌 생성, 삭제, 조회 등의 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	/**
	 * 사용자의 계좌를 생성합니다.
	 *
	 * @param request 사용자 ID와 초기 잔액 정보를 담은 요청 DTO
	 * @return 생성된 계좌 정보를 담은 응답 DTO
	 */
	@PostMapping("/account")
	public CreateAccount.Response createAccount(@RequestBody @Valid CreateAccount.Request request) {
		return CreateAccount.Response
				.from(accountService.createAccount(request.getUserId(), request.getInitialBalance()));
	}

	/**
	 * 사용자의 계좌를 삭제합니다.
	 *
	 * @param request 사용자 ID와 삭제할 계좌번호를 담은 요청 DTO
	 * @return 계좌 삭제 결과를 담은 응답 DTO
	 */
	@DeleteMapping("/account")
	public DeleteAccount.Response deleteAccount(@RequestBody @Valid DeleteAccount.Request request) {
		return DeleteAccount.Response
				.from(accountService.deleteAccount(request.getUserId(), request.getAccountNumber()));
	}

	/**
	 * 사용자 ID를 기준으로 사용자의 모든 계좌 정보를 조회합니다.
	 *
	 * @param userId 조회할 사용자의 ID
	 * @return 계좌 정보 리스트 (계좌번호 및 잔액 포함)
	 */
	@GetMapping("/account")
	public List<AccountInfo> getAccountByUserId(@RequestParam("user_id") Long userId) {
		return accountService
				.getAccountByUserId(userId).stream().map(accountDto -> AccountInfo.builder()
						.accountNumber(accountDto.getAccountNumber()).balance(accountDto.getBalance()).build())
				.collect(Collectors.toList());
	}

	/**
	 * 계좌 ID를 통해 단일 계좌 정보를 조회합니다.
	 *
	 * @param id 조회할 계좌의 ID
	 * @return 계좌 엔티티 객체
	 */
	@GetMapping("/account/{id}")
	public Account getAccount(@PathVariable("id") Long id) {
		return accountService.getAccount(id);
	}
}
