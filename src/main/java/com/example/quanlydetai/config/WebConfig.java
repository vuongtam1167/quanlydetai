package com.example.quanlydetai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LayoutModelInterceptor layoutModelInterceptor;

    public WebConfig(LayoutModelInterceptor layoutModelInterceptor) {
        this.layoutModelInterceptor = layoutModelInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(layoutModelInterceptor)
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/api/**");
    }
}
