package com.account.dto;

import java.time.LocalDateTime;

import com.account.aop.AccountLockIdInterface;
import com.account.dto.UseBalance.Response;
import com.account.type.TransactionResultType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UseBalance {
	/*
	 * { "userId": 1, "accountNumber":"1000000000", "amount":1000 }
	 */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Request implements AccountLockIdInterface {
		@NotNull
		@Min(1)
		private Long userId;

		@NotBlank
		@Size(min = 10, max = 10)
		private String accountNumber;

		@NotNull
		@Min(10)
		@Max(1000_000_000)
		private Long amount;
	}

	/*
	 * { "accountNumber": "1234567890", "transactionResult":"S",
	 * "transactionId":"c2033bb6d82a4250aecf8e27c49b63f6", "amount":"1000,
	 * "transactionedAt":"2022-06-01T23:26:14.671859"
	 */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		private String accountNumber;
		private TransactionResultType transactionResult;
		private String transactionId;
		private Long amount;
		private LocalDateTime transactedAt;
		public static Response from(TransactionDto transactionDto) {
			return Response.builder()
					.accountNumber(transactionDto.getAccountNumber())
					.transactionResult(transactionDto.getTransactionResultType())
					.transactionId(transactionDto.getTransactionId())
					.amount(transactionDto.getAmount())
					.transactedAt(transactionDto.getTransactedAt())
					.build();
		}

	}
}
