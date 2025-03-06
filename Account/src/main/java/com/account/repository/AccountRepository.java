package com.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.account.domain.Account;
import com.account.domain.AccountUser;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findFirstByOrderByIdDesc();

	Integer countByAccountUser(AccountUser accountUser);

	Optional<Account> findByAccountNumber(String AccountNumber);

	List<Account> findByAccountUser(AccountUser accountUser);
}
