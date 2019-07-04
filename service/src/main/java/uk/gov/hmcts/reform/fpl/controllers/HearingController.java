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
import uk.gov.hmcts.reform.fpl.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @SuppressWarnings("unchecked")
    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
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

            // only check for description with the post migration code.
            if (hearing.getHearingDescription() == null || hearing.getHearingDescription().isBlank()) {
                errors.add("Hearing description cannot be empty");
            }

            // new fields post migration
            // id, created by and created on are added in code.
            // id
            String newId = UUID.randomUUID().toString();
            hearingData.put("id", newId);
            // created by
            String userIdWhoCreatedThis = Integer.toString(caseDetails.getLockedBy());
            hearingData.put("createdBy", userIdWhoCreatedThis);
            // created on
            String currentDateAsAString = DateUtils.convertLocalDateTimeToString(LocalDateTime.now());
            hearingData.put("createdOn", currentDateAsAString);

        }

        return errors.build();
    }

}
