package com.account.controller;

import static com.account.type.TransactionResultType.S;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.account.dto.TransactionDto;
import com.account.dto.UseBalance;
import com.account.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
	@MockBean
	private TransactionService transactionService;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void SuccessfulBalance() throws Exception {
		// given
		given(transactionService.useBalance(anyLong(), any(), anyLong())).willReturn(TransactionDto.builder()
				.accountNumber("1000000000")
				.transactedAt(LocalDateTime.now())
				.amount(12345L)
				.transactionId("transactionId")
				.transactionResultType(S)
				.build());
		// when

		// then
		mockMvc.perform(post("/transaction/use")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						new UseBalance.Request(1L, "2000000000",3000L)))
				).andDo(print()).andExpect(status().isOk())
		.andExpect(jsonPath("$.accountNumber").value("1000000000"))
		.andExpect(jsonPath("$.transactionResult").value("S"))
		.andExpect(jsonPath("$.transactionId").value("transactionId"))
		.andExpect(jsonPath("$.amount").value("12345"));
		
		
	}

}
