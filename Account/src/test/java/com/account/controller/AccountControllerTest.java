package com.account.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.account.domain.Account;
import com.account.dto.AccountDto;
import com.account.dto.CreateAccount;
import com.account.service.AccountService;
import com.account.type.AccountStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AccountController.class)
class AccountControllerTest {
	@MockBean
	private AccountService accountService;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void successCreateAccount() throws JsonProcessingException, Exception {
		// given
		given(accountService.createAccount(anyLong(), anyLong()))
				.willReturn(AccountDto.builder().userId(1L).accountNumber("12345678").registeredAt(LocalDateTime.now())
						.unRegisteredAt(LocalDateTime.now()).build());
		// when

		// then	
		mockMvc.perform(post("account").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateAccount.Request(1L, 100L))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.accountNumber").value("1234567890")).andDo(print());
	}

	@Test
	void successGetAccount() throws Exception {
		// given
		given(accountService.getAccount(anyLong()))
				.willReturn(Account.builder().accountNumber("3456").accountStatus(AccountStatus.IN_USE).build());

		// when
		// then
		mockMvc.perform(get("/account/876")).andDo(print()).andExpect(jsonPath("$.accountNumber").value("3456"))
				.andExpect(jsonPath("$.accountStatus").value("IN_USE")).andExpect(status().isOk());
	}
}