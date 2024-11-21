# 당근모아 (CarrotMoa) 🥕
지역 기반 중고거래, 숙소 예약, 커뮤니티 통합 플랫폼

## 📌 프로젝트 개요
- **프로젝트명**: 당근모아 (중고거래 플랫폼)
- **개발 기간**: 2024.09.24 ~ 2024.11.08 (약 6주)
- **팀 구성**: 총 5명 (팀장 1명, 팀원 4명)

## 🛠 기술 스택
### Backend
- Java 17
- Spring Boot 3.3.4
- Spring Security
- Spring Data JPA
- QueryDSL
- Jasypt

### Frontend
- HTML
- JavaScript
- Thymeleaf

### Database
- MySQL 8.0
- Redis 7.4

### DevOps
- AWS RDS
- AWS S3
- nGrinder

### Tools
- IntelliJ
- Gradle
- Git & GitHub
- Swagger
- DataGrip

## 👥 팀원 소개
- **팀장**: 로그인, 회원가입, 채팅 기능
- **본인**: 지도 커스텀, 통합 검색, 알림, 동네생활 게시판
- **팀원1**: 중고거래 기능
- **팀원2**: 숙소(호스트) 기능
- **팀원3**: 숙소(게스트) 기능

## 🚀 주요 기능
- 지역 기반 중고거래
- 숙소 예약 서비스
- 커뮤니티 게시판
- 실시간 채팅
- 통합 검색
- 알림 시스템

## 🔍 핵심 기술 및 구현 내용
### 1. 지도 커스텀
- Kakao Map API 활용
- Geolocation API 연동
- 동네 설정 및 거래 장소 지정 기능

### 2. 통합 검색
- MySQL Full-Text Index 적용
- 키워드 기반 멀티 도메인 검색

### 3. 실시간 알림
- Spring SSE (Server-Sent Events) 구현
- LocalStorage 활용한 최적화

### 4. 커뮤니티 게시판
- Spring Data JPA Slice 페이징
- Redis 캐싱을 통한 성능 최적화

## 🏆 기술적 성과
- Full-Text Index 성능 개선
- SSE 실시간 알림 최적화
- Redis 캐싱을 통한 TPS 120% 향상

## 🔜 향후 개선 계획
- 코드 리팩토링
- 추가 기능 고도화
