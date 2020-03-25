package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import com.launchdarkly.client.LDClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsSubmittedControllerTest extends AbstractControllerTest {

    private static final UserInfo USER_INFO_CAFCASS = UserInfo.builder().roles(UserRole.CAFCASS.getRoles()).build();
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final Long CASE_ID = 12345L;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private LDClient ldClient;

    @MockBean
    private PaymentService paymentService;

    UploadC2DocumentsSubmittedControllerTest() {
        super("upload-c2");
    }

    @BeforeEach
    void setup() {
        given(idamApi.retrieveUserInfo(any())).willReturn(USER_INFO_CAFCASS);
    }

    @Test
    void submittedEventShouldNotifyHmctsAdminWhenCtscToggleIsDisabled() throws Exception {
        postSubmittedEvent(enableSendToCtscOnCaseDetails(NO, YES));

        verify(notificationClient).sendEmail(
            C2_UPLOAD_NOTIFICATION_TEMPLATE,
            "admin@family-court.com",
            expectedNotificationParams(),
            CASE_ID.toString()
        );

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_NOTIFICATION_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedNotificationParams(),
            CASE_ID.toString()
        );
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenCtscToggleIsEnabled() throws Exception {
        postSubmittedEvent(enableSendToCtscOnCaseDetails(YES, YES));

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_NOTIFICATION_TEMPLATE,
            "admin@family-court.com",
            expectedNotificationParams(),
            CASE_ID.toString()
        );

        verify(notificationClient).sendEmail(
            C2_UPLOAD_NOTIFICATION_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com", expectedNotificationParams(),
            CASE_ID.toString()
        );
    }

    @Test
    void submittedEventShouldNotifyAdminWhenC2IsNotUsingPbaPayment() throws Exception {
        postSubmittedEvent(enableSendToCtscOnCaseDetails(NO, NO));

        verify(notificationClient).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "admin@family-court.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            CASE_ID.toString()
        );

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            CASE_ID.toString()
        );
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenC2IsNotUsingPbaPaymentAndCtscToggleIsEnabled() throws Exception {
        postSubmittedEvent(enableSendToCtscOnCaseDetails(YES, NO));

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "admin@family-court.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            CASE_ID.toString()
        );

        verify(notificationClient).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            CASE_ID.toString()
        );
    }

    @Test
    void submittedEventShouldNotNotifyAdminWhenUC2IsUsingPbaPayment() throws Exception {
        postSubmittedEvent(enableSendToCtscOnCaseDetails(NO, YES));

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "admin@family-court.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            CASE_ID.toString()
        );

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            CASE_ID.toString()
        );
    }

    @Test
    void shouldMakePaymentWhenFeatureToggleIsTrue() {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildC2DocumentBundle(YES))
            .build();

        postSubmittedEvent(createCase(caseData));

        verify(paymentService).makePaymentForC2(CASE_ID, mapper.convertValue(caseData, CaseData.class));
    }

    @Test
    void shouldNotMakePaymentWhenFeatureToggleIsFalse() {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(false);
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildC2DocumentBundle(YES))
            .build();

        postSubmittedEvent(createCase(caseData));

        verify(paymentService, never()).makePaymentForC2(any(), any());
    }

    private CaseDetails enableSendToCtscOnCaseDetails(YesNo enableCtsc, YesNo usePbaPayment) {
        return createCase(ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildC2DocumentBundle(usePbaPayment))
            .put("sendToCtsc", enableCtsc.getValue())
            .build());
    }

    private Map<String, Object> expectedNotificationParams() {
        return Map.of(
            "reference", CASE_ID.toString(),
            "hearingDetailsCallout", String.format("%s, %s", RESPONDENT_SURNAME, CASE_ID.toString()),
            "subjectLine", String.format("%s, %s", RESPONDENT_SURNAME, CASE_ID.toString()),
            "caseUrl", "http://fake-url/case/PUBLICLAW/CARE_SUPERVISION_EPO/" + CASE_ID
        );
    }

    private Map<String, Object> buildCommonNotificationParameters() {
        return Map.of(
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "familyManCaseNumber", String.valueOf(CASE_ID),
            "respondents1", List.of(
                Map.of(
                    "value", Respondent.builder()
                        .party(RespondentParty.builder()
                            .lastName(RESPONDENT_SURNAME)
                            .build())
                        .build()))
        );
    }

    private CaseDetails createCase(Map<String, Object> data) {
        return CaseDetails.builder()
            .data(data)
            .id(CASE_ID)
            .build();
    }

    private Map<String, Object> buildC2DocumentBundle(YesNo usePbaPayment) {
        return ImmutableMap.of(
            "c2DocumentBundle", wrapElements(C2DocumentBundle.builder()
                .usePbaPayment(usePbaPayment.getValue())
                .build())
        );
    }

    private Map<String, Object> expectedPbaPaymentNotTakenNotificationParams() {
        return Map.of(
            "caseUrl", "http://fake-url/case/PUBLICLAW/CARE_SUPERVISION_EPO/" + CASE_ID
        );
    }
}
