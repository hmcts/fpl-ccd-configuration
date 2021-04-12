package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.CAFCASS_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.CAFCASS_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {GeneratedOrderEventHandler.class, InboxLookupService.class, LookupTestConfig.class,
    IssuedOrderAdminNotificationHandler.class, RepresentativeNotificationService.class,
    HmctsAdminNotificationHandler.class, SendDocumentService.class})
class GeneratedOrderEventHandlerTest {

    @MockBean
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private RepresentativeNotificationService representativeNotificationService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;

    @MockBean
    private SendDocumentService sendDocumentService;

    @Autowired
    private GeneratedOrderEventHandler generatedOrderEventHandler;

    private CaseData caseData = caseData();

    private final DocumentReference testDocument = DocumentReference.builder()
        .filename("GeneratedOrder")
        .url("url")
        .binaryUrl("testUrl").build();

    private final GeneratedOrderEvent event = new GeneratedOrderEvent(caseData, testDocument);

    @BeforeEach
    void before() {
        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData, testDocument,
            GENERATED_ORDER))
            .willReturn(getExpectedParameters(BLANK_ORDER.getLabel(), true));

        given(orderIssuedEmailContentProvider.getNotifyDataWithoutCaseUrl(
            caseData, event.getOrderDocument(), GENERATED_ORDER))
            .willReturn(getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true));
    }

    @Test
    void shouldNotifyPartiesOnOrderSubmission() {
        generatedOrderEventHandler.notifyParties(event);

        verify(issuedOrderAdminNotificationHandler).notifyAdmin(
            caseData,
            testDocument,
            GENERATED_ORDER);

        verify(notificationService).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            getExpectedParameters(BLANK_ORDER.getLabel(), true),
            caseData.getId().toString());

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            caseData.getId(),
            getExpectedParameters(BLANK_ORDER.getLabel(), true),
            getExpectedDigitalServedRepresentativesForAddingPartiesToCase(),
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES);

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            caseData.getId(),
            getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true),
            getExpectedEmailRepresentativesForAddingPartiesToCase(),
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES);
    }

    @Test
    void shouldNotBuildNotificationTemplateDataForEmailRepresentativesWhenEmailRepresentativesDoNotExist() {
        CaseData caseData = caseData().toBuilder()
            .representatives(List.of(
                element(Representative.builder()
                    .servingPreferences(DIGITAL_SERVICE)
                    .fullName("Test user")
                    .email("testuser@test.co.uk")
                    .build())
            )).build();

        GeneratedOrderEvent event = new GeneratedOrderEvent(caseData, testDocument);

        generatedOrderEventHandler.notifyParties(event);

        verify(orderIssuedEmailContentProvider, never()).getNotifyDataWithoutCaseUrl(any(), any(), any());

        verify(representativeNotificationService, never()).sendNotificationToRepresentatives(
            any(), any(), any(), eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES));
    }

    @Test
    void shouldSendOrderToRepresentativesAndNotRepresentedRespondentsByPost() {
        final Representative representative = Representative.builder()
            .fullName("First Representative")
            .servingPreferences(POST)
            .address(testAddress())
            .build();

        final RespondentParty respondent = RespondentParty.builder()
            .firstName("First")
            .lastName("Respondent")
            .address(testAddress())
            .build();

        final CaseData caseData = caseData().toBuilder()
            .representatives(wrapElements(representative))
            .respondents1(wrapElements(Respondent.builder().party(respondent).build()))
            .build();

        final GeneratedOrderEvent event = new GeneratedOrderEvent(caseData, testDocument);

        given(sendDocumentService.getStandardRecipients(caseData)).willReturn(List.of(representative, respondent));

        generatedOrderEventHandler.sendOrderByPost(event);

        verify(sendDocumentService).sendDocuments(caseData, List.of(testDocument), List.of(representative, respondent));
        verify(sendDocumentService).getStandardRecipients(caseData);

        verifyNoMoreInteractions(sendDocumentService);
        verifyNoInteractions(notificationService);
    }

    private AllocatedJudgeTemplateForGeneratedOrder getOrderIssuedAllocatedJudgeParameters() {
        return AllocatedJudgeTemplateForGeneratedOrder.builder()
            .orderType("blank order (c21)")
            .callout("^Jones, SACCCCCCCC5676576567, hearing 26 Aug 2020")
            .caseUrl("null/case/\" + JURISDICTION + \"/\" + CASE_TYPE + \"/12345")
            .respondentLastName("Smith")
            .judgeTitle("Her Honour Judge")
            .judgeName("Byrne")
            .build();
    }

    private List<Representative> getExpectedEmailRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("barney@rubble.com")
                .fullName("Barney Rubble")
                .servingPreferences(EMAIL)
                .positionInACase("Cafcass Solicitor")
                .telephoneNumber("0717171718")
                .role(CAFCASS_SOLICITOR)
                .address(Address.builder()
                    .addressLine1("160 Tooley St")
                    .addressLine2("Tooley road")
                    .addressLine3("Tooley")
                    .postTown("Limerick")
                    .postcode("SE1 2QH")
                    .country("Ireland")
                    .county("Galway")
                    .build())
                .build());
    }

    private List<Representative> getExpectedDigitalServedRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("fred@flinstone.com")
                .fullName("Fred Flinstone")
                .servingPreferences(DIGITAL_SERVICE)
                .positionInACase("Cafcass Guardian")
                .telephoneNumber("0717171717")
                .role(CAFCASS_GUARDIAN)
                .address(Address.builder()
                    .addressLine1("160 Tooley St")
                    .addressLine2("Tooley road")
                    .addressLine3("Tooley")
                    .postTown("Limerick")
                    .postcode("SE1 2QH")
                    .country("Ireland")
                    .county("Galway")
                    .build())
                .build());
    }
}
