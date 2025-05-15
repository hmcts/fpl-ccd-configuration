package uk.gov.hmcts.reform.fpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataClientAutoConfiguration;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.fpl"},
    scanBasePackageClasses = {DocumentUploadClientApi.class, IdamClient.class})
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam.client",
    "uk.gov.hmcts.reform.rd.client",
    "uk.gov.hmcts.reform.fnp.client",
    "uk.gov.hmcts.reform.calendar.client",
    "uk.gov.hmcts.reform.am.client",
    "uk.gov.hmcts.reform.aac.client",
    "uk.gov.hmcts.reform.ccd.document.am.feign"})
@ImportAutoConfiguration(CoreCaseDataClientAutoConfiguration.class)
@EnableRetry
@EnableAsync
@EnableCaching
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, it's not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
