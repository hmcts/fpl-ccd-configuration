package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.LegalRepresentativesChangeToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentativesChange;
import uk.gov.hmcts.reform.fpl.service.LegalRepresentativesDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalRepresentativeAddedContentProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class LegalRepresentativesChangeToCaseEventHandlerTest {

    public static final List<LegalRepresentative> LEGAL_REPRESENTATIVES_BEFORE =
        List.of(mock(LegalRepresentative.class));
    public static final CaseData CASE_DATA_BEFORE = CaseData.builder()
        .legalRepresentatives(wrapElements(LEGAL_REPRESENTATIVES_BEFORE))
        .build();
    public static final List<LegalRepresentative> LEGAL_REPRESENTATIVES_NOW = List.of(mock(LegalRepresentative.class));
    public static final Map<String, Object> TEMPLATE_PARAMETERS = Map.of("blah", new Object());
    public static final Map<String, Object> TEMPLATE_PARAMETERS_2 = Map.of("blah2", new Object());
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
    private LegalRepresentativesChangeToCaseEventHandler underTest;

    @Test
    void sendEmailToLegalRepresentativesAddedToCase() {
        when(legalRepresentativesDifferenceCalculator.calculate(
            LEGAL_REPRESENTATIVES_BEFORE, LEGAL_REPRESENTATIVES_NOW)
        ).thenReturn(LegalRepresentativesChange.builder()
            .added(Set.of(LEGAL_REPRESENTATIVE))
            .build()
        );

        when(legalRepresentativeAddedContentProvider.getParameters(LEGAL_REPRESENTATIVE, CASE_DATA)).thenReturn(
            TEMPLATE_PARAMETERS);

        underTest.sendEmailToLegalRepresentativesAddedToCase(
            new LegalRepresentativesChangeToCaseEvent(CASE_DATA, CASE_DATA_BEFORE)
        );

        verify(notificationService).sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE,
            REPRESENTATIVE_EMAIL,
            TEMPLATE_PARAMETERS,
            CASE_ID
        );
    }

    @Test
    void sendEmailToLegalRepresentativesAddedToCaseMultiple() {
        when(legalRepresentativesDifferenceCalculator.calculate(
            LEGAL_REPRESENTATIVES_BEFORE, LEGAL_REPRESENTATIVES_NOW)
        ).thenReturn(LegalRepresentativesChange.builder()
            .added(Set.of(LEGAL_REPRESENTATIVE, LEGAL_REPRESENTATIVE_2))
            .build()
        );
        when(legalRepresentativeAddedContentProvider.getParameters(LEGAL_REPRESENTATIVE, CASE_DATA)).thenReturn(
            TEMPLATE_PARAMETERS);
        when(legalRepresentativeAddedContentProvider.getParameters(LEGAL_REPRESENTATIVE_2, CASE_DATA)).thenReturn(
            TEMPLATE_PARAMETERS_2);

        underTest.sendEmailToLegalRepresentativesAddedToCase(
            new LegalRepresentativesChangeToCaseEvent(CASE_DATA, CASE_DATA_BEFORE)
        );

        verify(notificationService).sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE,
            REPRESENTATIVE_EMAIL,
            TEMPLATE_PARAMETERS,
            CASE_ID
        );
        verify(notificationService).sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE,
            REPRESENTATIVE_EMAIL_2,
            TEMPLATE_PARAMETERS_2,
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
            new LegalRepresentativesChangeToCaseEvent(CASE_DATA, CASE_DATA_BEFORE)
        );

        verifyNoInteractions(legalRepresentativeAddedContentProvider,notificationService);
    }
}
