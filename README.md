# 💳 Account_transaction_api_practice

Spring Boot 기반의 계좌 관리 및 거래 처리 연습 프로젝트입니다.  
사용자의 계좌 생성/삭제, 잔액 사용/취소, 거래 내역 조회 등의 기능을 제공하며,  
실제 서비스에서 발생할 수 있는 다양한 비즈니스 로직과 예외 처리, 분산 락 처리 등을 연습합니다.

---

## 🔧 기술 스택

- Java 17  
- Spring Boot  
- Spring Data JPA  
- H2 Database (개발환경)  
- Lombok  
- Redisson (분산 락)  
- Jakarta Validation  
- SLF4J Logging

---

## ✨ 주요 기능

### 1. 계좌 관리
- 사용자 계좌 생성 (최대 10개까지 가능)
- 계좌 삭제 (잔액이 0원이고, 미해지 상태일 경우만 가능)
- 사용자 ID로 계좌 목록 조회
- 계좌 ID로 상세 조회

### 2. 잔액 거래
- 계좌 잔액 사용
- 잔액 사용 취소 (거래 ID 기반, 전액 취소만 가능)
- 거래 ID로 거래 내역 조회
- 실패 거래에 대한 기록 저장

### 3. 락 처리
- Redisson 기반 계좌별 분산 락 적용
- 동시 요청에서의 데이터 정합성 보장

---

## 📂 API 요약

### 계좌 API

| 메서드 | URL | 설명 |
|--------|-----|------|
| POST | `/account` | 계좌 생성 |
| DELETE | `/account` | 계좌 삭제 |
| GET | `/account?user_id={userId}` | 사용자 계좌 목록 조회 |
| GET | `/account/{id}` | 계좌 ID로 조회 |

### 거래 API

| 메서드 | URL | 설명 |
|--------|-----|------|
| POST | `/transaction/use` | 잔액 사용 요청 |
| POST | `/transaction/cancel` | 잔액 사용 취소 요청 |
| GET | `/transaction/{transactionId}` | 거래 내역 조회 |

---

## ⚠️ 예외 처리 코드

| 에러코드 | 메시지 설명 |
|----------|-------------|
| `ACCOUNT_TRANSACTION_LOCK` | 해당 계좌는 사용 중입니다. |
| `INTERNAL_SERVER_ERROR` | 내부 서버 오류가 발생했습니다. |
| `INVALID_REQUEST` | 잘못된 요청입니다. |
| `USER_NOT_FOUND` | 사용자가 없습니다. |
| `ACCOUNT_NOT_FOUND` | 계좌가 없습니다. |
| `AMOUNT_EXCEED_BALANCE` | 거래 금액이 계좌 잔액보다 큽니다. |
| `MAX_ACCOUNT_PER_USER_10` | 사용자 최대 계좌는 10개입니다. |
| `USER_ACCOUNT_UNMATCHED` | 사용자 계좌의 소유주가 다릅니다. |
| `ACCOUNT_ALREADY_UNREGISTERED` | 계좌가 이미 해지되었습니다. |
| `CANCEL_MUST_FULLY` | 부분 취소는 허용되지 않습니다. |
| `TRANSACTION_ACCOUNT_UNMATCHED` | 이 거래는 해당 계좌에서 발생한 거래가 아닙니다. |
| `TOO_OLD_ORDER_TO_CANCEL` | 1년이 지난 거래는 취소가 불가능합니다. |
| `BALANCE_NOT_EMPTY` | 잔액이 있는 계좌는 해지할 수 없습니다. |
| `TRANSACTION_NOT_FOUND` | 해당 거래가 없습니다. |

---

## ▶️ 실행 방법

```bash
# 1. 프로젝트 클론
git clone https://github.com/your-id/Account_transaction_api_practice.git

# 2. 빌드 및 실행
./gradlew bootRun
```

> 💡 기본 포트는 8080이며, H2 콘솔은 `/h2-console`에서 접근할 수 있습니다.  
> application.yml 설정에 따라 H2 DB 정보가 포함되어 있어야 합니다.

---

## 🗂️ 디렉토리 구조

```
src
├── controller          # API 컨트롤러
├── service             # 비즈니스 로직
├── dto                 # 요청/응답 DTO
├── domain              # JPA Entity
├── repository          # DB 접근 레이어
├── exception           # 커스텀 예외 처리
├── type                # Enum 및 에러 코드 정의
```

---

## 📌 프로젝트 성격

이 프로젝트는 실무 수준의 계좌 및 거래 시스템 구조를 연습하기 위한 목적이며,  
**도메인 기반 설계**, **비즈니스 로직 검증**, **예외 처리**, **락 처리** 등의 다양한 기능을 손쉽게 실습할 수 있도록 구성되어 있습니다.

---

## 📬 문의

궁금한 점이나 제안이 있다면 [이슈 등록](https://github.com/your-id/Account_transaction_api_practice/issues)을 통해 남겨주세요!

