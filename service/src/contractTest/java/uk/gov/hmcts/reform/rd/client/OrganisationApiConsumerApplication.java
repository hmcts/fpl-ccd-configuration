package uk.gov.hmcts.reform.rd.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.rd.client"
})
public class OrganisationApiConsumerApplication {
}
