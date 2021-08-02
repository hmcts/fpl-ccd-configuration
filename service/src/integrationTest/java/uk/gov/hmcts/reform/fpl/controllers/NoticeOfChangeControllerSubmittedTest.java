package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(NoticeOfChangeController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfChangeControllerSubmittedTest extends AbstractCallbackTest {
    private static final Long CASE_ID = 10L;
    private static final String CASE_NAME = "Test";
    private static final String NEW_EMAIL = "new@test.com";
    private static final String OLD_EMAIL = "old@test.com";

    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    public static final RespondentParty RESPONDENT_PARTY = RespondentParty.builder()
        .firstName("John").lastName("Smith").build();

    public static final RespondentSolicitor OLD_REGISTERED_SOLICITOR = RespondentSolicitor.builder()
        .firstName("Old")
        .lastName("Solicitor")
        .email(OLD_EMAIL)
        .organisation(Organisation.builder().organisationID("123").build()).build();

    public static final RespondentSolicitor NEW_REGISTERED_SOLICITOR = RespondentSolicitor.builder()
        .firstName("New")
        .lastName("Solicitor")
        .email(NEW_EMAIL)
        .organisation(Organisation.builder().organisationID("321").build()).build();

    public static final Element<Respondent> OLD_RESPONDENT = element(Respondent.builder()
        .party(RESPONDENT_PARTY)
        .solicitor(OLD_REGISTERED_SOLICITOR).build());

    private static final Element<Respondent> OTHER_RESPONDENT = element(Respondent.builder()
        .legalRepresentation("Yes")
        .solicitor(RespondentSolicitor.builder()
            .firstName("Other")
            .lastName("Solicitor")
            .email("other@test.com")
            .organisation(Organisation.builder().organisationID("123").build()).build())
        .build());

    private static final List<Element<Child>> CHILDREN = wrapElements(Child.builder()
        .party(ChildParty.builder()
            .dateOfBirth(LocalDate.of(2015, 1, 1))
            .lastName("Jones").build())
        .build());

    @MockBean
    private NotificationClient notificationClient;

    NoticeOfChangeControllerSubmittedTest() {
        super("noc-decision");
    }

    @Test
    void shouldNotifyRespondentSolicitorsWhenNoticeOfChangeIsSubmitted() {

        NoticeOfChangeRespondentSolicitorTemplate noticeOfChangeNewRespondentSolicitorTemplate =
            getExpectedNoticeOfChangeParameters("Dear New Solicitor");

        NoticeOfChangeRespondentSolicitorTemplate noticeOfChangeOldRespondentSolicitorTemplate =
            getExpectedNoticeOfChangeParameters("Dear Old Solicitor");

        final Map<String, Object> newSolicitorParameters = caseConverter.toMap(
            noticeOfChangeNewRespondentSolicitorTemplate);

        final Map<String, Object> oldSolicitorParameters = caseConverter.toMap(
            noticeOfChangeOldRespondentSolicitorTemplate);

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .children1(CHILDREN)
            .respondents1(List.of(OTHER_RESPONDENT, OLD_RESPONDENT))
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .respondents1(List.of(OTHER_RESPONDENT, element(
                Respondent.builder()
                    .legalRepresentation("Yes")
                    .party(RESPONDENT_PARTY)
                    .solicitor(NEW_REGISTERED_SOLICITOR)
                    .build())))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() -> {
                verify(notificationClient, times(1)).sendEmail(
                    NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
                    NEW_EMAIL,
                    newSolicitorParameters,
                    NOTIFICATION_REFERENCE);

                verify(notificationClient, times(1)).sendEmail(
                    NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
                    OLD_EMAIL,
                    oldSolicitorParameters,
                    NOTIFICATION_REFERENCE);
            }
        );
    }

    @Test
    void shouldNotifyNewRespondentSolicitorWhenNoPreviousRepresentationAndNoticeOfChangeIsSubmitted() {
        NoticeOfChangeRespondentSolicitorTemplate noticeOfChangeNewRespondentSolicitorTemplate =
            getExpectedNoticeOfChangeParameters("Dear New Solicitor");

        final Map<String, Object> newSolicitorParameters = caseConverter.toMap(
            noticeOfChangeNewRespondentSolicitorTemplate);

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .children1(CHILDREN)
            .respondents1(wrapElements(
                Respondent.builder()
                    .party(RESPONDENT_PARTY)
                    .legalRepresentation("No")
                    .build()
            )).build();

        CaseData caseData = caseDataBefore.toBuilder()
            .respondents1(wrapElements(
                Respondent.builder()
                    .legalRepresentation("Yes")
                    .party(RESPONDENT_PARTY)
                    .solicitor(NEW_REGISTERED_SOLICITOR)
                    .build()))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() -> verify(notificationClient, times(1)).sendEmail(
            NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
            NEW_EMAIL,
            newSolicitorParameters,
            NOTIFICATION_REFERENCE)
        );
    }

    private NoticeOfChangeRespondentSolicitorTemplate getExpectedNoticeOfChangeParameters(String expectedSalutation) {
        return NoticeOfChangeRespondentSolicitorTemplate.builder()
            .salutation(expectedSalutation)
            .caseName(CASE_NAME)
            .ccdNumber(CASE_ID.toString())
            .caseUrl(caseUrl(CASE_ID))
            .clientFullName("John Smith")
            .childLastName("Jones")
            .build();
    }
}
