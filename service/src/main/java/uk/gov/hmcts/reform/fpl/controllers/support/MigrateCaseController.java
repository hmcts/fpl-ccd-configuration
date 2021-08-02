package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.CHILD;
import static uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator.NoticeOfChangeAnswersPopulationStrategy.BLANK;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private static final List<State> RESTRICTED_STATES = List.of(State.OPEN, State.RETURNED);
    private static final int MAX_CHILDREN = 15;

    private final NoticeOfChangeFieldPopulator populator;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        String migrationId = (String) caseDetails.getData().get(MIGRATION_ID_KEY);
        Long id = caseDetails.getId();

        log.info("Migration {id = {}, case reference = {}} started", migrationId, id);

        switch (migrationId) {
            case "FPLA-3132":
                run3132(caseDetails);
                break;
            default:
                log.error("Unhandled migration {}", migrationId);
        }

        log.info("Migration {id = {}, case reference = {}} finished", migrationId, id);

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3132(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        Long id = caseData.getId();
        State state = caseData.getState();
        if (RESTRICTED_STATES.contains(state)) {
            log.warn("Migration {id = FPLA-3132, case reference = {}} not migrating when state = {}", id, state);
            return;
        }
        int numChildren = caseData.getAllChildren().size();
        if (MAX_CHILDREN < numChildren) {
            log.warn(
                "Migration {id = FPLA-3132, case reference = {}} not migrating when number of children = {} (max = {})",
                id, numChildren, MAX_CHILDREN
            );
            return;
        }

        caseDetails.getData().putAll(populator.generate(caseData, CHILD, BLANK));
    }
}
