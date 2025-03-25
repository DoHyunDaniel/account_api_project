package com.account.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.account.aop.AccountLockIdInterface;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {
	private final LockService lockService;
	
	@Around("@annotation(com.account.aop.AccountLock) && args(request)") 
	public Object arountMethod(
			ProceedingJoinPoint pjp,
			AccountLockIdInterface request) throws Throwable{
		// lock 취득 시도
		lockService.lock(request.getAccountNumber());
		try {
			return pjp.proceed();
		}finally {
			// lock 해제
			lockService.unLock(request.getAccountNumber());
		}
	}
}
