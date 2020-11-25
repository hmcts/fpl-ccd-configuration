package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.LegalRepresentativesUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentativesChange;
import uk.gov.hmcts.reform.fpl.model.notify.LegalRepresentativeAddedTemplate;
import uk.gov.hmcts.reform.fpl.service.LegalRepresentativesDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalRepresentativeAddedContentProvider;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class LegalRepresentativesUpdatedHandlerTest {

    public static final List<LegalRepresentative> LEGAL_REPRESENTATIVES_BEFORE =
        List.of(mock(LegalRepresentative.class));
    public static final CaseData CASE_DATA_BEFORE = CaseData.builder()
        .legalRepresentatives(wrapElements(LEGAL_REPRESENTATIVES_BEFORE))
        .build();
    public static final List<LegalRepresentative> LEGAL_REPRESENTATIVES_NOW = List.of(mock(LegalRepresentative.class));
    public static final String REPRESENTATIVE_EMAIL = "representativeEmail";
    public static final String REPRESENTATIVE_EMAIL_2 = "representativeEmail2";
    public static final LegalRepresentative LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .email(REPRESENTATIVE_EMAIL)
        .build();
    public static final LegalRepresentative LEGAL_REPRESENTATIVE_2 = LegalRepresentative.builder()
        .email(REPRESENTATIVE_EMAIL_2)
        .build();
    public static final String CASE_ID = "123344";
    public static final CaseData CASE_DATA = CaseData.builder()
        .id(Long.valueOf(CASE_ID))
        .legalRepresentatives(wrapElements(LEGAL_REPRESENTATIVES_NOW))
        .build();

    @Mock
    private LegalRepresentativeAddedContentProvider legalRepresentativeAddedContentProvider;
    @Mock
    private LegalRepresentativesDifferenceCalculator legalRepresentativesDifferenceCalculator;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LegalRepresentativesUpdatedHandler underTest;

    @Test
    void sendEmailToLegalRepresentativesAddedToCase() {
        when(legalRepresentativesDifferenceCalculator.calculate(
            LEGAL_REPRESENTATIVES_BEFORE, LEGAL_REPRESENTATIVES_NOW)
        ).thenReturn(LegalRepresentativesChange.builder()
            .added(Set.of(LEGAL_REPRESENTATIVE))
            .build()
        );

        LegalRepresentativeAddedTemplate notifyData = LegalRepresentativeAddedTemplate.builder().build();
        when(legalRepresentativeAddedContentProvider.getNotifyData(LEGAL_REPRESENTATIVE, CASE_DATA))
            .thenReturn(notifyData);

        underTest.sendEmailToLegalRepresentativesAddedToCase(
            new LegalRepresentativesUpdated(CASE_DATA, CASE_DATA_BEFORE)
        );

        verify(notificationService).sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE,
            REPRESENTATIVE_EMAIL,
            notifyData,
            CASE_ID
        );
    }

    /*private LegalRepresentativeAddedTemplate getLegalRepresentativeAddedTemplateParameters(String repName) {
        return LegalRepresentativeAddedTemplate.builder()
            .firstRespondentLastName("test")
            .familyManCaseNumber("6789")
            .localAuthority(REPRESENTATIVE_EMAIL)
            .repName(repName)
            .caseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + CASE_ID)
            .build();
    }*/

    @Test
    void sendEmailToLegalRepresentativesAddedToCaseMultiple() {
        when(legalRepresentativesDifferenceCalculator.calculate(
            LEGAL_REPRESENTATIVES_BEFORE, LEGAL_REPRESENTATIVES_NOW)
        ).thenReturn(LegalRepresentativesChange.builder()
            .added(Set.of(LEGAL_REPRESENTATIVE, LEGAL_REPRESENTATIVE_2))
            .build()
        );

        LegalRepresentativeAddedTemplate notifyData1 = LegalRepresentativeAddedTemplate.builder().build();
        LegalRepresentativeAddedTemplate notifyData2 = LegalRepresentativeAddedTemplate.builder().build();

        when(legalRepresentativeAddedContentProvider.getNotifyData(LEGAL_REPRESENTATIVE, CASE_DATA)).thenReturn(
            notifyData1);
        when(legalRepresentativeAddedContentProvider.getNotifyData(LEGAL_REPRESENTATIVE_2, CASE_DATA)).thenReturn(
            notifyData2);

        underTest.sendEmailToLegalRepresentativesAddedToCase(
            new LegalRepresentativesUpdated(CASE_DATA, CASE_DATA_BEFORE)
        );

        verify(notificationService).sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE,
            REPRESENTATIVE_EMAIL,
            notifyData1,
            CASE_ID
        );
        verify(notificationService).sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE,
            REPRESENTATIVE_EMAIL_2,
            notifyData2,
            CASE_ID
        );
    }

    @Test
    void sendEmailToLegalRepresentativesAddedToCaseNone() {

        when(legalRepresentativesDifferenceCalculator.calculate(
            LEGAL_REPRESENTATIVES_BEFORE, LEGAL_REPRESENTATIVES_NOW)
        ).thenReturn(LegalRepresentativesChange.builder()
            .added(emptySet())
            .build()
        );

        underTest.sendEmailToLegalRepresentativesAddedToCase(
            new LegalRepresentativesUpdated(CASE_DATA, CASE_DATA_BEFORE)
        );

        verifyNoInteractions(legalRepresentativeAddedContentProvider, notificationService);
    }
}
