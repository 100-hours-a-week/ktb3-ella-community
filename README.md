<div align="center">
  <img width="100%" alt="D'velop Logo" src="https://github.com/user-attachments/assets/cd91f0fa-953e-43d8-8b54-b68268e6105d" />

  <br>
  <br>

  <h1>D'velop</h1>
  <p><strong>개발자들을 위한 코드 공유 및 이슈 토론 커뮤니티</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
    <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white"/>
    <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
    <img src="https://img.shields.io/badge/AWS%20S3-569A31?style=flat-square&logo=amazons3&logoColor=white"/>
    <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white"/>
  </p>
</div>

<br>

## 🧐 프로젝트 소개
**D'velop**은 개발자들이 자신의 코드나 이슈를 공유하고 피드백을 주고받을 수 있는 최적의 커뮤니티 환경을 제공합니다.
**Spring Boot**를 기반으로 안정적이고 확장 가능한 RESTful API 서버를 구축했습니다.

<br>

## ✨ 주요 기능

| 카테고리 | 상세 기능 |
|:---:|:---|
| **인증** | • JWT 기반 Access Token 발급 및 검증<br>• **Redis를 활용한 Refresh Token 관리 (RTR)** |
| **사용자** | • 회원가입, 내 정보 조회 및 수정<br>• 비밀번호 변경 및 회원 탈퇴 기능 |
| **게시판** | • **커서 기반 페이징**을 통한 무한 스크롤 최적화<br>• AWS S3 Presigned URL을 이용한 이미지 업로드<br>• 조회수 증가 및 다양한 정렬 필터(최신순, 조회순 등) 지원 |
| **인터랙션** | • 게시글 좋아요 및 취소 <br>• 댓글 작성, 수정, 삭제 |
| **문서화** | • Swagger (SpringDoc)를 이용한 API 명세서 자동화<br>• `GlobalExceptionHandler`를 통한 전역 예외 처리 및 공통 응답 포맷 |

<br>

## 🖼️ ERD
<div align="center">
  <img width="1580" height="682" alt="image" src="https://github.com/user-attachments/assets/b1658631-905c-404d-aa2a-7c66c1eedaa8" />
</div>
<br>

## 🛠 기술 스택 (Tech Stack)

* **Language:** Java 17
* **Framework:** Spring Boot 3.x, Spring Security, Spring Data JPA
* **Database:** MySQL, **Redis**
* **Infrastructure:** AWS S3 (Image)
* **Build Tool:** Gradle

<br>

## 🚀 Getting Started

이 프로젝트를 로컬 환경에서 실행하려면 아래 단계를 따라주세요.

<br>

## 🚀 Getting Started

이 프로젝트를 로컬 환경에서 실행하려면 아래 단계를 따라주세요.

### 1. Installation
레포지토리를 클론하고 의존성을 설치합니다.

```bash
# Repository Clone
git clone [https://github.com/your-repo/ktb3-community.git](https://github.com/your-repo/ktb3-community.git)
cd ktb3-community
