package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerMidEventTest extends AbstractControllerTest {
    MessageJudgeControllerMidEventTest() {
        super("message-judge");
    }

    @Test
    void shouldSetHearingLabelWhenNextHearingExists() {
        HearingBooking expectedNextHearing = HearingBooking.builder()
            .startDate(now().plusDays(1))
            .type(CASE_MANAGEMENT)
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .startDate(now().plusDays(3))
                    .type(FINAL)
                    .build()),
                element(expectedNextHearing),
                element(HearingBooking.builder()
                    .startDate(now().plusDays(5))
                    .type(ISSUE_RESOLUTION)
                    .build())
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getData().get("nextHearingLabel")).isEqualTo(
            String.format("Next hearing in the case: %s", expectedNextHearing.toLabel()));
    }

    @Test
    void shouldPopulateRelatedDocumentsFields() {
        UUID selectedC2Id = UUID.randomUUID();

        DocumentReference mainC2Document = DocumentReference.builder()
            .filename("c2.doc")
            .build();

        DocumentReference supportingC2Document = DocumentReference.builder()
            .filename("additional_c2_document.doc")
            .build();

        C2DocumentBundle selectedC2DocumentBundle = C2DocumentBundle.builder()
            .document(mainC2Document)
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(supportingC2Document)
                    .build())))
            .build();

        CaseData caseData = CaseData.builder()
            .c2DynamicList(selectedC2Id)
            .c2DocumentBundle(List.of(
                element(selectedC2Id, selectedC2DocumentBundle),
                element(UUID.randomUUID(), C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("other_c2.doc")
                        .build())
                    .build())
            ))
            .build();

        String expectedC2Label = mainC2Document + "\n" + supportingC2Document;

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("c2DynamicList"), DynamicList.class
        );

        assertThat(response.getData().get("relatedDocumentsLabel")).isEqualTo(expectedC2Label);
        assertThat(builtDynamicList).isEqualTo(caseData.buildC2DocumentDynamicList(selectedC2Id));
    }
}
