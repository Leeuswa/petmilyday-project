# Petmilyday (펫밀리데이)

반려동물 통합 케어 플랫폼. 동물병원 예약·진료기록 관리부터 반려용품 쇼핑몰, 중고거래, 커뮤니티, AI 자가진단까지 하나의 서비스에서 제공하는 Spring Boot 기반 모놀리식 웹 애플리케이션입니다.

- **프로젝트 기간**: 팀 프로젝트 (4인)
- **저장소**: https://github.com/Leeuswa/petmilyday-project

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language / Runtime | Java 21 |
| Framework | Spring Boot 3.5.14 (Web MVC, Data JPA, Validation) |
| Persistence | Spring Data JPA, QueryDSL, MariaDB, HikariCP |
| View | Thymeleaf, Spring Security 6 연동 |
| Auth | Spring Security, OAuth2 Client(Google·Kakao), JWT |
| Realtime | WebSocket(1:1 채팅), SSE(실시간 알림) |
| External | AWS S3, Kakao Map/지오코딩, KakaoPay, Anthropic Claude(Spring AI) |
| Build | Gradle |

---

## 아키텍처

계층형 아키텍처로 관심사를 분리하고, 서비스는 인터페이스와 구현체를 나누어 결합도를 낮췄습니다.

```
Controller  →  Service (interface) / ServiceImpl  →  Repository  →  DB
                                                        │
                                              Entity / DTO / QueryDSL
```

```
src/main/java/com/petmilyday
├── config/            보안, JWT, QueryDSL, S3, WebSocket, Web 설정
│   └── jwt/           JWT 필터, 토큰 Provider, OAuth2 성공 핸들러
├── controller/        도메인별 컨트롤러 (admin, community, hospital, shop, usedpost ...)
├── service/           서비스 인터페이스
│   └── impl/          서비스 구현체
├── repository/        JPA 리포지토리 (+ QueryDSL Custom / Impl)
├── entity/            JPA 엔티티 (도메인별)
└── dto/               요청 / 응답 DTO

src/main/resources
├── templates/         Thymeleaf 뷰
├── static/            css / js
└── application.properties   설정 파일 (git 미추적 — 아래 참고)
```

**도메인**: member · community · diagnosis · hospital · medical · reservation · product/shop · used · cart · wishlist · notification · admin

---

## 주요 기능

- **회원 / 인증**: 일반 로그인 및 소셜 로그인(Google·Kakao), JWT 기반 인증, 역할별 접근제어(USER / 병원관리자 / ADMIN)
- **동물병원**: 병원 목록·상세, 주소 기반 검색 및 필터, 카카오맵 표시, 병원 리뷰
- **예약**: 예약 신청·취소·내역 조회, 예약 슬롯 동시성 제어, 병원 관리자 승인 흐름
- **진료 / 건강 관리**: 진료기록·예방접종 이력 조회
- **커뮤니티**: 자유 게시판·모임 게시판, 댓글·대댓글·좋아요, 익명 처리, 신고
- **쇼핑몰**: 상품·카테고리, 장바구니, 주문·결제(KakaoPay), 정기구독, 리뷰·QnA·위시리스트
- **중고거래**: 게시글 CRUD, 1:1 실시간 채팅(WebSocket), 매너 점수, 신고
- **AI**: Claude 기반 반려동물 자가진단·가이드, 맞춤 사료 추천
- **알림**: SSE 기반 실시간 알림
- **관리자**: 대시보드, 회원·병원·예약·콘텐츠 관리, 검색·페이징

---

## 실행 방법

### 사전 요구사항
- JDK 21
- MariaDB (로컬에 `petmilyday` 데이터베이스 생성)

### 설정
`application.properties`는 민감 정보를 포함하므로 git에 추적되지 않습니다. `src/main/resources/application.properties`를 아래 형식으로 생성합니다.

