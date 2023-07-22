package com.example.connectionchecker.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public HttpRequest.Builder httpRequestBuilder() {
        return HttpRequest
                .newBuilder()
                .timeout(Duration.ofSeconds(10));
    }
}
