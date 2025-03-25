package com.account.dto;

import java.time.LocalDateTime;

import com.account.domain.Transaction;
import com.account.type.TransactionResultType;
import com.account.type.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
	private String accountNumber;
	private TransactionType transactionType;
	private TransactionResultType transactionResultType;
	private Long amount;
	private Long balanceSnapshot;
	private String transactionId;
	private LocalDateTime transactedAt;
	
	public static TransactionDto fromEntity(Transaction transaction) {
		return TransactionDto.builder()
				.accountNumber(transaction.getAccount().getAccountNumber())
				.transactionType(transaction.getTransactionType())
				.transactionResultType(transaction.getTransactionResultType())
				.amount(transaction.getAmount())
				.balanceSnapshot(transaction.getBalanceSnapshot())
				.transactionId(transaction.getTransactionId())
				.transactedAt(transaction.getTransactedAt())
				.build();
	}

}
