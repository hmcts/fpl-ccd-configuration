package uk.gov.hmcts.reform.fpl;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
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
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "security")
public class AuthCheckerConfiguration {

    List<String> authorisedServices;
    List<String> authorisedRoles = new ArrayList<>();

    public List<String> getAuthorisedServices() {
        return authorisedServices;
    }

    public void setAuthorisedServices(List<String> authorisedServices) {
        this.authorisedServices = authorisedServices;
    }

    public List<String> getAuthorisedRoles() {
        return authorisedRoles;
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return any -> ImmutableSet.copyOf(authorisedServices);
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
        return any -> ImmutableSet.copyOf(authorisedRoles);
    }

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {

        return any -> Optional.empty();
    }

    @Bean(name = {"serviceTokenParserHttpClient", "userTokenParserHttpClient"})
    @ConditionalOnProperty(
            value = "ssl.verification.enable",
            havingValue = "false",
            matchIfMissing = true)
    public HttpClient userTokenParserHttpClient()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .disableCookieManagement()
                .disableAuthCaching()
                .useSystemProperties();

        TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;
        // ignore Sonar's weak hostname verifier as we are deliberately disabling SSL verification
        HostnameVerifier allowAllHostnameVerifier = (hostName, session) -> true; // NOSONAR
        SSLContext sslContextWithoutValidation = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory allowAllSslSocketFactory = new SSLConnectionSocketFactory(
                sslContextWithoutValidation,
                allowAllHostnameVerifier);

        httpClientBuilder.setSSLSocketFactory(allowAllSslSocketFactory);

        // also disable SSL valiation for plain java httpurlconnection
        HttpsURLConnection.setDefaultHostnameVerifier(allowAllHostnameVerifier);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContextWithoutValidation.getSocketFactory());

        return httpClientBuilder.build();
    }
}
