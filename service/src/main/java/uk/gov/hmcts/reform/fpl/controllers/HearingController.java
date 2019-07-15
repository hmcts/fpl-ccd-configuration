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


    // PROGRESS SO FAR ON FPLA-384
    // DONE - HEARING MIGRATION SERVICE IN DATA MIGRATION TOOL GIT REPO
    // TODO - FIX CCD UI HEARING PAGE (UNKNOWN FIELD IS THE LATEST ISSUE)
    // TODO - FINISH ANY OTHER ISSUES WITH THE NEW PLACEHOLDERS IN THE CCD UI FOR THE NEW HEARING
    // TODO - FIX CCD UI APPEARANCE - LOOKS ODD AT THE MOMENT
    // TODO - FIX HEARINGCONTROLLER TEST UNIT TEST FOR MID EVENT (ACTUALLY AN INTEGRATION TEST)
    // TODO - RUN MIGRATION TOOL THING LOCALLY
    // TODO - PDF CHANGES FOR HEARING
    // TODO - E2E TEST CHANGES FOR HEARING
    // TODO - SEE IF IT ALL WORKS
    // TODO - ALL OF THIS IS IN OTHER ENVIRONMENTS
    // TODO - ANYTHING ELSE TO DO WITH HEARING MIGRATION
    // TODO - UPDATE JIRA TICKET FPLA-384!!!
    @Autowired
    public HearingController(MapperService mapper,
                                HearingMigrationService hearingMigrationService) {
        this.mapper = mapper;
        this.hearingMigrationService = hearingMigrationService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        System.out.println("START: ABOUT TO START: HEARING");
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        System.out.println("END: ABOUT TO START: HEARING");
        return hearingMigrationService.setMigratedValue(caseDetails);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        System.out.println("START: MID EVENT: HEARING");
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        System.out.println("END: MID EVENT: HEARING");
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackrequest.getCaseDetails().getData())
            .errors(validate(caseDetails))
            .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();

        System.out.println("case data keys=" + caseDetails.getData().keySet());

        if (caseDetails.getData().containsKey("hearing")) {
            System.out.println("Validating hearing object");
            Map<String, Object> migratedHearingObject = (Map<String, Object>) caseDetails.getData().get("hearing");

            System.out.println("migratedHearingObject=" + migratedHearingObject.toString());
            MigratedHearing migratedHearing = mapper.mapObject((Map<String, Object>)
                migratedHearingObject, MigratedHearing.class);
            System.out.println("migratedHearing=" + migratedHearing.toString());

            if (migratedHearing.getHearingDescription() == null || migratedHearing.getHearingDescription().isBlank()) {
                errors.add("Hearing description cannot be empty");
            }

        } else {
            System.out.println("Validating hearing1 object");
            Map<String, Object> hearingData =
                (Map<String, Object>) defaultIfNull(caseDetails.getData().get("hearing1"), null);

            System.out.println("hearing1Data=" + hearingData.toString());
            Hearing hearing = mapper.mapObject(hearingData, Hearing.class);
            System.out.println("hearing1=" + hearing.toString());

            // only check for description with the post migration code.
            if (hearing.getDescription() == null || hearing.getDescription().isBlank()) {
                errors.add("Hearing description cannot be empty");
            }

            // new fields post migration
            // id, created by and created on are added in code.
            // id
            String newId = UUID.randomUUID().toString();
            hearingData.put("id", newId);
            // created by
            String userIdWhoCreatedThis = "not implemented yet";
            hearingData.put("createdBy", userIdWhoCreatedThis);
            // created on
            String currentDateAsAString = DateUtils.convertLocalDateTimeToString(LocalDateTime.now());
            hearingData.put("createdOn", currentDateAsAString);

        }

        return errors.build();
    }

}
