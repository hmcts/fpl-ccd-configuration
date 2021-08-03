package uk.gov.hmcts.reform.fpl.service.representative;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {RepresentativeNotificationService.class, RepresentativesInbox.class,
    OtherRecipientsInbox.class})
class RepresentativeNotificationServiceTest {
    @MockBean
    private NotificationService notificationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private OtherRecipientsInbox otherRecipientsInbox;

    @Autowired
    private RepresentativeNotificationService representativeNotificationService;

    private static final Long CASE_ID = 1111159545791091L;
    private static final OrderIssuedNotifyData TEMPLATE_DATA = OrderIssuedNotifyData.builder().build();
    private static final String TEMPLATE_NAME = LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE;

    @Test
    void shouldNotifyRepresentativesWithDigitalServingPreferenceOnly() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .representatives(getRepresentativesOfMixedServingPreferences())
            .build();

        representativeNotificationService.sendToRepresentativesByServedPreference(
            DIGITAL_SERVICE, TEMPLATE_NAME, TEMPLATE_DATA, caseData);

        verify(notificationService).sendEmail(
            TEMPLATE_NAME,
            "tom@test.co.uk",
            TEMPLATE_DATA,
            CASE_ID);

        verify(notificationService).sendEmail(
            TEMPLATE_NAME,
            "sara@test.co.uk",
            TEMPLATE_DATA,
            CASE_ID);
    }

    @Test
    void shouldNotifyRepresentativesWithEmailServingPreferenceOnly() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .representatives(getRepresentativesOfMixedServingPreferences())
            .build();

        representativeNotificationService.sendToRepresentativesByServedPreference(
            EMAIL, TEMPLATE_NAME, TEMPLATE_DATA, caseData);

        verify(notificationService).sendEmail(
            TEMPLATE_NAME,
            "sam@test.co.uk",
            TEMPLATE_DATA,
            CASE_ID);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotNotifyRepresentativesOfNonSelectedOthers() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .representatives(getRepresentativesOfMixedServingPreferences())
            .build();

        given(otherRecipientsInbox.getNonSelectedRecipients(
            eq(EMAIL),
            eq(caseData),
            eq(emptyList()),
            any())).willReturn((Set) Set.of("sam@test.co.uk"));

        representativeNotificationService.sendToRepresentativesByServedPreference(
            EMAIL, TEMPLATE_NAME, TEMPLATE_DATA, caseData, emptyList());

        verify(notificationService, never()).sendEmail(
            TEMPLATE_NAME,
            "sam@test.co.uk",
            TEMPLATE_DATA,
            CASE_ID);
    }

    @Test
    void shouldNotNotifyAnyRepresentativesWhenRepresentativesDoNotMatchServingPreference() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .representatives(getRepresentativesOfMixedServingPreferences())
            .build();

        assertThrows(IllegalArgumentException.class,
            () -> representativeNotificationService.sendToRepresentativesByServedPreference(
                POST, TEMPLATE_NAME, TEMPLATE_DATA, caseData));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotNotifyAnyRepresentativesWhenRepresentativesDoNotExist() {
        CaseData caseData = CaseData.builder().id(CASE_ID).build();

        assertThrows(IllegalArgumentException.class,
            () -> representativeNotificationService.sendToRepresentativesByServedPreference(
                POST, TEMPLATE_NAME, TEMPLATE_DATA, caseData));

        verifyNoMoreInteractions(notificationService);
    }

    private List<Element<Representative>> getRepresentativesOfMixedServingPreferences() {
        return List.of(
            element(Representative.builder()
                .servingPreferences(DIGITAL_SERVICE)
                .email("tom@test.co.uk")
                .build()),
            element(Representative.builder()
                .servingPreferences(DIGITAL_SERVICE)
                .email("sara@test.co.uk")
                .build()),
            element(Representative.builder()
                .servingPreferences(EMAIL)
                .email("sam@test.co.uk")
                .build()));
    }
}
