package likelion.team1.mindscape.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // CORS 설정을 URL 패턴별로 적용할 수 있게 해주는 클래스
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();

        // allowCredentials를 true로 설정하면 allowedOrigins에는 *를 사용할 수 없습니다.
        config.setAllowCredentials(true);

        // 구체적인 origin을 설정
        config.addAllowedOriginPattern("*");  // 개발 환경에서만 사용하세요

        // 모든 헤더와 메소드 허용
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        // preflight 요청의 캐시 시간을 1시간으로 설정
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);

    }
}
