package com.schoolerp.staff.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtExtractionFilter> jwtFilter() {
        FilterRegistrationBean<JwtExtractionFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JwtExtractionFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(1);
        return bean;
    }
}
