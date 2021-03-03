package uk.gov.hmcts.reform.ccd.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.ccd.client"
})
public class CaseAccessDataStoreConsumerApplication {

}
