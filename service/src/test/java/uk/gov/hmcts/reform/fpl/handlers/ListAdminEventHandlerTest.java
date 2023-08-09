package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtToCourtAdminLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType;
import uk.gov.hmcts.reform.fpl.events.ListAdminEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.COURT_ADMIN_LISTING_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.COURT_ADMIN_UDO_LISTING_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class ListAdminEventHandlerTest {
    private static final DocumentReference ORDER = testDocumentReference();
    @Mock
    private HmctsCourtToCourtAdminLookupConfiguration hmctsCourtToCourtAdminLookupConfiguration;
    @Mock
    private SDONotifyData notifyData;
    @Mock
    private SDOIssuedContentProvider standardContentProvider;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private ListAdminEventHandler underTest;

    //@Test
    void shouldNotifyCourtAdminWhenJudgeRequestListingForSDO() {
        CaseData caseData = CaseData.builder()
            .id(1123L)
            .court(Court.builder()
                .code("344")
                .name("Family Court")
                .build()
            )
            .familyManCaseNumber("FamilyMan1234")
            .build();

        ListAdminEvent event = ListAdminEvent.builder()
            .caseData(caseData)
            .order(ORDER)
            .isSentToAdmin(true)
            .sendToAdminReason("Please complete")
            .directionsOrderType(DirectionsOrderType.SDO)
            .build();

        given(hmctsCourtToCourtAdminLookupConfiguration.getEmail("344"))
            .willReturn("FamilyPublicLaw+ctsc@gmail.com");

        given(standardContentProvider.buildNotificationParameters(caseData,
            ORDER,
            event.getSendToAdminReason())).willReturn(notifyData);

        underTest.notifyCourtAdmin(event);

        verify(notificationService).sendEmail(COURT_ADMIN_LISTING_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            notifyData,
            1123L
        );
    }

    //@Test
    void shouldNotifyCourtAdminWhenJudgeRequestListingForUDO() {
        CaseData caseData = CaseData.builder()
            .id(1123L)
            .court(Court.builder()
                .code("344")
                .name("Family Court")
                .build()
            )
            .familyManCaseNumber("FamilyMan1234")
            .build();

        ListAdminEvent event = ListAdminEvent.builder()
            .caseData(caseData)
            .order(ORDER)
            .isSentToAdmin(true)
            .sendToAdminReason("Please complete")
            .directionsOrderType(DirectionsOrderType.UDO)
            .build();

        given(hmctsCourtToCourtAdminLookupConfiguration.getEmail("344"))
            .willReturn("FamilyPublicLaw+ctsc@gmail.com");

        given(standardContentProvider.buildNotificationParameters(caseData,
            ORDER,
            event.getSendToAdminReason())).willReturn(notifyData);

        underTest.notifyCourtAdmin(event);

        verify(notificationService).sendEmail(COURT_ADMIN_UDO_LISTING_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            notifyData,
            1123L
        );
    }

    //@Test
    void shouldNotNotifyCourtAdminWhenJudgeHasCompletedListing() {
        CaseData caseData = CaseData.builder()
            .id(1123L)
            .court(Court.builder()
                .code("344")
                .name("Family Court")
                .build()
            )
            .familyManCaseNumber("FamilyMan1234")
            .build();

        ListAdminEvent event = ListAdminEvent.builder()
            .caseData(caseData)
            .order(ORDER)
            .isSentToAdmin(false)
            .directionsOrderType(DirectionsOrderType.SDO)
            .build();

        underTest.notifyCourtAdmin(event);

        verify(notificationService, never()).sendEmail(COURT_ADMIN_LISTING_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            notifyData,
            1123L
        );
    }

}
