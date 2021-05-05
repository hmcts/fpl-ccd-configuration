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
import uk.gov.hmcts.reform.fpl.model.NoticeOfChangeAnswersData;
import uk.gov.hmcts.reform.fpl.model.RespondentPolicyData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.RespondentRepresentationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final RespondentRepresentationService respondentRepresentationService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2982".equals(migrationId)) {
            run2982(caseDetails);
        }

        if ("FPLA-2961".equals(migrationId)) {
            run2961(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2982(CaseDetails caseDetails) {
        final List<Long> validCases = List.of(
            1598429153622508L,
            1615191831533551L,
            1594384486007055L,
            1601977974423857L,
            1615571327261140L,
            1615476016828466L,
            1616507805759840L,
            1610015759403189L,
            1615994076934396L,
            1611613172339094L,
            1612440806991994L,
            1607004182103389L,
            1617045146450299L,
            1612433400114865L,
            1615890702114702L,
            1610018233059619L
        );

        CaseData caseData = getCaseData(caseDetails);

        if (!validCases.contains(caseData.getId())) {
            throw new IllegalArgumentException("Invalid case Id");
        }

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            caseData.getAdditionalApplicationsBundle();

        if (additionalApplicationsBundle.stream()
            .noneMatch(this::checkNullIds)) {
            throw new IllegalArgumentException("No c2DocumentBundle or otherApplicationsBundle found with missing Id");
        }

        List<Element<AdditionalApplicationsBundle>> fixedAdditionalApplicationsBundle =
            additionalApplicationsBundle.stream().map(this::fixMissingIds).collect(Collectors.toList());

        caseDetails.getData().put("additionalApplicationsBundle", fixedAdditionalApplicationsBundle);
    }

    private void run2961(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isUnsupportedState(caseData.getState()) || containsNoCFields(caseData)) {
            throw new IllegalStateException(String.format("Migration failed on case %s: Unexpected migration",
                caseDetails.getId()));
        }

        if (caseData.getRespondents1().size() > 10) {
            throw new IllegalStateException(String.format("Migration failed on case %s: Case has %s respondents",
                caseDetails.getId(), caseData.getRespondents1().size()));
        }

        Map<String, Object> data = caseDetails.getData();

        data.putAll(respondentRepresentationService.generateForSubmission(caseData));
    }

    private boolean isUnsupportedState(State state) {
        List<State> unsupportedStates = List.of(
            State.OPEN,
            State.CLOSED,
            State.DELETED
        );

        return unsupportedStates.contains(state);
    }

    private boolean containsNoCFields(CaseData caseData) {
        RespondentPolicyData emptyRespondentPolicyData = RespondentPolicyData.builder().build();
        NoticeOfChangeAnswersData emptyNoCAnswerData = NoticeOfChangeAnswersData.builder().build();

        return !emptyNoCAnswerData.equals(caseData.getNoticeOfChangeAnswersData())
            || !emptyRespondentPolicyData.equals(caseData.getRespondentPolicyData());
    }

    private boolean checkNullIds(Element<AdditionalApplicationsBundle> documentBundle) {
        AdditionalApplicationsBundle value = documentBundle.getValue();
        C2DocumentBundle c2DocumentBundle = value.getC2DocumentBundle();
        OtherApplicationsBundle otherApplicationsBundle = value.getOtherApplicationsBundle();

        return (!isNull(c2DocumentBundle) && isNull(c2DocumentBundle.getId()))
            || (!isNull(otherApplicationsBundle) && isNull(otherApplicationsBundle.getId()));
    }

    private Element<AdditionalApplicationsBundle> fixMissingIds(Element<AdditionalApplicationsBundle> bundle) {
        C2DocumentBundle c2DocumentBundle = bundle.getValue().getC2DocumentBundle();
        OtherApplicationsBundle otherApplicationsBundle = bundle.getValue().getOtherApplicationsBundle();

        if (!isNull(c2DocumentBundle) && isNull(c2DocumentBundle.getId())) {
            bundle.getValue().getC2DocumentBundle().setId(UUID.randomUUID());
        }

        if (!isNull(otherApplicationsBundle) && isNull(otherApplicationsBundle.getId())) {
            bundle.getValue().getOtherApplicationsBundle().setId(UUID.randomUUID());
        }
        return bundle;
    }
}
