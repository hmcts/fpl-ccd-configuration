package uk.gov.hmcts.reform.fpl.controllers;

import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients("uk.gov.hmcts.reform.rd.client")
@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {
}