```properties
server.port=8080

# Database
spring.datasource.url=jdbc:mariadb://localhost:3306/petmilyday
spring.datasource.username=<db-user>
spring.datasource.password=<db-password>

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# 외부 연동 키 (각 콘솔에서 발급)
# OAuth2(Google/Kakao), JWT, KakaoPay, Kakao Map, AWS S3, Anthropic Claude
jwt.secret=<32자 이상 시크릿>
```

### 빌드 및 실행
```bash
./gradlew build       # 빌드
./gradlew bootRun     # 실행 → http://localhost:8080
```

---

## 담당 파트 (Leeuswa)

동물병원 및 예약 도메인을 중심으로, 인증·알림·AI 연동 등 서비스 전반의 공통 기능을 담당했습니다.

### 1. 동물병원 검색 (QueryDSL 동적 쿼리)
- 키워드·진료과목·응급여부 조건을 `BooleanExpression`으로 조합해 **null 조건은 자동 무시되는 동적 검색** 구현
- 사용자 위치 또는 검색 지역 좌표가 있으면 **하버사인(Haversine) 거리 계산으로 가까운 병원 순 정렬**, 없으면 기본 정렬로 분기
- 카카오 지오코딩 API로 주소 → 좌표 변환 후 카카오맵에 병원 위치 표시
- 목록 페이징 처리

### 2. 예약 시스템 (동시성 제어)
- 예약 신청·취소·내역 조회 및 병원 관리자 승인 흐름 구현
- **동일 슬롯 중복 예약을 막기 위해 `@Lock(PESSIMISTIC_WRITE)` 비관적 락 적용** — 예약 등록 시 병원 엔티티를 락으로 조회(`findByIdForUpdate`)해 동시 요청 경합 상황에서 정합성 보장
- 과거 날짜·시간 예약 차단, 승인 과정에서 병원 소유권 검증 추가

### 3. 진료 / 건강 기록
- 진료기록·예방접종 이력 조회 기능
- 진료기록 미래 날짜 작성 방지 등 입력 검증

### 4. 인증 / 권한 (JWT + OAuth2)
- **JWT 토큰 발급·검증** 구현 (HMAC-SHA 서명, `role` 커스텀 클레임, 30분 만료)
- 소셜 로그인 성공 시 JWT를 발급해 **HttpOnly 쿠키(SameSite=Lax)로 저장**하는 `OAuth2SuccessHandler` 구현
- 하드코딩된 권한 로직 정리, 일반회원 → 병원관리자 전환 시 재로그인 처리

### 5. 실시간 알림 (SSE)
- `SseEmitter` 기반 실시간 알림 구현
- 사용자별 다중 연결을 `ConcurrentHashMap` + `CopyOnWriteArrayList`로 관리하여 **동시성 안전** 확보
- 연결 종료·타임아웃·에러 및 전송 실패한(dead) emitter 자동 정리

### 6. AI 연동
- Anthropic Claude(Spring AI) 기반 **맞춤 사료 추천** 기능
- 기존 AI 로직을 Claude 모델로 교체

### 7. 그 외
- 마이페이지(내가 작성한 병원 리뷰, 진료기록, 예방접종 이력)
- 주요 목록 화면 페이징 처리
- 관리자 검색 기능 및 다수 버그 수정, 메인·병원 페이지 UI 개선

---

## 팀 구성

| 멤버 | 담당 |
|------|------|
| Leeuswa | 동물병원·예약·진료기록, 인증(JWT/OAuth2), SSE 알림, 카카오맵, AI 사료추천, 관리자 |
| endy | 회원가입/로그인, 커뮤니티(자유·모임) 게시판, 댓글·좋아요, 반려동물 프로필, 신고 |
| n1mppp | 쇼핑몰(상품·장바구니·주문·정기구독·리뷰·QnA·위시리스트), KakaoPay 결제 |
| jungwonseok | 중고거래(게시글·1:1 채팅), KakaoPay, AI 자가진단·가이드 |
