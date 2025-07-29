package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_FORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.BARRISTER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.LegalCounsellorTestHelper.buildLegalCounsellor;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLegalCounselControllerSubmittedTest extends AbstractCallbackTest {

    @MockBean
    private CaseAccessService caseAccessService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private CaseRoleLookupService caseRoleLookupService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> emailVariablesCaptor;

    ManageLegalCounselControllerSubmittedTest() {
        super("manage-legal-counsel");
    }

    @BeforeEach
    void setUp() {
        when(organisationService.findOrganisation())
            .thenReturn(Optional.of(Organisation.builder().name("Solicitors Ltd").build()));
        when(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(TEST_CASE_ID))
            .thenReturn(List.of(SolicitorRole.CHILDSOLICITORA));
        when(caseRoleLookupService.getCaseSolicitorRolesByCaseRoles(any())).thenCallRealMethod();
        when(caseAccessService.getUserCaseRoles(TEST_CASE_ID)).thenReturn(Set.of(CaseRole.CHILDSOLICITORA));
    }

    @Test
    void shouldUpdateCaseRoleAndNotifyModifiedLegalCounsellorsWhenSubmittedEndpointIsCalled()
        throws NotificationClientException {
        LegalCounsellor addedLegalCounsellor = buildLegalCounsellor("1");
        LegalCounsellor maintainedLegalCounsellor = buildLegalCounsellor("2");
        LegalCounsellor removedLegalCounsellor = buildLegalCounsellor("3");

        List<Element<Child>> childrenInPreviousCaseData = testChildren();
        childrenInPreviousCaseData.get(0).getValue().setLegalCounsellors(wrapElements(
            maintainedLegalCounsellor, removedLegalCounsellor
        ));
        CaseDetails previousCaseDetails = buildCaseDetailsWithGivenChildren(childrenInPreviousCaseData);

        List<Element<Child>> childrenInCurrentCaseData = new ArrayList<>(childrenInPreviousCaseData);
        childrenInCurrentCaseData.get(0).getValue().setLegalCounsellors(wrapElements(
            addedLegalCounsellor, maintainedLegalCounsellor
        ));
        CaseDetails currentCaseDetails = buildCaseDetailsWithGivenChildren(childrenInCurrentCaseData);

        postSubmittedEvent(toCallBackRequest(currentCaseDetails, previousCaseDetails));

        String childLastName = childrenInCurrentCaseData.get(0).getValue().getParty().getLastName();
        assertAdditionAndNotification(addedLegalCounsellor, childLastName);
        assertRemovalAndNotification(removedLegalCounsellor, childLastName);
    }

    private CaseDetails buildCaseDetailsWithGivenChildren(List<Element<Child>> children) {
        return asCaseDetails(
            CaseData.builder().children1(children).caseName("testCaseName").id(TEST_CASE_ID).build()
        );
    }

    private void assertAdditionAndNotification(LegalCounsellor addedLegalCounsellor,
                                               String childLastName) throws NotificationClientException {
        verify(caseAccessService).grantCaseRoleToUser(TEST_CASE_ID, "testUserId1", BARRISTER);
        verify(notificationClient).sendEmail(
            any(),
            eq(addedLegalCounsellor.getEmail()),
            emailVariablesCaptor.capture(), eq(notificationReference(TEST_CASE_ID)));

        assertThat(emailVariablesCaptor.getValue())
            .containsEntry("childLastName", childLastName)
            .containsEntry("caseID", TEST_FORMATTED_CASE_ID)
            .containsEntry("caseUrl", caseUrl(TEST_CASE_ID));
    }

    private void assertRemovalAndNotification(LegalCounsellor removedLegalCounsellor,
                                              String childLastName) throws NotificationClientException {
        verify(caseAccessService).revokeCaseRoleFromUser(TEST_CASE_ID, "testUserId3", BARRISTER);
        verify(notificationClient).sendEmail(
            any(), eq(removedLegalCounsellor.getEmail()),
            emailVariablesCaptor.capture(),
            eq(notificationReference(TEST_CASE_ID))
        );

        assertThat(emailVariablesCaptor.getValue())
            .containsEntry("caseName", "testCaseName")
            .containsEntry("childLastName", childLastName)
            .containsEntry("salutation",
                format("Dear %s %s", removedLegalCounsellor.getFirstName(), removedLegalCounsellor.getLastName()))
            .containsEntry("clientFullName", "Solicitors Ltd")
            .containsEntry("ccdNumber", TEST_FORMATTED_CASE_ID);
    }

}
