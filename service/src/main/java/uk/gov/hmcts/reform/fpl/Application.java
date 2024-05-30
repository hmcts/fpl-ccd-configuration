package uk.gov.hmcts.reform.fpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

//@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.fpl", "uk.gov.hmcts.reform.idam.client",
//    "uk.gov.hmcts.reform.document"})
//@EnableFeignClients(basePackages = {
//    "uk.gov.hmcts.reform.idam.client",
//    "uk.gov.hmcts.reform.rd.client",
//    "uk.gov.hmcts.reform.fnp.client",
//    "uk.gov.hmcts.reform.calendar.client",
//    "uk.gov.hmcts.reform.aac.client",
//    "uk.gov.hmcts.reform.am.client",
//    "uk.gov.hmcts.reform.ccd.client",
//    "uk.gov.hmcts.reform.authorisation",
//    "uk.gov.hmcts.reform.document",
//    "uk.gov.hmcts.reform.ccd.document"
//})
//@EnableRetry
//@EnableAsync
//@EnableCaching



@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam.client",
    "uk.gov.hmcts.reform.rd.client",
    "uk.gov.hmcts.reform.fnp.client",
    "uk.gov.hmcts.reform.calendar.client",
    "uk.gov.hmcts.reform.aac.client",
    "uk.gov.hmcts.reform.am.client",
    "uk.gov.hmcts.reform.ccd.client",
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.document",
    "uk.gov.hmcts.reform.ccd.document"
})
@ComponentScan
@EnableRetry
@EnableAsync
@EnableCaching
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, it's not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
