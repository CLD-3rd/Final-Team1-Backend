package likelion.team1.mindscape.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import likelion.team1.mindscape.dto.LoginRequestDTO;
import likelion.team1.mindscape.entity.User;
import likelion.team1.mindscape.repository.UserRepository;
import likelion.team1.mindscape.service.PrincipalDetails;
import likelion.team1.mindscape.service.RedisRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    //인증(Authentication)을 처리하는 핵심 인터페이스
    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;
    private final RedisRefreshTokenService redisRefreshTokenService;


//    // 생성자에서 로그인 URL 설정
//    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
//        super(authenticationManager);
//        // 로그인 URL 설정 - 기본값은 /login
//        setFilterProcessesUrl("/login");  // 여기서 URL 변경 가능
//    }


    // 인증 시도 메소드
    // 클라이언트로부터 받은 인증 정보로 로그인을 시도
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        deleteExistingTokenCookies(response);

        // 1. HTTP 요청 본문을 DTO로 변환
        ObjectMapper mapper = new ObjectMapper();
        LoginRequestDTO loginRequestDTO = null;

        try {
            loginRequestDTO = mapper.readValue(request.getInputStream(), LoginRequestDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 2. 인증 토큰 생성 (아직 인증된거 아님)
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getAccountId(), loginRequestDTO.getPassword());

        // 3. 인증 검토 -> 여기서 인증 처리
        Authentication authentication = authenticationManager.authenticate(authToken);


        return authentication;
    }

    // JWT 토큰 발급
    // 인증 성공 시, 해당 메소드 호출
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        deleteExistingTokenCookies(response);

        // accessToken 생성
        String accessToken = JWT.create()
                .withSubject(principalDetails.getAccountId())
                .withClaim("uid", principalDetails.getUser().getId())
                .withClaim("uname", principalDetails.getUser().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getACCESS_TOKEN_EXPIRATION()))
                .sign(Algorithm.HMAC512(jwtProperties.getSECRET()));


        String refreshToken = JWT.create()
                .withSubject(principalDetails.getAccountId())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getREFRESH_TOKEN_EXPIRATION()))
                .sign(Algorithm.HMAC512(jwtProperties.getSECRET()));



        redisRefreshTokenService.saveRefreshToken(principalDetails.getAccountId(), refreshToken,
                                                    jwtProperties.getREFRESH_TOKEN_EXPIRATION());



        String accessCookie = String.format(
                "AccessToken=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                accessToken,jwtProperties.getACCESS_TOKEN_EXPIRATION());


        String refreshCookie = String.format(
                "RefreshToken=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                refreshToken, jwtProperties.getREFRESH_TOKEN_EXPIRATION());



        response.addHeader("Set-Cookie", accessCookie);
        response.addHeader("Set-Cookie", refreshCookie);
    }

    // 기존 토큰 쿠키 삭제 메서드
    private void deleteExistingTokenCookies(HttpServletResponse response) {
        // AccessToken 만료 처리
        String expiredAccessToken = "AccessToken=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None";

        // RefreshToken 만료 처리
        String expiredRefreshToken = "RefreshToken=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None";

        // 응답 헤더로 만료 쿠키 추가
        response.addHeader("Set-Cookie", expiredAccessToken);
        response.addHeader("Set-Cookie", expiredRefreshToken);
    }
}
