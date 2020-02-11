package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.MapDifference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAdminOrderIssuedTestHelper.buildRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAdminOrderIssuedTestHelper.getExpectedParametersForAdminWhenNoRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAdminOrderIssuedTestHelper.verifyNotificationSentToAdminWhenOrderIssued;

@ActiveProfiles("integration-test")
@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementSubmittedEventControllerTest extends AbstractControllerTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String CASE_ID = "12345";
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final byte[] PDF = {1, 2, 3, 4, 5};

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> dataCaptor;

    PlacementSubmittedEventControllerTest() {
        super("placement");
    }

    @Test
    void shouldSendNotificationToAdminWhenNoticeOfPlacementOrderIssued() throws NotificationClientException {
        postSubmittedEvent(buildCallbackRequestWithoutRepresentatives());

        verify(notificationClient).sendEmail(
            eq(ORDER_NOTIFICATION_TEMPLATE_FOR_ADMIN), eq("admin@family-court.com"),
            eq(getExpectedParametersForAdminWhenNoRepresentativesServedByPost()), eq(CASE_ID));

        verifyZeroInteractions(notificationClient);
    }

    @Test
    void shouldSendOrderIssuedNotificationToAdminWhenRepresentativesNeedServing() throws NotificationClientException {
        given(documentDownloadService.downloadDocument(anyString(), anyString(), anyString()))
            .willReturn(PDF);

        postSubmittedEvent(buildCallbackRequestWithRepresentatives());

        verify(notificationClient).sendEmail(
            eq(ORDER_NOTIFICATION_TEMPLATE_FOR_ADMIN), eq("admin@family-court.com"),
            dataCaptor.capture(), eq(CASE_ID));

        MapDifference<String, Object> difference = verifyNotificationSentToAdminWhenOrderIssued(dataCaptor);
        assertThat(difference.areEqual()).isTrue();

        verifyZeroInteractions(notificationClient);
    }

    private CallbackRequest buildCallbackRequestWithoutRepresentatives() {
        return CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder()
                .id(12345L)
                .data(Map.of(
                    "respondents1", createRespondents(),
                    "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
                    "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER))
                .build())

            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(Map.of(
                    "confidentialPlacements", List.of(element(Placement.builder()
                        .orderAndNotices(wrapElements(PlacementOrderAndNotices.builder()
                            .type(NOTICE_OF_PLACEMENT_ORDER)
                            .document(DocumentReference.buildFromDocument(document()))
                            .build()))
                        .build())),
                    "respondents1", createRespondents(),
                    "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
                    "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER))
                .build())
            .build();
    }

    private CallbackRequest buildCallbackRequestWithRepresentatives() {
        return CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder()
                .id(12345L)
                .data(Map.of(
                    "respondents1", createRespondents(),
                    "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
                    "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER))
                .build())

            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(Map.of(
                    "confidentialPlacements", List.of(element(Placement.builder()
                        .orderAndNotices(wrapElements(PlacementOrderAndNotices.builder()
                            .type(NOTICE_OF_PLACEMENT_ORDER)
                            .document(DocumentReference.buildFromDocument(document()))
                            .build()))
                        .build())),
                    "respondents1", createRespondents(),
                    "representatives", buildRepresentativesServedByPost(),
                    "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
                    "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER))
                .build())
            .build();
    }
}
