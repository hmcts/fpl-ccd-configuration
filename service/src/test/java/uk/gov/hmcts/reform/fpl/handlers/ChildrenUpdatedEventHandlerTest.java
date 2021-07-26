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
    private static final String EMAIL_2 = "email2";
    private static final Long CASE_ID = 123L;

    private final UnregisteredRepresentativeSolicitorTemplate unregisteredTemplate = mock(
        UnregisteredRepresentativeSolicitorTemplate.class
    );
    private final UnregisteredRepresentativeSolicitorTemplate unregisteredTemplate2 = mock(
        UnregisteredRepresentativeSolicitorTemplate.class
    );
    private final RegisteredRepresentativeSolicitorTemplate registeredTemplate = mock(
        RegisteredRepresentativeSolicitorTemplate.class
    );
    private final RegisteredRepresentativeSolicitorTemplate registeredTemplate2 = mock(
        RegisteredRepresentativeSolicitorTemplate.class
    );
    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);
    private final Child child = mock(Child.class);
    private final Child child2 = mock(Child.class);
    private final Child child3 = mock(Child.class);
    private final List<Element<Child>> children = wrapElements(child);
    private final RespondentSolicitor solicitor = mock(RespondentSolicitor.class);
    private final RespondentSolicitor solicitor2 = mock(RespondentSolicitor.class);

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

        when(calculator.getRegisteredDiff(children, children)).thenReturn(List.of(child, child2, child3));

        when(child.getSolicitor()).thenReturn(solicitor);
        when(child2.getSolicitor()).thenReturn(solicitor2);
        when(child3.getSolicitor()).thenReturn(solicitor2);

        when(registeredContentProvider.buildContent(caseData, solicitor, List.of(child)))
            .thenReturn(registeredTemplate);
        when(solicitor.getEmail()).thenReturn(EMAIL);

        when(registeredContentProvider.buildContent(caseData, solicitor2, List.of(child2, child3)))
            .thenReturn(registeredTemplate2);
        when(solicitor2.getEmail()).thenReturn(EMAIL_2);

        when(caseData.getId()).thenReturn(CASE_ID);

        underTest.notifyRegisteredSolicitors(new ChildrenUpdated(caseData, caseDataBefore));

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, EMAIL, registeredTemplate, CASE_ID
        );
        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, EMAIL_2, registeredTemplate2, CASE_ID
        );
    }

    @Test
    void notifyUnRegisteredSolicitors() {
        when(caseData.getAllChildren()).thenReturn(children);
        when(caseDataBefore.getAllChildren()).thenReturn(children);

        when(calculator.getUnregisteredDiff(children, children)).thenReturn(List.of(child, child2, child3));

        when(child.getSolicitor()).thenReturn(solicitor);
        when(child2.getSolicitor()).thenReturn(solicitor2);
        when(child3.getSolicitor()).thenReturn(solicitor2);

        when(unregisteredContentProvider.buildContent(caseData, List.of(child)))
            .thenReturn(unregisteredTemplate);
        when(solicitor.getEmail()).thenReturn(EMAIL);

        when(unregisteredContentProvider.buildContent(caseData, List.of(child2, child3)))
            .thenReturn(unregisteredTemplate2);
        when(solicitor2.getEmail()).thenReturn(EMAIL_2);

        when(caseData.getId()).thenReturn(CASE_ID);

        underTest.notifyUnRegisteredSolicitors(new ChildrenUpdated(caseData, caseDataBefore));

        verify(notificationService).sendEmail(
            UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, EMAIL, unregisteredTemplate, CASE_ID
        );
        verify(notificationService).sendEmail(
            UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, EMAIL_2, unregisteredTemplate2, CASE_ID
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
