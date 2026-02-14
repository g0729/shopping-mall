# 🔐 OAuth 2.0 소셜 로그인 설정 가이드

이 문서는 Google과 Kakao OAuth 2.0 소셜 로그인을 설정하는 방법을 안내합니다.

---

## 📋 목차
1. [환경 변수 설정](#환경-변수-설정)
2. [Google OAuth 설정](#google-oauth-설정)
3. [Kakao OAuth 설정](#kakao-oauth-설정)
4. [로컬 실행 확인](#로컬-실행-확인)
5. [트러블슈팅](#트러블슈팅)

---

## 🔧 환경 변수 설정

### IntelliJ IDEA에서 설정

1. **Run Configuration 열기**
   - 상단 메뉴: `Run` → `Edit Configurations...`

2. **Environment Variables 추가**
   ```
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret
   KAKAO_CLIENT_ID=your_kakao_rest_api_key
   KAKAO_CLIENT_SECRET=your_kakao_client_secret
   UPLOAD_PATH=/Users/g0729/shop/
   ```

3. **적용 및 저장**

### macOS/Linux 터미널에서 설정

```bash
# ~/.zshrc 또는 ~/.bash_profile에 추가
export GOOGLE_CLIENT_ID="your_google_client_id"
export GOOGLE_CLIENT_SECRET="your_google_client_secret"
export KAKAO_CLIENT_ID="your_kakao_rest_api_key"
export KAKAO_CLIENT_SECRET="your_kakao_client_secret"
export UPLOAD_PATH="/Users/g0729/shop/"

# 적용
source ~/.zshrc  # 또는 source ~/.bash_profile
```

### Gradle로 실행 시

```bash
GOOGLE_CLIENT_ID=xxx GOOGLE_CLIENT_SECRET=xxx KAKAO_CLIENT_ID=xxx KAKAO_CLIENT_SECRET=xxx ./gradlew bootRun
```

---

## 🔵 Google OAuth 설정

### 1. Google Cloud Console 접속

https://console.cloud.google.com/

### 2. 프로젝트 생성 (없는 경우)

1. 상단의 프로젝트 선택 드롭다운 클릭
2. `새 프로젝트` 클릭
3. 프로젝트 이름 입력 (예: `shopping-mall`)
4. `만들기` 클릭

### 3. OAuth 동의 화면 구성

1. 좌측 메뉴: `API 및 서비스` → `OAuth 동의 화면`
2. 사용자 유형: `외부` 선택 → `만들기`
3. **앱 정보 입력:**
   - 앱 이름: `Shopping Mall`
   - 사용자 지원 이메일: (본인 이메일)
   - 개발자 연락처 정보: (본인 이메일)
4. `저장 후 계속` 클릭
5. **범위 추가:**
   - `범위 추가 또는 삭제` 클릭
   - 다음 범위 선택:
     - `.../auth/userinfo.email`
     - `.../auth/userinfo.profile`
   - `업데이트` 클릭
6. `저장 후 계속` 클릭
7. **테스트 사용자 추가:**
   - `ADD USERS` 클릭
   - 본인 Gmail 주소 입력
   - `추가` 클릭
8. `저장 후 계속` → `대시보드로 돌아가기`

### 4. OAuth 클라이언트 ID 만들기

1. 좌측 메뉴: `API 및 서비스` → `사용자 인증 정보`
2. `+ 사용자 인증 정보 만들기` → `OAuth 클라이언트 ID` 선택
3. **애플리케이션 유형:** `웹 애플리케이션`
4. **이름:** `Shopping Mall Web Client`
5. **승인된 리디렉션 URI 추가:**
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
   - 배포 후 추가:
   ```
   https://yourdomain.com/login/oauth2/code/google
   ```
6. `만들기` 클릭

### 5. 클라이언트 ID와 Secret 복사

```
클라이언트 ID: 123456789-abcdefg.apps.googleusercontent.com
클라이언트 보안 비밀: GOCSPX-xxxxxxxxxxxxx
```

**⚠️ 주의:** 절대로 GitHub에 커밋하지 마세요!

### 6. 환경 변수에 설정

```bash
export GOOGLE_CLIENT_ID="123456789-abcdefg.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="GOCSPX-xxxxxxxxxxxxx"
```

---

## 🟡 Kakao OAuth 설정

### 1. Kakao Developers 접속

https://developers.kakao.com/

### 2. 로그인

- 카카오 계정으로 로그인

### 3. 애플리케이션 추가

1. 상단 메뉴: `내 애플리케이션`
2. `애플리케이션 추가하기` 클릭
3. **앱 정보 입력:**
   - 앱 이름: `Shopping Mall`
   - 사업자명: (개인의 경우 본인 이름)
4. `저장` 클릭

### 4. 앱 키 확인

1. 생성된 애플리케이션 클릭
2. `앱 설정` → `앱 키` 메뉴
3. **REST API 키 복사**
   ```
   예: a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
   ```
   - 이것이 `KAKAO_CLIENT_ID`입니다

### 5. Client Secret 생성

1. `제품 설정` → `카카오 로그인` → `보안` 메뉴
2. **Client Secret 설정:**
   - `Client Secret` 코드 생성: `활성화` 상태로 변경
   - `생성` 버튼 클릭
3. **생성된 코드 복사**
   ```
   예: A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6
   ```
   - 이것이 `KAKAO_CLIENT_SECRET`입니다
4. **적용 상태:** `사용 필수` 선택
5. `저장` 클릭

### 6. Redirect URI 설정

1. `제품 설정` → `카카오 로그인` 메뉴
2. **활성화 설정:**
   - `카카오 로그인 활성화` 상태: `ON`
3. **Redirect URI 등록:**
   - `Redirect URI` 섹션에서 `Redirect URI 등록` 클릭
   - 입력:
   ```
   http://localhost:8080/login/oauth2/code/kakao
   ```
   - 배포 후 추가:
   ```
   https://yourdomain.com/login/oauth2/code/kakao
   ```
4. `저장` 클릭

### 7. 동의 항목 설정

1. `제품 설정` → `카카오 로그인` → `동의항목` 메뉴
2. **필수 동의 항목 설정:**
   - `닉네임`: 필수 동의
   - `카카오계정(이메일)`: 필수 동의
3. `저장` 클릭

### 8. 환경 변수에 설정

```bash
export KAKAO_CLIENT_ID="a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
export KAKAO_CLIENT_SECRET="A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6"
```

---

## 🚀 로컬 실행 확인

### 1. 환경 변수 확인

```bash
echo $GOOGLE_CLIENT_ID
echo $GOOGLE_CLIENT_SECRET
echo $KAKAO_CLIENT_ID
echo $KAKAO_CLIENT_SECRET
```

### 2. 애플리케이션 실행

```bash
cd main
./gradlew bootRun
```

또는 IntelliJ에서 `MainApplication` 실행

### 3. 브라우저에서 확인

```
http://localhost:8080/users/login
```

- `Google로 로그인` 버튼 확인
- `Kakao로 로그인` 버튼 확인

### 4. 테스트

#### Google 로그인 테스트
1. `Google로 로그인` 클릭
2. Google 계정 선택
3. 권한 동의
4. 로그인 성공 → 메인 페이지 리다이렉트

#### Kakao 로그인 테스트
1. `Kakao로 로그인` 클릭
2. 카카오 계정 로그인
3. 동의 화면에서 `동의하고 계속하기`
4. 로그인 성공 → 메인 페이지 리다이렉트

---

## 🔧 트러블슈팅

### 문제: "redirect_uri_mismatch" 에러

**원인:** Redirect URI가 일치하지 않음

**해결:**
1. 에러 메시지에서 실제 요청된 URI 확인
2. Google/Kakao Console에서 등록된 URI와 비교
3. 완전히 일치하도록 수정 (끝의 `/` 포함 여부 주의)

---

### 문제: "invalid_client" 에러

**원인:** Client ID 또는 Secret이 잘못됨

**해결:**
1. 환경 변수가 제대로 설정되었는지 확인
   ```bash
   echo $GOOGLE_CLIENT_ID
   ```
2. 공백이나 특수문자가 잘못 들어가지 않았는지 확인
3. Client Secret을 재발급

---

### 문제: 카카오 로그인 시 "insufficient_scope" 에러

**원인:** 동의 항목이 설정되지 않음

**해결:**
1. Kakao Developers → `동의항목`
2. `닉네임`, `카카오계정(이메일)` 필수 동의로 설정
3. 저장

---

### 문제: Google 로그인 시 "access_denied" 에러

**원인:** 테스트 사용자로 등록되지 않음 (앱이 게시되지 않은 경우)

**해결:**
1. Google Cloud Console → `OAuth 동의 화면`
2. `테스트 사용자` 섹션에서 사용자 추가
3. 또는 앱을 `게시` 상태로 변경 (심사 필요)

---

### 문제: 환경 변수가 적용되지 않음

**해결 (IntelliJ):**
1. `Run` → `Edit Configurations`
2. `Environment Variables` 확인
3. IntelliJ 재시작

**해결 (터미널):**
```bash
source ~/.zshrc
./gradlew clean bootRun
```

---

## 📝 체크리스트

### Google OAuth
- [ ] Google Cloud 프로젝트 생성
- [ ] OAuth 동의 화면 구성
- [ ] 테스트 사용자 추가
- [ ] OAuth 클라이언트 ID 생성
- [ ] Redirect URI 등록
- [ ] Client ID, Secret 환경 변수 설정
- [ ] 로그인 테스트 성공

### Kakao OAuth
- [ ] Kakao Developers 앱 생성
- [ ] REST API 키 확인
- [ ] Client Secret 생성
- [ ] 카카오 로그인 활성화
- [ ] Redirect URI 등록
- [ ] 동의 항목 설정 (닉네임, 이메일)
- [ ] Client ID, Secret 환경 변수 설정
- [ ] 로그인 테스트 성공

---

## 🔒 보안 권장사항

1. **절대로 Git에 커밋하지 마세요:**
   - `application.yml` (실제 값 포함)
   - Client ID, Secret

2. **환경 변수 사용:**
   - 개발: 로컬 환경 변수
   - 운영: AWS Secrets Manager, Parameter Store

3. **정기적으로 Secret 변경:**
   - 3-6개월마다 재발급

4. **프로덕션 배포 시:**
   - `security.debug: false`
   - HTTPS 필수
   - Redirect URI를 실제 도메인으로 변경

---

## 📚 참고 자료

- [Spring Security OAuth2 공식 문서](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html)
- [Google OAuth2 가이드](https://developers.google.com/identity/protocols/oauth2)
- [Kakao 로그인 가이드](https://developers.kakao.com/docs/latest/ko/kakaologin/common)

---

## ❓ 문의

문제가 해결되지 않으면 이슈를 생성해주세요.
