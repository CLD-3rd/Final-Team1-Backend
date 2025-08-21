
# 🌙 MindScape

<p align="center">
  <img src="images/mindscape.png" width="750"/>
</p>

### 🎯 성향으로 연결되는 콘텐츠 추천의 시작

<div align="left" style="border: 1px solid #ddd; padding: 16px; border-radius: 8px; background-color: #f9f9f9;">
  <b>MindScape</b>는 사용자의 성향을 분석하여 영화, 음악, 책 등 맞춤형 콘텐츠를 추천하는 성향 기반 콘텐츠 추천 플랫폼입니다.<br/>
  간단한 DISC 테스트를 통해 성향을 파악하고, 유형별로 최적화된 콘텐츠를 제공합니다.<br/><br/>
  <b> 제작 기간:</b> 2025/07/16 ~ 2025/08/21
  <b> Backend GitHub:</b> https://github.com/CLD-3rd/Final-Team1-Backend.git
  <b> Frontend GitHub:</b> https://github.com/CLD-3rd/Final-Team1-Frontend.git
</div>



<br>

## 1️⃣ 팀원 구성 및 역할
<table>
  <tr>
    <th width="200">문지현<br/><sub>팀장</sub></th>
    <th width="200">백주희<br/><sub>부팀장</sub></th>
    <th width="200">김종훈<br/><sub>팀원</sub></th>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/door-JH">
        <img src="https://github.com/door-JH.png" width="100" style="border-radius:50%"/><br/>
      </a>
      <br/>
       Frontend infra <br/>
       Log <br/>
       Auth server <br/>
       회원가입 페이지<br/>
       로그인 페이지<br/>
       공유
    </td>
    <td align="center">
      <a href="https://github.com/ju0204">
        <img src="https://github.com/ju0204.png" width="100" style="border-radius:50%"/><br/>
      </a>
      <br/>
      Backend infra<br/>
      Argocd<br/>
      Alert<br/>
      Hpa, Karpenter<br/>
      Service server<br/>
      결과페이지<br/>
      마이페이지<br/>
    </td>
    <td align="center">
      <a href="https://github.com/yhy0009">
        <img src="https://github.com/yhy0009.png" width="100" style="border-radius:50%"/><br/>
      </a>
      <br/>
      Backend infra<br/>
      Argocd<br/>
      Hpa<br/>
      Info server<br/>
      결과 페이지<br/>
      테스트 페이지<br/>
      인기 컨텐츠<br/>
    </td>
  </tr>
  <tr>
    <th width="200">장민지<br/><sub>팀원</sub></th>
    <th width="200">정영민<br/><sub>팀원</sub></th>
    <th width="200">이예지<br/><sub>팀원</sub></th>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/rossenzii">
        <img src="https://github.com/rossenzii.png" width="100" style="border-radius:50%"/><br/>
      </a>
      <br/>
      Backend Terraform<br/>
      Argocd<br/>
      Cicd<br/>
      Log<br/>
      Grafana<br/>
      Service server<br/>
    </td>
    <td align="center">
      <a href="https://github.com/jyoungmin-com">
        <img src="https://github.com/jyoungmin-com.png" width="100" style="border-radius:50%"/><br/>
      </a>
      <br/>
      Frontend infra <br/>
      Grafana<br/>
      K6<br/>
      Service server<br/>
      인기 컨텐츠<br/>
    </td>
    <td align="center">
      <a href="https://github.com/yejipo">
        <img src="https://github.com/yejipo.png" width="100" style="border-radius:50%"/><br/>
      </a>
      <br/>
      Backend Terraform<br/>
      Grafana<br/>
      Info server<br/>
      검색<br/>
      Dark 모드 <br/>
    </td>
  </tr>
</table>

<br>

## 2️⃣ 기술 스택

| 구분               | 기술 스택 |
|--------------------|-----------|
| 운영 도구     | Termius, STS (Spring Tool Suite), Visual Studio Code, Intellij IDEA, Cursor |
| 백엔드        | Java 17, Spring Boot 3, Spring Data JPA, Maven |
| 인프라        | AWS, Terraform |
| 컨테이너 / 배포 | Docker, Kubernetes, Helm, ArgoCD |
| CI/CD & GitOps | GitHub Actions, ArgoCD |
| 모니터링 / 테스트 | Prometheus, Grafana, k6, Loki, Promtail, InfluxDB, Discord |
| 협업 툴 | Discord, Gather |

<br>

## 3️⃣ 앱 주요 기능


### 1. 회원가입 · 로그인 

<div align="center">
  <img src="images/signup.gif" alt="회원가입" width="420" style="display:inline-block; margin: 0 20px;">
  <img src="images/login.gif" alt="로그인" width="420" style="display:inline-block; margin: 0 20px;">
