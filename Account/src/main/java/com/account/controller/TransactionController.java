package com.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.account.aop.AccountLock;
import com.account.dto.CancelBalance;
import com.account.dto.QueryTransactionResponse;
import com.account.dto.UseBalance;
import com.account.exception.AccountException;
import com.account.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 잔액 관련 기능을 처리하는 컨트롤러입니다.
 * <p>
 * 제공 기능:
 * <ul>
 *   <li>잔액 사용</li>
 *   <li>잔액 사용 취소</li>
 *   <li>거래 조회</li>
 * </ul>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {
	private final TransactionService transactionService;

	/**
	 * 계좌에서 잔액을 사용하는 요청을 처리합니다.
	 * 
	 * @param request 사용자 ID, 계좌번호, 금액이 포함된 요청 객체
	 * @return 잔액 사용 결과 응답 객체
	 * @throws InterruptedException 
	 * @throws AccountException 잔액 부족, 계좌 상태 오류 등 예외 발생 시
	 */
	@PostMapping("/transaction/use")
	@AccountLock
	public UseBalance.Response useBalance(@Valid @RequestBody UseBalance.Request request) throws InterruptedException {

		try {
			Thread.sleep(5000L);
			return UseBalance.Response.from(transactionService.useBalance(
					request.getUserId(), request.getAccountNumber(), request.getAmount()));
		} catch (AccountException e) {
			log.error("잔액 사용 실패");

			// 실패한 거래도 기록
			transactionService.saveFailedUseTransaction(request.getAccountNumber(), request.getAmount());

			throw e;
		}
	}

	/**
	 * 잔액 사용 거래를 취소합니다.
	 *
	 * @param request 거래 ID, 계좌번호, 금액이 포함된 요청 객체
	 * @return 잔액 취소 결과 응답 객체
	 * @throws AccountException 거래 취소 실패 시 예외 발생
	 */
	@PostMapping("/transaction/cancel")
	public CancelBalance.Response cancelBalance(@Valid @RequestBody CancelBalance.Request request) {

		try {
			return CancelBalance.Response.from(transactionService.cancelBalance(
					request.getTransactionId(), request.getAccountNumber(), request.getAmount()));
		} catch (AccountException e) {
			log.error("잔액 취소 실패");

			// 실패한 거래도 기록
			transactionService.saveFailedCancelTransaction(request.getAccountNumber(), request.getAmount());

			throw e;
		}
	}

	/**
	 * 거래 ID를 기반으로 거래 내역을 조회합니다.
	 *
	 * @param transactionId 조회할 거래의 ID
	 * @return 거래 정보 응답 객체
	 */
	@GetMapping("/transaction/{transactionId}")
	public QueryTransactionResponse queryTransaction(@PathVariable String transactionId) {
		return QueryTransactionResponse.from(transactionService.queryTransaction(transactionId));
	}
}
