package likelion.team1.mindscape.security.jwt;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion.team1.mindscape.entity.User;
import likelion.team1.mindscape.repository.UserRepository;
import likelion.team1.mindscape.service.PrincipalDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.Date;


public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, JwtProperties jwtProperties) {
        super(authenticationManager);
        this.userRepository = userRepository;
        this.jwtProperties = jwtProperties;
    }
    // 실제 필터링 로직이 수행되는 메소드
    // JWT 토큰을 검증하고 인증 정보를 설정
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        //jwt 토큰 가져오기
        String accessToken = null;
        String refreshToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(JwtProperties.ACCESS_TOKEN_STRING)) {
                    accessToken = cookie.getValue();
                }
                if (cookie.getName().equals(JwtProperties.REFRESH_TOKEN_STRING)) {
                    refreshToken = cookie.getValue();
                }
            }
        } else {
            chain.doFilter(request, response);
            return;
        }

        try {
            // JWT 검증 및 사용자 정보 추출
            String accountId = JWT.require(Algorithm.HMAC512(jwtProperties.getSECRET()))
                    .build()
                    .verify(accessToken)
                    .getSubject();

           if(accountId != null) {
               // 추출된 사용자가 현재 DB에 등록된 사용자인지 확인
               User user = userRepository.findByAccountId(accountId);

               // 사용자 정보
               PrincipalDetails principalDetails = new PrincipalDetails(user);

               Authentication authentication =
                       new UsernamePasswordAuthenticationToken(
                               principalDetails, // 사용자 정보
                               null, // 인증 완료로 비밀번호 불필요
                               principalDetails.getAuthorities()); // 권한 정보

               //SecurityContext에 인증 정보 저장
               SecurityContextHolder.getContext().setAuthentication(authentication);
           }
        } catch (TokenExpiredException e){ // 토큰이 만료된 경우

            if(refreshToken != null) {

                String accountId = JWT.require(Algorithm.HMAC512(jwtProperties.getSECRET()))
                        .build()
                        .verify(refreshToken)
                        .getSubject();

                User user = userRepository.findByAccountId(accountId);

                if(user.vailateRefreshToken(refreshToken)) {

                    String newAccessToken = JWT.create()
                            .withSubject(user.getAccountId())
                            .withClaim("uid", user.getId())
                            .withClaim("uname", user.getUsername())
                            .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getACCESS_TOKEN_EXPIRATION()))
                            .sign(Algorithm.HMAC512(jwtProperties.getSECRET()));

                    String accessCookie = String.format(
                            "AccessToken=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                            newAccessToken,jwtProperties.getACCESS_TOKEN_EXPIRATION());
                    response.addHeader("Set-Cookie", accessCookie);

                    // 인증 정보 설정
                    PrincipalDetails principalDetails = new PrincipalDetails(user);

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            principalDetails,
                            null,
                            principalDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                }

            } else {
                chain.doFilter(request, response);
                return;
            }

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            throw new RuntimeException(ex);
        }

        chain.doFilter(request, response);
    }
}
