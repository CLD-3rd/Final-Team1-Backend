package likelion.team1.mindscape.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // CORS 설정을 URL 패턴별로 적용할 수 있게 해주는 클래스
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // CORS 정책을 설정하는 클래스의 인스턴스 생성
        CorsConfiguration config = new CorsConfiguration();

        // 쿠키를 포함한 크로스 도메인 요청을 허용할지 설정
        // true로 설정하면 클라이언트에서 쿠키를 보내고 받을 수 있음
        config.setAllowCredentials(true);

        // 모든 도메인에서의 요청을 허용 ("*")
        // 실제 운영환경에서는 특정 도메인만 허용하는 것이 좋음
        // 예: config.addAllowedOrigin("http://localhost:3000")
        config.addAllowedOrigin("http://localhost:3000");

        // 모든 헤더의 요청을 허용
        // 실제 운영환경에서는 필요한 헤더만 허용하는 것이 좋음
        // 예: Authorization, Content-Type 등
        config.addAllowedHeader("*");

        // 모든 HTTP 메서드를 허용 (GET, POST, PUT, DELETE 등)
        // 실제 운영환경에서는 필요한 메서드만 허용하는 것이 좋음
        config.addAllowedMethod("*");

        // /api/** 경로에 대해 위에서 설정한 CORS 정책을 적용
        // 예: /api/users, /api/auth 등의 모든 API 엔드포인트에 적용
        source.registerCorsConfiguration("/**", config);

        // 설정한 CORS 정책을 적용한 필터를 생성하여 반환
        // 이 필터는 스프링 시큐리티 필터 체인에 추가됨
        return new CorsFilter(source);
    }
}
