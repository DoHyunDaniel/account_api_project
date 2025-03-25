package com.account.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AccountInfo {
	private String accountNumber;
	private Long balance;
}
