package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Api
@RestController
@RequestMapping("/callback/upload-c2")
public class UploadC2DocumentsController {
    private final ObjectMapper mapper;
    private final UserDetailsService userDetailsService;
    private final DateFormatterService dateFormatterService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private UploadC2DocumentsController(
        ObjectMapper mapper,
        UserDetailsService userDetailsService,
        DateFormatterService dateFormatterService,
        ApplicationEventPublisher applicationEventPublisher) {
        this.mapper = mapper;
        this.userDetailsService = userDetailsService;
        this.dateFormatterService = dateFormatterService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackrequest,
        @RequestHeader(value = "authorization") String authorization) {
        Map<String, Object> data = callbackrequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("c2DocumentBundle", buildC2DocumentBundle(caseData, authorization));
        data.remove("temporaryC2Document");

        return AboutToStartOrSubmitCallbackResponse.builder().data(data).build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {

        applicationEventPublisher.publishEvent(new C2UploadedEvent(callbackRequest, authorization, userId));
    }

    private List<Element<C2DocumentBundle>> buildC2DocumentBundle(CaseData caseData, String authorization) {
        List<Element<C2DocumentBundle>> c2DocumentBundle = defaultIfNull(caseData.getC2DocumentBundle(),
            Lists.newArrayList());

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        c2DocumentBundle.add(Element.<C2DocumentBundle>builder()
            .id(UUID.randomUUID())
            .value(C2DocumentBundle.builder()
                .author(userDetailsService.getUserName(authorization))
                .description(caseData.getTemporaryC2Document().getDescription())
                .document(caseData.getTemporaryC2Document().getDocument())
                .uploadedDateTime(dateFormatterService.formatLocalDateTimeBaseUsingFormat(zonedDateTime
                        .toLocalDateTime(), "h:mma, d MMMM yyyy"))
                .build())
            .build());

        return c2DocumentBundle;
    }
}
