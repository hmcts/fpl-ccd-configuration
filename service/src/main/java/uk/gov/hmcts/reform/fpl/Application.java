package uk.gov.hmcts.reform.fpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import uk.gov.hmcts.reform.auth.checker.spring.AuthCheckerConfiguration;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;

@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam.client",
    "uk.gov.hmcts.reform.rd.client",
    "uk.gov.hmcts.reform.fnp.client",
    "uk.gov.hmcts.reform.calendar.client",
    "uk.gov.hmcts.reform.aac.client",
    "uk.gov.hmcts.reform.ccd.document.am",
    }, basePackageClasses = {CaseDocumentClientApi.class})
@ComponentScan(basePackages = "uk.gov.hmcts.reform", excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { AuthCheckerConfiguration.class}) })
@EnableRetry
@EnableAsync
@EnableCaching
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
