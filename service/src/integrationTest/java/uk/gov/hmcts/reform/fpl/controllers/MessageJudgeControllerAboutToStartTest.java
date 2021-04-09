package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerAboutToStartTest extends AbstractCallbackTest {

    @SpyBean
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

    @MockBean
    private UserService userService;

    MessageJudgeControllerAboutToStartTest() {
        super("message-judge");
    }

    @Test
    void shouldInitialiseCaseFieldsWhenAdditionalApplicationsDocumentsAndJudicialMessagesExist() {
        UUID c2DocumentBundleId = randomUUID();
        UUID otherApplicationsBundleId = randomUUID();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles = List.of(
            element(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .id(c2DocumentBundleId)
                    .uploadedDateTime("1 January 2021, 12:00pm")
                    .build())
                .otherApplicationsBundle(OtherApplicationsBundle.builder()
                    .id(otherApplicationsBundleId)
                    .uploadedDateTime("1 January 2021, 12:00pm")
                    .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN)
                   .build())
                .build()
        ));

        List<Element<JudicialMessage>> judicialMessages = List.of(
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("Some date sent")
                .build()),
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("Some date sent")
                .build())
        );

        CaseData caseData = CaseData.builder()
            .id(1111L)
            .additionalApplicationsBundle(additionalApplicationsBundles)
            .judicialMessages(judicialMessages)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        DynamicList additionalApplicationsDynamicList = mapper.convertValue(response.getData()
            .get("additionalApplicationsDynamicList"), DynamicList.class);

        DynamicList judicialMessageDynamicList = mapper.convertValue(
            response.getData().get("judicialMessageDynamicList"), DynamicList.class
        );

        DynamicList expectedAdditionalApplicationsDynamicList = buildDynamicList(
            Pair.of(otherApplicationsBundleId, "C1, 1 January 2021, 12:00pm"),
            Pair.of(c2DocumentBundleId, "C2, 1 January 2021, 12:00pm")
        );

        DynamicList expectedJudicialMessageDynamicList = buildDynamicList(
            Pair.of(judicialMessages.get(0).getId(), "Some date sent"),
            Pair.of(judicialMessages.get(1).getId(), "Some date sent")
        );

        assertThat(additionalApplicationsDynamicList).isEqualTo(expectedAdditionalApplicationsDynamicList);
        assertThat(judicialMessageDynamicList).isEqualTo(expectedJudicialMessageDynamicList);

        assertThat(response.getData().get("hasAdditionalApplications")).isEqualTo(YES.getValue());
        assertThat(response.getData().get("hasJudicialMessages")).isEqualTo(YES.getValue());
    }

    @Test
    void shouldInitialiseOnlySenderAndRecipientEmailAddressesWhenJudicialMessagesAndApplicationDocumentsDoNotExist() {
        CaseData caseData = CaseData.builder().id(1111L).build();
        Map<String, Object> caseDetails = postAboutToStartEvent(caseData).getData();

        assertThat(caseDetails.get("additionalApplicationsDynamicList")).isNull();
        assertThat(caseDetails.get("judicialMessageDynamicList")).isNull();
        assertThat(caseDetails.get("hasAdditionalApplications")).isNull();
        assertThat(caseDetails.get("hasJudicialMessages")).isNull();
        assertThat(caseDetails.get("judicialMessageMetaData"))
            .extracting("sender", "recipient")
            .containsExactly(ctscEmailLookupConfiguration.getEmail(), EMPTY);
    }

    @Test
    void shouldPrePopulateRecipientIfCaseInitiatedByJudge() {
        CaseData caseData = CaseData.builder().build();

        when(userService.getUserEmail()).thenReturn("sender@mail.com");
        when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(true);

        Map<String, Object> caseDetails = postAboutToStartEvent(caseData, UserRole.JUDICIARY.getRoleName()).getData();

        assertThat(caseDetails.get("judicialMessageMetaData")).extracting("sender", "recipient")
            .containsExactly("sender@mail.com", ctscEmailLookupConfiguration.getEmail());
    }
}
