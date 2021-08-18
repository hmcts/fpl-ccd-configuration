package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(NoticeOfChangeController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfChangeControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final RespondentSolicitor MAIN_REPRESENTATIVE = RespondentSolicitor.builder()
        .firstName("x")
        .lastName("y")
        .email("z")
        .organisation(Organisation.builder()
            .organisationID("org 1234")
            .build())
        .build();
    private static final RespondentSolicitor ANOTHER_REPRESENTATIVE = RespondentSolicitor.builder()
        .firstName("a")
        .lastName("b")
        .email("c")
        .organisation(Organisation.builder()
            .organisationID("org 5678")
            .build())
        .build();


    NoticeOfChangeControllerAboutToSubmitTest() {
        super("noc-decision");
    }

    @Test
    void shouldTransferLegalCounselWhenSolicitorChanged() {
        List<Element<LegalCounsellor>> legalCounsellors = wrapElements(LegalCounsellor.builder().build());
        List<Element<LegalCounsellor>> differentLegalCounsellors = wrapElements(LegalCounsellor.builder().build());

        CaseData caseDataBefore = CaseData.builder()
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder().build())
                    .solicitor(ANOTHER_REPRESENTATIVE)
                    .legalCounsellors(legalCounsellors)
                    .build()
            ))
            .respondents1(wrapElements(
                Respondent.builder()
                    .solicitor(MAIN_REPRESENTATIVE)
                    .legalCounsellors(differentLegalCounsellors)
                    .build()
            ))
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder().build())
                    .solicitor(MAIN_REPRESENTATIVE)
                    .legalCounsellors(legalCounsellors)
                    .build()
            ))
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(toCallBackRequest(caseData, caseDataBefore)));

        assertThat(responseData.getChildren1()).hasSize(1)
            .first()
            .extracting(e -> e.getValue().getLegalCounsellors())
            .isEqualTo(differentLegalCounsellors);
    }
}
