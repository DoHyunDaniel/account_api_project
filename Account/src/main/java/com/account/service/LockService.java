package com.account.service;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.account.exception.AccountException;
import com.account.type.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redisson 기반의 분산 락 서비스를 제공합니다.
 * <p>
 * - 동시성 제어를 위해 계좌 번호를 기준으로 락을 설정 및 해제합니다.  
 * - 락을 획득하지 못하면 예외를 발생시켜 트랜잭션 충돌을 방지합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LockService {
	private final RedissonClient redissonClient;

	/**
	 * 계좌번호를 기준으로 분산 락을 획득합니다.
	 * <p>
	 * - 최대 1초 대기 후 락 획득 시도<br>
	 * - 15초 동안 락 유지<br>
	 * - 락 획득 실패 시 예외 발생
	 *
	 * @param accountNumber 락을 걸 계좌번호
	 * @throws AccountException 락 획득 실패 시
	 */
	public void lock(String accountNumber) {
		RLock lock = redissonClient.getLock(getLockKey(accountNumber));
		log.debug("Trying lock for accountNumber: {}", accountNumber);

		try {
			boolean isLock = lock.tryLock(1, 15, TimeUnit.SECONDS);
			if (!isLock) {
				log.error("=====Lock acquisition failed=====");
				throw new AccountException(ErrorCode.ACCOUNT_NOT_FOUND); // 실제로는 별도 LOCK_FAIL 에러코드가 적절
			}
		} catch (AccountException e) {
			throw e;
		} catch (Exception e) {
			log.error("Redis lock failed", e);
			throw new RuntimeException("Redis lock error");
		}
	}

	/**
	 * 락 해제를 수행합니다.
	 *
	 * @param accountNumber 락을 해제할 계좌번호
	 */
	public void unLock(String accountNumber) {
		log.debug("Unlock for accountNumber: {} ", accountNumber);
		redissonClient.getLock(getLockKey(accountNumber)).unlock();
	}

	/**
	 * 계좌번호를 기반으로 Redis 락 키를 생성합니다.
	 *
	 * @param accountNumber 계좌번호
	 * @return Redis 락 키 문자열
	 */
	private String getLockKey(String accountNumber) {
		return "ACLK: " + accountNumber;
	}
}
