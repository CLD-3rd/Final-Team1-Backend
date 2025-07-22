package likelion.team1.mindscape.security.jwt;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }
    // 실제 필터링 로직이 수행되는 메소드
    // JWT 토큰을 검증하고 인증 정보를 설정
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String header = request.getHeader(JwtProperties.HEADER_STRING);

        if(header == null || !header.startsWith(JwtProperties.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        System.out.println("header: " + header);


        // 헤더에서 토큰 값 추출
        String token = request.getHeader(JwtProperties.HEADER_STRING)
                .replace(JwtProperties.TOKEN_PREFIX, "");

        try {

            // JWT 검증 및 사용자 정보 추출
            String username = JWT.require(Algorithm.HMAC256(JwtProperties.SECRET))
                    .build()
                    .verify(token)
                    .getClaim("uname").asString();

           if(username != null) {
               // 추출된 사용자가 현재 DB에 등록된 사용자인지 확인
               User user = userRepository.findByUsername(username);

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        chain.doFilter(request, response);
    }
}
