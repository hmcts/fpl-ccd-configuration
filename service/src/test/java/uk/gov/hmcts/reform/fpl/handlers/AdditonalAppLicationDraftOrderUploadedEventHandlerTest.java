package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    private void mockHelper(MockedStatic<CafcassHelper> cafcassHelper, boolean notifyCafcass) {
        cafcassHelper.when(() -> CafcassHelper.isNotifyingCafcass(any(), any()))
            .thenReturn(notifyCafcass);
    }

    @Test
    void shouldSendNotificationToCafcassWhenDraftOrderPresent() {
        try (MockedStatic<CafcassHelper> cafcassHelper = Mockito.mockStatic(CafcassHelper.class)) {
            mockHelper(cafcassHelper, true);

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
    }

    @Test
    void shouldSendNotificationToCafcassWhenNoNewApplicationUploaded() {
        try (MockedStatic<CafcassHelper> cafcassHelper = Mockito.mockStatic(CafcassHelper.class)) {
            mockHelper(cafcassHelper, true);

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
    }

    @Test
    void shouldNotSendNotificationToCafcassWhenNoDraftOrderPresent() {
        try (MockedStatic<CafcassHelper> cafcassHelper = Mockito.mockStatic(CafcassHelper.class)) {
            mockHelper(cafcassHelper, true);

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
    }

    @Test
    void shouldNotSendNotificationToCafcassWhenNoC2Present() {
        try (MockedStatic<CafcassHelper> cafcassHelper = Mockito.mockStatic(CafcassHelper.class)) {
            mockHelper(cafcassHelper, true);

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

    @Test
    void shouldNotSendNotificationToCafcassWhenNonEnglishLA() {
        try (MockedStatic<CafcassHelper> cafcassHelper = Mockito.mockStatic(CafcassHelper.class)) {
            mockHelper(cafcassHelper, false);

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
}
