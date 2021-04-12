package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.MessageJudgeOptions.REPLY;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerMidEventTest extends AbstractCallbackTest {
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
        DocumentReference mainDocument = DocumentReference.builder()
            .filename("c2.doc")
            .build();

        DocumentReference supportingDocument = DocumentReference.builder()
            .filename("supporting.doc")
            .build();

        SupportingEvidenceBundle supportingEvidenceBundle = SupportingEvidenceBundle.builder()
            .name("Supporting evidence")
            .document(supportingDocument)
            .build();

        UUID notSelectedBundleId = randomUUID();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles = List.of(
            element(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .id(DYNAMIC_LIST_ITEM_ID)
                    .uploadedDateTime("1 January 2021, 12:00pm")
                    .document(mainDocument)
                    .supportingEvidenceBundle(wrapElements(supportingEvidenceBundle))
                    .build())
                .otherApplicationsBundle(OtherApplicationsBundle.builder()
                    .id(notSelectedBundleId)
                    .uploadedDateTime("1 January 2021, 12:00pm")
                    .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN)
                    .build())
                .build()
            ));

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .additionalApplicationsDynamicList(DYNAMIC_LIST_ITEM_ID)
                .build())
            .additionalApplicationsBundle(additionalApplicationsBundles)
            .build();

        String expectedDocumentLabel = mainDocument.getFilename() + "\n" + supportingDocument.getFilename();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("additionalApplicationsDynamicList"), DynamicList.class
        );

        DynamicList expectedDynamicList = buildDynamicList(1,
            Pair.of(notSelectedBundleId, "C1, 1 January 2021, 12:00pm"),
            Pair.of(DYNAMIC_LIST_ITEM_ID, "C2, 1 January 2021, 12:00pm")
        );

        assertThat(response.getData().get("relatedDocumentsLabel")).isEqualTo(expectedDocumentLabel);
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

    @Test
    void shouldNotReturnAValidationErrorWhenEmailIsValid() {
        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData
                .builder()
                .judicialMessageMetaData(JudicialMessageMetaData
                    .builder()
                    .recipient("valid-email@test.com")
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnAValidationErrorWhenEmailIsInvalid() {
        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData
                .builder()
                .judicialMessageMetaData(JudicialMessageMetaData
                    .builder()
                    .recipient("Test user <Test.User@HMCTS.NET>")
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getErrors()).contains(
            "Enter an email address in the correct format, for example name@example.com");
    }
}
