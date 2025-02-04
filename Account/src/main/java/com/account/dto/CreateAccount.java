package com.account.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CreateAccount {
	@Getter
	@Setter
	@AllArgsConstructor
	public static class Request {
		@NotNull
		@Min(1)
		private long userId;

		@NotNull
		@Min(100)
		private long initialBalance;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		private long userId;
		private String accountNumber;
		private LocalDateTime registeredAt;

		public static Response from(AccountDto accountDto) {
			return Response.builder().userId(accountDto.getUserId()).accountNumber(accountDto.getAccountNumber())
					.registeredAt(accountDto.getRegisteredAt()).build();
		}
	}
}
