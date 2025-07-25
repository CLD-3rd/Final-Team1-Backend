package likelion.team1.mindscape.security;


import likelion.team1.mindscape.repository.UserRepository;
import likelion.team1.mindscape.security.jwt.JwtAuthenticationFilter;
import likelion.team1.mindscape.security.jwt.JwtAuthorizationFilter;
import likelion.team1.mindscape.security.jwt.JwtProperties;
import likelion.team1.mindscape.security.oauth.OAuth2FailureHandler;
import likelion.team1.mindscape.security.oauth.OAuth2SuccessHandler;
import likelion.team1.mindscape.service.OAuth2UserService;
import likelion.team1.mindscape.service.RedisRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration  // 스프링의 설정 클래스임을 나타내는 어노테이션
@EnableWebSecurity  // Spring Security 설정을 활성화하는 어노테이션
@RequiredArgsConstructor  // final 필드에 대한 생성자를 자동으로 생성하는 롬복 어노테이션
public class SecurityConfig {

    private final UserRepository userRepository;
    private final CorsConfig corsConfig;
    private final JwtProperties jwtProperties;
    private final RedisRefreshTokenService redisRefreshTokenService;
    private final OAuth2UserService oAuth2UserService;



    //AuthenticationManager 빈을 생성하는 메소드
    //스프링 시큐리티의 인증을 담당하는 매니저를 설정
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //비밀번호 암호화를 위한 인코더를 빈으로 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //스프링 시큐리티의 필터 체인을 구성하는 메소드
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   OAuth2SuccessHandler oAuth2SuccessHandler,
                                                   OAuth2FailureHandler oAuth2FailureHandler,
                                                   AuthenticationManager authenticationManager) throws Exception {
        http
                .addFilter(corsConfig.corsFilter())
                // JWT 인증 필터 추가
                .addFilter(new JwtAuthenticationFilter(authenticationManager, jwtProperties, redisRefreshTokenService))

                // JWT 인가 필터 추가
                .addFilter(new JwtAuthorizationFilter(authenticationManager, userRepository, jwtProperties))

                // google OAuth 로그인
                .oauth2Login(oauth2 -> oauth2.userInfoEndpoint(
                                userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )

                // CSRF 보호 비활성화 (JWT 사용으로 불필요) 왜지?
                // JWT를 사용하는 REST API에서는 CSRF 공격 방지가 불필요
                // 토큰 기반 인증이 CSRF 공격을 방지할 수 있기 때문
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 설정 (JWT는 세션을 사용하지 않음)
                // JWT는 상태를 저장하지 않는(stateless) 방식이므로 세션이 불필요
                // 서버의 확장성과 성능 향상을 위해 세션을 사용하지 않음
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 폼 로그인 비활성화
                // REST API에서는 폼 로그인 방식을 사용하지 않음
                // JWT 토큰 기반의 인증을 사용하므로 불필요
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화
                // 기본 인증은 보안에 취약하고 JWT를 사용하므로 불필요
                // 매 요청마다 인증 정보를 보내는 방식이라 보안에 취약
                .httpBasic(AbstractHttpConfigurer::disable)


                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/**").authenticated() //모든 API 호출 유저 인증 필요.
                        .anyRequest().permitAll()); // 이 외 요청은 권한 필요 X


        return http.build();
    }

}
