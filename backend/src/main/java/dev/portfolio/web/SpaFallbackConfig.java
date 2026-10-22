package dev.portfolio.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * 화면은 history 모드 라우터라 /cases/... 로 바로 들어오거나 새로고침하면 서버가 index.html 을 줘야 한다.
 * 실제 파일이 있으면 그 파일을, 없으면 index.html 을 준다. 단 /api 와 확장자가 있는 주소(없는 파일)는 404.
 */
@Configuration
public class SpaFallbackConfig implements WebMvcConfigurer {

    private static final Resource INDEX = new ClassPathResource("static/index.html");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String path, Resource location) throws IOException {
                        Resource found = super.getResource(path, location);
                        if (found != null) {
                            return found;
                        }
                        boolean isApi = path.startsWith("api/") || path.equals("api");
                        boolean looksLikeFile = path.substring(path.lastIndexOf('/') + 1).contains(".");
                        return isApi || looksLikeFile || !INDEX.exists() ? null : INDEX;
                    }
                });
    }
}
