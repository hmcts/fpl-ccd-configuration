package uk.gov.hmcts.reform.am.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.am.client"
})

public class RoleAssignmentServiceConsumerApplication {
}
