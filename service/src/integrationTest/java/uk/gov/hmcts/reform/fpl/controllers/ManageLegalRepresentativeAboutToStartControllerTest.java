package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLegalRepresentativeAboutToStartControllerTest extends AbstractCallbackTest {

    private static final LegalRepresentative LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .fullName("John Smith")
        .role(LegalRepresentativeRole.EXTERNAL_LA_BARRISTER)
        .email("email")
        .organisation("organisation")
        .telephoneNumber("07500045455")
        .build();


    ManageLegalRepresentativeAboutToStartControllerTest() {
        super("manage-legal-representatives");
    }

    @Test
    void shouldPrePopulateWithEmptyElement() {

        CaseDetails caseDetailsBefore = CaseDetails.builder().data(Collections.emptyMap()).build();
        CaseDetails caseDetails = CaseDetails.builder().data(Collections.emptyMap()).build();

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(
            callbackRequest
        );

        CaseData actualCaseData = mapper.convertValue(actualResponse.getData(), CaseData.class);

        assertThat(unwrapElements(actualCaseData.getLegalRepresentatives())).isEqualTo(
            List.of(LegalRepresentative.builder().build())
        );
    }

    @Test
    void shouldReturnTheSameCaseIfAlreadyPopulated() {

        List<Element<LegalRepresentative>> legalRepresentatives = wrapElements(LEGAL_REPRESENTATIVE);

        CaseDetails caseDetailsBefore = buildCaseData(legalRepresentatives);
        CaseDetails caseDetails = buildCaseData(legalRepresentatives);

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(
            callbackRequest
        );

        CaseData actualCaseData = mapper.convertValue(actualResponse.getData(), CaseData.class);

        assertThat(actualCaseData.getLegalRepresentatives()).isEqualTo(legalRepresentatives);
    }

    private CallbackRequest buildCallbackRequest(CaseDetails originalCaseDetails, CaseDetails caseDetails) {
        return CallbackRequest.builder()
            .caseDetailsBefore(originalCaseDetails)
            .caseDetails(caseDetails)
            .build();
    }

    private CaseDetails buildCaseData(List<Element<LegalRepresentative>> legalRepresentatives) {
        return CaseDetails.builder()
            .data(Map.of(
                "legalRepresentatives", legalRepresentatives
            ))
            .build();
    }
}
