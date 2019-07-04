package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.migration.MigratedHearing;
import uk.gov.hmcts.reform.fpl.service.HearingMigrationService;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Api
@RestController
@RequestMapping("/callback/enter-hearing")
public class HearingController {

    private final MapperService mapper;
    private final HearingMigrationService hearingMigrationService;


    @Autowired
    public HearingController(MapperService mapper,
                                HearingMigrationService hearingMigrationService) {
        this.mapper = mapper;
        this.hearingMigrationService = hearingMigrationService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return hearingMigrationService.setMigratedValue(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackrequest.getCaseDetails().getData())
            .errors(validate(caseDetails))
            .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();

        Map<String, Object> hearingData =
            (Map<String, Object>) defaultIfNull(caseDetails.getData().get("hearing"), null);

        if (caseDetails.getData().containsKey("hearing1")) {

            Map<String, Object> migratedHearingObject = (Map<String, Object>) caseDetails.getData().get("hearing1");

            MigratedHearing migratedHearing = mapper.mapObject((Map<String, Object>)
                migratedHearingObject.get("value"), MigratedHearing.class);

            if (migratedHearing.getHearingDescription() == null || migratedHearing.getHearingDescription().isBlank()) {
                errors.add("Hearing description cannot be empty");
            }

        } else {
            Hearing hearing = mapper.mapObject(hearingData, Hearing.class);
            if (hearing.getHearingDescription() == null || hearing.getHearingDescription().isBlank()) {
                errors.add("Hearing description cannot be empty");
            }
        }
        return errors.build();
    }

}
