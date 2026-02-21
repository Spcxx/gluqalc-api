package pl.srozga.gluqalc_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ExternalApiConfig {
    @Value("${app.integration.open-food-facts.user-agent}")
    private String offUserAgent;
    @Value("${app.integration.open-food-facts.url}")
    private String offApiUrl;

    @Bean
    public RestClient offRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl(offApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, offUserAgent)
                .build();
    }
}
