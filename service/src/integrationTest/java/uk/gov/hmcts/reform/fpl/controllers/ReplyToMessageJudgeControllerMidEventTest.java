package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.OrganisationalRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.RoleAssignmentService;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@WebMvcTest(ReplyToMessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class ReplyToMessageJudgeControllerMidEventTest extends MessageJudgeControllerAbstractTest {
    private static final UUID DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();
    private static final String SENDER = "sender@gmail.com";
    private static final JudicialMessageRoleType SENDER_TYPE = JudicialMessageRoleType.CTSC;
    private static final String CURRENT_USER = "current@gmail.com";
    private static final JudicialMessageRoleType CURRENT_USER_TYPE = JudicialMessageRoleType.LOCAL_COURT_ADMIN;
    private static final String RELATED_FILE_NAME = "file1.doc";
    private static final DocumentReference RELATED_FILE = DocumentReference.builder()
            .filename(RELATED_FILE_NAME)
            .build();
    private static final String MESSAGE_HISTORY = "message history";
    private static final String LATEST_MESSAGE = "Some note";
    private static final String DATE_SENT = "16 December 2020";

    @SpyBean
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

    @MockBean
    private RoleAssignmentService roleAssignmentService;

    @MockBean
    private UserService userService;

    ReplyToMessageJudgeControllerMidEventTest() {
        super("reply-message-judge");
    }

    @BeforeEach
    void beforeEach() {
        when(userService.getOrgRoles())
            .thenReturn(Set.of(OrganisationalRole.LOCAL_COURT_ADMIN));
        when(userService.getUserEmail()).thenReturn(CURRENT_USER);
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
            .judicialMessages(List.of(
                element(DYNAMIC_LIST_ITEM_ID, buildMessage())
            ))
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .judicialMessageDynamicList(DYNAMIC_LIST_ITEM_ID)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getData().get("replyToMessageJudgeNextHearingLabel")).isEqualTo(
            String.format("Next hearing in the case: %s", expectedNextHearing.toLabel()));
    }

    @Test
    void shouldPopulateRelatedDocumentsAndJudgeReplyFieldsWhenReplyingToAMessage() {
        JudicialMessage selectedJudicialMessage = buildMessage();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .judicialMessageDynamicList(DYNAMIC_LIST_ITEM_ID)
                .build())
            .judicialMessages(List.of(
                element(DYNAMIC_LIST_ITEM_ID, selectedJudicialMessage)
            ))
            .build();

        JudicialMessage expectedJudicialMessage = JudicialMessage.builder()
            .relatedDocumentFileNames(selectedJudicialMessage.getRelatedDocumentFileNames())
            .senderType(JudicialMessageRoleType.LOCAL_COURT_ADMIN)
            .recipientLabel(JudicialMessageRoleType.CTSC.getLabel())
            .relatedDocuments(selectedJudicialMessage.getRelatedDocuments())
            .recipientDynamicList(buildRecipientDynamicList(List.of(
                JudicialMessageRoleType.CTSC.toString(),
                JudicialMessageRoleType.OTHER.toString()
            ), null, null).toBuilder()
                .value(DynamicListElement.builder()
                    .code(JudicialMessageRoleType.CTSC.toString())
                    .label(JudicialMessageRoleType.CTSC.getLabel())
                    .build())
                .build())
            .replyFrom(CURRENT_USER)
            .replyTo(SENDER)
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

    private JudicialMessage buildMessage() {
        return JudicialMessage.builder()
            .sender(SENDER)
            .senderType(SENDER_TYPE)
            .relatedDocumentFileNames(RELATED_FILE_NAME)
            .relatedDocuments(List.of(element(RELATED_FILE)))
            .messageHistory(MESSAGE_HISTORY)
            .latestMessage(LATEST_MESSAGE)
            .dateSent(DATE_SENT)
            .build();
    }
}
