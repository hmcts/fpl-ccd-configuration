package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.PartyAddedNotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE_CHILD_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE_WITH_CHILD;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class PartyAddedToCaseEventHandlerTest {

    private static final List<Representative> EMAIL_REPS = List.of(mock(Representative.class));
    private static final List<Representative> DIGITAL_REPS = List.of(mock(Representative.class));
    private static final PartyAddedNotifyData DIGITAL_REP_NOTIFY_DATA = mock(PartyAddedNotifyData.class);
    private static final PartyAddedNotifyData EMIAL_REP_NOTIFY_DATA = mock(PartyAddedNotifyData.class);
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final CaseData CASE_DATA_BEFORE = mock(CaseData.class);
    private static final long CASE_ID = 12345L;

    @Mock
    private RepresentativeNotificationService notificationService;
    @Mock
    private RepresentativeService representativeService;
    @Mock
    private PartyAddedToCaseContentProvider contentProvider;
    @Mock
    private FeatureToggleService toggleService;

    @InjectMocks
    private PartyAddedToCaseEventHandler underTest;


    @BeforeEach
    void init() {
        List<Element<Representative>> reps = wrapElements(mock(Representative.class));
        List<Element<Representative>> repsBefore = wrapElements(mock(Representative.class));

        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(CASE_DATA.getRepresentatives()).willReturn(reps);
        given(CASE_DATA_BEFORE.getRepresentatives()).willReturn(repsBefore);

        given(representativeService.getUpdatedRepresentatives(reps, repsBefore, EMAIL))
            .willReturn(EMAIL_REPS);
        given(representativeService.getUpdatedRepresentatives(reps, repsBefore, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REPS);

        given(contentProvider.getPartyAddedToCaseNotificationParameters(CASE_DATA, EMAIL))
            .willReturn(EMIAL_REP_NOTIFY_DATA);
        given(contentProvider.getPartyAddedToCaseNotificationParameters(CASE_DATA, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REP_NOTIFY_DATA);

        given(toggleService.isEldestChildLastNameEnabled()).willReturn(false);
    }

    @ParameterizedTest
    @MethodSource("templateSource")
    void notifyParties(boolean toggle, String emailRepsTemplate, String digitalRepsTemplate) {
        given(toggleService.isEldestChildLastNameEnabled()).willReturn(toggle);

        underTest.notifyParties(new PartyAddedToCaseEvent(CASE_DATA, CASE_DATA_BEFORE));

        verify(notificationService).sendToUpdatedRepresentatives(
            emailRepsTemplate, EMIAL_REP_NOTIFY_DATA, CASE_DATA, EMAIL_REPS
        );

        verify(notificationService).sendToUpdatedRepresentatives(
            digitalRepsTemplate, DIGITAL_REP_NOTIFY_DATA, CASE_DATA, DIGITAL_REPS
        );
    }

    private static Stream<Arguments> templateSource() {
        return Stream.of(
            Arguments.of(
                false,
                PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE,
                PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE
            ),
            Arguments.of(
                true,
                PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE_CHILD_NAME,
                PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE_WITH_CHILD
            )
        );
    }
}
