package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.MessageJudgeOptions.REPLY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@ActiveProfiles("integration-test")
@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerMidEventTest extends AbstractControllerTest {
    private static final UUID DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();

    @SpyBean
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

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
    void shouldPopulateRelatedDocumentsFieldsWhenSendingANewJudicialMessage() {
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

        UUID notSelectedC2BundleId = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .c2DynamicList(DYNAMIC_LIST_ITEM_ID)
                .build())
            .c2DocumentBundle(List.of(
                element(DYNAMIC_LIST_ITEM_ID, selectedC2DocumentBundle),
                element(notSelectedC2BundleId, C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("other_c2.doc")
                        .build())
                    .build())
            ))
            .build();

        String expectedC2Label = mainC2Document.getFilename() + "\n" + supportingC2Document.getFilename();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("c2DynamicList"), DynamicList.class
        );

        DynamicList expectedDynamicList = buildDynamicList(0,
            Pair.of(DYNAMIC_LIST_ITEM_ID, "Application 1: null"),
            Pair.of(notSelectedC2BundleId, "Application 2: null")
        );

        assertThat(response.getData().get("relatedDocumentsLabel")).isEqualTo(expectedC2Label);
        assertThat(builtDynamicList).isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldPopulateRelatedDocumentsAndJudgeReplyFieldsWhenReplyingToAMessage() {
        JudicialMessage selectedJudicialMessage = JudicialMessage.builder()
            .sender("sender@gmail.com")
            .relatedDocumentFileNames("file1.doc")
            .messageHistory("message history")
            .latestMessage("Some note")
            .dateSent("16 December 2020")
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .messageJudgeOption(REPLY)
                .judicialMessageDynamicList(DYNAMIC_LIST_ITEM_ID)
                .build())
            .judicialMessages(List.of(
                element(DYNAMIC_LIST_ITEM_ID, selectedJudicialMessage)
            ))
            .build();

        JudicialMessage expectedJudicialMessage = JudicialMessage.builder()
            .relatedDocumentFileNames(selectedJudicialMessage.getRelatedDocumentFileNames())
            .recipient(selectedJudicialMessage.getSender())
            .replyFrom(ctscEmailLookupConfiguration.getEmail())
            .replyTo("sender@gmail.com")
            .subject(selectedJudicialMessage.getSubject())
            .messageHistory(selectedJudicialMessage.getMessageHistory())
            .latestMessage("")
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        DynamicList judicialMessageDynamicList = mapper.convertValue(
            response.getData().get("judicialMessageDynamicList"), DynamicList.class
        );

        JudicialMessage judicialMessageReply = mapper.convertValue(
            response.getData().get("judicialMessageReply"), JudicialMessage.class
        );

        DynamicList expectedJudicialMessageDynamicList = buildDynamicList(
            0, Pair.of(DYNAMIC_LIST_ITEM_ID, "16 December 2020")
        );

        assertThat(judicialMessageReply).isEqualTo(expectedJudicialMessage);
        assertThat(judicialMessageDynamicList).isEqualTo(expectedJudicialMessageDynamicList);
    }
}