</div>


- 회원가입시 중복 아이디 방지를 위한 중복 확인 기능 추가했습니다.
- 중복 확인 검사가 통과 되어야만 회원가입 버튼이 활성화 됩니다.
- Spring Security 기반 Stateless JWT 인증 시스템을 도입하고 구글 소셜 로그인 구현하였습니다.
  
   
### 2. DISC 테스트 

<div align="center">
  <img src="images/moveTestpage.gif" alt="disc 테스트" width="420" style="display:inline-block; margin: 0 20px;">
  <img src="images/moveResultpage.gif" alt="결과페이지 이동" width="420" style="display:inline-block; margin: 0 20px;">
</div>
 

- 사용자는 DISC 테스트 검사를 진행합니다.
- 답변 선택 시 페이지가 이동되도록 React Router를 활용한 client-side routing을 구현하였습니다.
- 결과 확인하러가기 버튼을 눌러 결과 페이지로 이동합니다.


### 3. 결과페이지


<img src="images/resultpage.gif" alt="결과페이지" width="480">

- DISC 테스트 결과에 따라 분석된 성향 유형과 그에 맞는 콘텐츠 추천 리스트를 보여줍니다.
- 영화, 책, 음악 콘텐츠를 유형별로 분류하여 제공합니다.
- 추천 결과를 redis에 저장하고 히스토리 열람을 위해 AOF 기능을 사용하였습니다.


### 4. 공유


 <img src="images/share.gif" alt="공유" width="480"> 

- 테스트 결과를 링크로 공유하여 외부에서 해당 결과를 확인할 수 있습니다.
- 사용자별 추천 결과를 url에 매핑하여 외부에서 접근하도록 동적 라우팅 기반 공유 기능을 구현하였습니다.


### 5.마이페이지


 <img src="images/mypage.gif" alt="마이페이지" width="480"> 

- 유저가 테스트를 통해 추천받은 영화, 책, 음악 등의 콘텐츠 이력을 한눈에 확인할 수 있습니다.
- 마이페이지 진입 시 DB에 저장된 테스트 데이터를 불러오고, 해당 userId의 모든 테스트 정보를 불러와서 json 형식으로 프론트엔드에 전달합니다.
- 유형별 히스토리와 나의 취향 데이터를 기반으로 개인화된 기록을 제공합니다.

### 6. 인기컨텐츠


<img src="images/ranking.gif" alt="인기컨텐츠" width="480"> 

- 유형별로 유저들이 가장 많이 추천한 인기 콘텐츠를 확인할 수 있습니다.
- Info service에 유저의 성향 타입별 test id 목록을 가져와서 각각의 항목(책, 음악, 영화)에 대해 sql에서 제목별 개수 카운트하여 내림차순으로 정렬합니다.
- 각 항목 고유 id로 sql에서 상세 정보 가져온 후 보여줍니다
- DISC 성향에 따라 어떤 콘텐츠가 많이 선택되었는지 시각적으로 확인할 수 있습니다.

### 7. 검색


 <img src="images/search.gif" alt="검색" width="480"> 

- 사용자는 테스트 추천 외에도 원하는 콘텐츠(책, 영화, 음악 등)를 직접 검색할 수 있는 기능을 사용할 수 있습니다.
- 외부 API(TMDB, Kakao Books, Last.fm 등)를 연동해 검색 결과를 가져옵니다.
- 검색 결과는 카드 형식으로 보여주며, 각 콘텐츠에 대한 상세 정보까지 확인 가능합니다.
 

### 8. dark 모드


 <img src="images/dark.gif" alt="검색" width="480"> 
  

- 모든 페이지에서 라이트 모드일 때는 기존 디자인이 유지되며, 다크 모드일 때만 배경·텍스트·카드 색상이 어둡게 변경됩니다.
- TailwindCSS `dark:` prefix 활용해 다크모를 적용했습니다.

## 4️⃣ 아키텍처

<p align="center">
  <img src="images/architecture.png" width="750"/>
</p>

- Terraform으로 AWS 인프라(VPC, EKS, RDS 등)를 구성하고 Terraform Helm provider로 Prometheus·Grafana·ArgoCD를 배포했으며, Karpenter 설치와 IRSA/IAM 기반 인증·권한 설정까지 포함했습니다.
- GitHub Actions와 Argo CD로 MSA를 자동 배포했으며, ALB Controller, ingress는 Argo CD의 yaml로 설치·관리했습니다.
- InfluxDB, Prometheus, Loki와 Discord 알림을 활용해 로그 및 모니터링 시스템을 구축했습니다.
