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
    private static final List<LegalRepresentative> LEGAL_REPRESENTATIVES_BEFORE =
        List.of(mock(LegalRepresentative.class));
    private static final CaseData CASE_DATA_BEFORE = CaseData.builder()
        .legalRepresentatives(wrapElements(LEGAL_REPRESENTATIVES_BEFORE))
        .build();
    private static final List<LegalRepresentative> LEGAL_REPRESENTATIVES_NOW = List.of(mock(LegalRepresentative.class));
    private static final String REPRESENTATIVE_EMAIL = "representativeEmail";
    private static final String REPRESENTATIVE_EMAIL_2 = "representativeEmail2";
    private static final LegalRepresentative LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .email(REPRESENTATIVE_EMAIL)
        .build();
    private static final LegalRepresentative LEGAL_REPRESENTATIVE_2 = LegalRepresentative.builder()
        .email(REPRESENTATIVE_EMAIL_2)
        .build();
    private static final Long CASE_ID = 123344L;
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID)
        .legalRepresentatives(wrapElements(LEGAL_REPRESENTATIVES_NOW))
        .build();

    @Mock
    private LegalRepresentativeAddedContentProvider contentProvider;
    @Mock
    private LegalRepresentativesDifferenceCalculator diffCalculator;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LegalRepresentativesUpdatedHandler underTest;

    @Test
    void sendEmailToLegalRepresentativesAddedToCase() {
        when(diffCalculator.calculate(LEGAL_REPRESENTATIVES_BEFORE, LEGAL_REPRESENTATIVES_NOW))
            .thenReturn(LegalRepresentativesChange.builder().added(Set.of(LEGAL_REPRESENTATIVE)).build());

        LegalRepresentativeAddedTemplate notifyData = mock(LegalRepresentativeAddedTemplate.class);
        when(contentProvider.getNotifyData(LEGAL_REPRESENTATIVE, CASE_DATA))
            .thenReturn(notifyData);

        underTest.sendEmailToLegalRepresentativesAddedToCase(
            new LegalRepresentativesUpdated(CASE_DATA, CASE_DATA_BEFORE)
        );

        verify(notificationService).sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE, REPRESENTATIVE_EMAIL, notifyData, CASE_ID
        );
    }

    @Test
    void sendEmailToLegalRepresentativesAddedToCaseMultiple() {
        when(diffCalculator.calculate(LEGAL_REPRESENTATIVES_BEFORE, LEGAL_REPRESENTATIVES_NOW))
            .thenReturn(LegalRepresentativesChange.builder()
                .added(Set.of(LEGAL_REPRESENTATIVE, LEGAL_REPRESENTATIVE_2))
                .build()
            );

        LegalRepresentativeAddedTemplate notifyData1 = mock(LegalRepresentativeAddedTemplate.class);
        LegalRepresentativeAddedTemplate notifyData2 = mock(LegalRepresentativeAddedTemplate.class);

        when(contentProvider.getNotifyData(LEGAL_REPRESENTATIVE, CASE_DATA)).thenReturn(notifyData1);
        when(contentProvider.getNotifyData(LEGAL_REPRESENTATIVE_2, CASE_DATA)).thenReturn(notifyData2);

        underTest.sendEmailToLegalRepresentativesAddedToCase(
            new LegalRepresentativesUpdated(CASE_DATA, CASE_DATA_BEFORE)
        );

        verify(notificationService).sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE, REPRESENTATIVE_EMAIL, notifyData1, CASE_ID
        );
        verify(notificationService).sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE, REPRESENTATIVE_EMAIL_2, notifyData2, CASE_ID
        );
    }

    @Test
    void sendEmailToLegalRepresentativesAddedToCaseNone() {
        when(diffCalculator.calculate(LEGAL_REPRESENTATIVES_BEFORE, LEGAL_REPRESENTATIVES_NOW))
            .thenReturn(LegalRepresentativesChange.builder().added(emptySet()).build());

        underTest.sendEmailToLegalRepresentativesAddedToCase(
            new LegalRepresentativesUpdated(CASE_DATA, CASE_DATA_BEFORE)
        );

        verifyNoInteractions(contentProvider, notificationService);
    }
}
