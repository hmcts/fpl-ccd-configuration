package uk.gov.hmcts.reform.fnp.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.fnp.client"
})
public class FeesRegisterApiConsumerApplication {

}
