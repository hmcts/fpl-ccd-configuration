package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.ChildrenUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.representative.RegisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.representative.UnregisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.RegisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.UnregisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.diff.ChildRepresentativeDiffCalculator;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

class ChildrenUpdatedEventHandlerTest {
    private static final String EMAIL = "email";
    private static final Long CASE_ID = 123L;

    private final UnregisteredRepresentativeSolicitorTemplate unregisteredTemplate = mock(
        UnregisteredRepresentativeSolicitorTemplate.class
    );
    private final RegisteredRepresentativeSolicitorTemplate registeredTemplate = mock(
        RegisteredRepresentativeSolicitorTemplate.class
    );
    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);
    private final Child child = mock(Child.class);
    private final List<Element<Child>> children = wrapElements(child);
    private final RespondentSolicitor solicitor = mock(RespondentSolicitor.class);

    private final RegisteredRepresentativeSolicitorContentProvider registeredContentProvider = mock(
        RegisteredRepresentativeSolicitorContentProvider.class
    );
    private final UnregisteredRepresentativeSolicitorContentProvider unregisteredContentProvider = mock(
        UnregisteredRepresentativeSolicitorContentProvider.class
    );
    private final ChildRepresentativeDiffCalculator calculator = mock(ChildRepresentativeDiffCalculator.class);
    private final NotificationService notificationService = mock(NotificationService.class);

    private final ChildrenUpdatedEventHandler underTest = new ChildrenUpdatedEventHandler(
        registeredContentProvider, unregisteredContentProvider, calculator, notificationService
    );

    @Test
    void notifyRegisteredSolicitors() {
        when(caseData.getAllChildren()).thenReturn(children);
        when(caseDataBefore.getAllChildren()).thenReturn(children);

        when(calculator.getRegisteredDiff(children, children)).thenReturn(List.of(child));
        when(registeredContentProvider.buildContent(caseData, child)).thenReturn(registeredTemplate);

        when(child.getSolicitor()).thenReturn(solicitor);
        when(solicitor.getEmail()).thenReturn(EMAIL);

        when(caseData.getId()).thenReturn(CASE_ID);

        underTest.notifyRegisteredSolicitors(new ChildrenUpdated(caseData, caseDataBefore));

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, EMAIL, registeredTemplate, CASE_ID
        );
    }

    @Test
    void notifyUnRegisteredSolicitors() {
        when(caseData.getAllChildren()).thenReturn(children);
        when(caseDataBefore.getAllChildren()).thenReturn(children);

        when(calculator.getUnregisteredDiff(children, children)).thenReturn(List.of(child));
        when(unregisteredContentProvider.buildContent(caseData, child)).thenReturn(unregisteredTemplate);

        when(child.getSolicitor()).thenReturn(solicitor);
        when(solicitor.getEmail()).thenReturn(EMAIL);

        when(caseData.getId()).thenReturn(CASE_ID);

        underTest.notifyUnRegisteredSolicitors(new ChildrenUpdated(caseData, caseDataBefore));

        verify(notificationService).sendEmail(
            UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, EMAIL, unregisteredTemplate, CASE_ID
        );
    }

    @Test
    void noNotifications() {
        when(caseData.getAllChildren()).thenReturn(children);
        when(caseDataBefore.getAllChildren()).thenReturn(children);

        when(calculator.getUnregisteredDiff(children, children)).thenReturn(List.of());
        when(calculator.getRegisteredDiff(children, children)).thenReturn(List.of());

        underTest.notifyUnRegisteredSolicitors(new ChildrenUpdated(caseData, caseDataBefore));
        underTest.notifyRegisteredSolicitors(new ChildrenUpdated(caseData, caseDataBefore));

        verifyNoInteractions(notificationService, registeredContentProvider, unregisteredContentProvider);
    }

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(ChildrenUpdatedEventHandler.class).hasAsyncMethods(
            "notifyRegisteredSolicitors", "notifyUnRegisteredSolicitors"
        );
    }
}
