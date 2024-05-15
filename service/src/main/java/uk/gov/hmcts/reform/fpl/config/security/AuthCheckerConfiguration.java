package uk.gov.hmcts.reform.fpl.config.security;

import com.google.common.collect.ImmutableSet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "security")
public class AuthCheckerConfiguration {

    List<String> authorisedRoles = new ArrayList<>();

    public List<String> getAuthorisedRoles() {
        return authorisedRoles;
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
        log.info(String.format("Configured authorised roles: %s", String.join(", ", authorisedRoles)));
        return any -> ImmutableSet.copyOf(authorisedRoles);
    }

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
        return any -> Optional.empty();
    }

    @Bean
    @ConditionalOnProperty(
        value = "ssl.verification.enable",
        havingValue = "false",
        matchIfMissing = true)
    public HttpClient customUserTokenParserHttpClient()
        throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .disableCookieManagement()
            .disableAuthCaching()
            .useSystemProperties();

        TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;
        HostnameVerifier allowAllHostnameVerifier = (hostName, session) -> true; // NOSONAR
        SSLContext sslContextWithoutValidation = SSLContexts.custom()
            .loadTrustMaterial(null, acceptingTrustStrategy)
            .build();

        SSLConnectionSocketFactory allowAllSslSocketFactory = new SSLConnectionSocketFactory(
            sslContextWithoutValidation,
            allowAllHostnameVerifier);

        httpClientBuilder.setSSLSocketFactory(allowAllSslSocketFactory);

        // also disable SSL validation for plain java HttpsURLConnection
        HttpsURLConnection.setDefaultHostnameVerifier(allowAllHostnameVerifier);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContextWithoutValidation.getSocketFactory());

        return httpClientBuilder.build();
    }

}
