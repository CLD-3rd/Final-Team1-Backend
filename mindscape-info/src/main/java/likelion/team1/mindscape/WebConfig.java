package likelion.team1.mindscape;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${server.frontend.pageUrl}")
    private String pageUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 요청 경로에 대해
                .allowedOrigins(pageUrl) //서비스 페이지
                .allowedMethods("*") // GET, POST, PUT 등 모두 허용
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 인증 정보 포함 허용 (JWT 등)
    }
}
