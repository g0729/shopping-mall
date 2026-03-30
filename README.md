# Shopping Mall

Spring Boot 기반 쇼핑몰 백엔드 프로젝트입니다.

## 기술 스택

| 분류 | 기술 |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Session |
| ORM | JPA/Hibernate, QueryDSL |
| Database | MySQL 8.0, H2 (개발) |
| Cache | Redis 7 |
| Storage | AWS S3 (운영), 로컬 디스크 (개발) |
| Auth | Spring Security, OAuth2 (Google, Kakao) |
| Payment | 토스페이먼츠 (테스트 모드) |
| Build | Gradle |
| Infra | Docker, Docker Compose, Nginx |
| CI/CD | GitHub Actions |
| API Docs | SpringDoc (Swagger UI) |

---

## 주요 기능

| 기능 | 설명 |
|---|---|
| 회원 | 일반 로그인 / OAuth2 소셜 로그인 (Google, Kakao) |
| 상품 | 상품 목록 (검색, 페이징), 상품 상세, 관리자 상품 등록/수정 |
| 장바구니 | 상품 담기, 수량 변경, 주문 |
| 주문 | 단건 주문, 장바구니 일괄 주문, 주문 취소 |
| 결제 | 토스페이먼츠 연동 (테스트 모드), 결제 승인/취소 |
| 이미지 | AWS S3 업로드 (운영) |
| API 문서 | Swagger UI (`/swagger-ui/index.html`) - 개발 환경 전용 |

---

## 인프라 및 배포

- GitHub Actions CI/CD: Gradle 빌드 → Docker 이미지 빌드 → DockerHub Push → SSH 배포
- Docker Compose: Nginx, Spring Boot, MySQL, Redis 컨테이너 구성
- 환경 분리: `@Profile` 기반으로 개발/운영 설정 자동 전환

---

## 로컬 실행 방법

**사전 요구사항**: Java 17, Docker

```bash
# 1. 저장소 클론
git clone https://github.com/g0729/shopping-mall.git
cd shopping-mall/main

# 2. Docker로 MySQL, Redis 실행
docker compose up mysql redis -d

# 3. 애플리케이션 실행
./gradlew bootRun
```

**.env 파일 예시**

```env
DB_USERNAME=shopping
DB_PASSWORD=your_password
DB_ROOT_PASSWORD=your_root_password
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
```

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
