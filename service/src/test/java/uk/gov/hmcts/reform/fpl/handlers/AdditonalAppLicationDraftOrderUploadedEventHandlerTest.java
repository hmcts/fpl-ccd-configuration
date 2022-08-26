package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.order.AdditonalAppLicationDraftOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class AdditonalAppLicationDraftOrderUploadedEventHandlerTest {

    private static final Long CASE_ID = 12345L;
    private static final DocumentReference DRAFT_ORDER = testDocumentReference();

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @InjectMocks
    private AdditonalAppLicationDraftOrderUploadedEventHandler underTest;

    @Test
    void shouldSendNotificationToCafcassWhenDraftOrderPresent() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .draftOrdersBundle(wrapElements(DraftOrder.builder().document(DRAFT_ORDER).build()))
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .additionalApplicationsBundle(
                wrapElements(
                    AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(c2DocumentBundle)
                        .build()
                )
            )
            .build();


        CaseData caseDataBefore = CaseData.builder()
                .id(RandomUtils.nextLong())
                .build();

        underTest.sendDocumentsToCafcass(new AdditonalAppLicationDraftOrderUploadedEvent(
            caseData,
            caseDataBefore
            )
        );

        verify(cafcassNotificationService).sendEmail(
            caseData,
            Set.of(DRAFT_ORDER),
            ORDER,
            OrderCafcassData.builder()
                .documentName("draft order")
                .build()
        );
    }

    @Test
    void shouldSendNotificationToCafcassWhenNoNewApplicationUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                        new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .draftOrdersBundle(wrapElements(DraftOrder.builder().document(DRAFT_ORDER).build()))
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .additionalApplicationsBundle(
                wrapElements(
                    AdditionalApplicationsBundle.builder()
                            .c2DocumentBundle(c2DocumentBundle)
                            .build()
                )
            )
            .build();


        CaseData caseDataBefore = CaseData.builder()
            .id(RandomUtils.nextLong())
            .additionalApplicationsBundle(
                wrapElements(
                    AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(c2DocumentBundle)
                        .build()
            )
            )
            .build();

        underTest.sendDocumentsToCafcass(new AdditonalAppLicationDraftOrderUploadedEvent(
                caseData,
                caseDataBefore
            )
        );

        verify(cafcassNotificationService, never()).sendEmail(
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void shouldNotSendNotificationToCafcassWhenNoDraftOrderPresent() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
                .build();

        CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .additionalApplicationsBundle(
                        wrapElements(
                                AdditionalApplicationsBundle.builder()
                                        .c2DocumentBundle(c2DocumentBundle)
                                        .build()
                        )
                )
                .build();


        CaseData caseDataBefore = CaseData.builder()
                .id(RandomUtils.nextLong())
                .build();

        underTest.sendDocumentsToCafcass(new AdditonalAppLicationDraftOrderUploadedEvent(
                        caseData,
                        caseDataBefore
                )
        );

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void shouldNotSendNotificationToCafcassWhenNoC2Present() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .additionalApplicationsBundle(
                wrapElements(
                    AdditionalApplicationsBundle.builder()
                        .otherApplicationsBundle(
                                OtherApplicationsBundle.builder().build()
                        )
                        .build()
                )
            )
            .build();


        CaseData caseDataBefore = CaseData.builder()
                .id(RandomUtils.nextLong())
                .build();

        underTest.sendDocumentsToCafcass(new AdditonalAppLicationDraftOrderUploadedEvent(
                        caseData,
                        caseDataBefore
                )
        );

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void shouldNotSendNotificationToCafcassWhenNonEnglishLA() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.empty()
            );

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .additionalApplicationsBundle(
                wrapElements(
                    AdditionalApplicationsBundle.builder()
                        .otherApplicationsBundle(
                                OtherApplicationsBundle.builder().build()
                        )
                        .build()
                )
            )
            .build();


        CaseData caseDataBefore = CaseData.builder()
            .id(RandomUtils.nextLong())
            .build();

        underTest.sendDocumentsToCafcass(new AdditonalAppLicationDraftOrderUploadedEvent(
                        caseData,
                        caseDataBefore
                )
        );

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any()
        );
    }
}
