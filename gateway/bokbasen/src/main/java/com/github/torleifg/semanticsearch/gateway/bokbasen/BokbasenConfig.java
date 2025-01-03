package com.github.torleifg.semanticsearch.gateway.bokbasen;

import com.github.torleifg.semanticsearch.book.repository.ResumptionTokenRepository;
import com.github.torleifg.semanticsearch.book.service.MetadataGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty(prefix = "gateway", name = "type", havingValue = "bokbasen")
class BokbasenConfig {

    @Value("${bokbasen.audience}")
    private String audience;

    @Bean
    MetadataGateway metadataGateway(BokbasenClient bokbasenClient, BokbasenMapper bokbasenMapper, BokbasenProperties bokbasenProperties, ResumptionTokenRepository resumptionTokenRepository) {
        return new BokbasenGateway(bokbasenClient, bokbasenMapper, bokbasenProperties, resumptionTokenRepository);
    }

    @Bean
    BokbasenMapper bokbasenMapper(BokbasenProperties bokbasenProperties) {
        return new BokbasenDefaultMapper();
    }

    @Bean
    BokbasenClient bokbasenClient(RestClient restClient) {
        return new BokbasenClient(restClient);
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder, OAuth2AuthorizedClientManager authorizedClientManager) {
        final OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        requestInterceptor.setClientRegistrationIdResolver(it -> "bokbasen");

        return builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .requestInterceptor(requestInterceptor)
                .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService clientService) {
        final RestClientClientCredentialsTokenResponseClient tokenResponseClient = new RestClientClientCredentialsTokenResponseClient();

        tokenResponseClient.addParametersConverter(grantRequest -> {
            final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.set(OAuth2ParameterNames.AUDIENCE, audience);

            return parameters;
        });

        final OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(builder -> builder.accessTokenResponseClient(tokenResponseClient))
                .build();

        final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, clientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
