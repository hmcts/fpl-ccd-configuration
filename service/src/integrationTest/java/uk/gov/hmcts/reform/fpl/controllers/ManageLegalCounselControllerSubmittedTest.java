package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID_AS_LONG;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_FORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.BARRISTER;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.LegalCounsellorTestHelper.buildLegalCounsellorWithOrganisationAndMockUserId;
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

    protected ManageLegalCounselControllerSubmittedTest() {
        super("manage-legal-counsel");
    }

    @BeforeEach
    void setUp() {
        when(organisationService.findOrganisation())
            .thenReturn(Optional.of(Organisation.builder().name("Solicitors Ltd").build()));
        when(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(TEST_CASE_ID))
            .thenReturn(asList(SolicitorRole.CHILDSOLICITORA));
    }

    @Test
    void shouldUpdateCaseRoleAndNotifyModifiedLegalCounsellorsWhenSubmittedEndpointIsCalled() {
        Pair<String, LegalCounsellor> addedLegalCounsellor =
            buildLegalCounsellorWithOrganisationAndMockUserId(organisationService, "1");
        Pair<String, LegalCounsellor> maintainedLegalCounsellor =
            buildLegalCounsellorWithOrganisationAndMockUserId(organisationService, "2");
        Pair<String, LegalCounsellor> removedLegalCounsellor =
            buildLegalCounsellorWithOrganisationAndMockUserId(organisationService, "3");

        List<Element<Child>> childrenInPreviousCaseData = testChildren();
        childrenInPreviousCaseData.get(0).getValue().setLegalCounsellors(asList(
            element(maintainedLegalCounsellor.getValue()),
            element(removedLegalCounsellor.getValue())
        ));
        CaseDetails previousCaseDetails = buildCaseDetailsWithGivenChildren(childrenInPreviousCaseData);

        ArrayList<Element<Child>> childrenInCurrentCaseData = new ArrayList<>(childrenInPreviousCaseData);
        childrenInCurrentCaseData.get(0).getValue().setLegalCounsellors(asList(
            element(addedLegalCounsellor.getValue()),
            element(maintainedLegalCounsellor.getValue())
        ));
        CaseDetails currentCaseDetails = buildCaseDetailsWithGivenChildren(childrenInCurrentCaseData);

        CallbackRequest callbackRequest = toCallBackRequest(currentCaseDetails, previousCaseDetails);
        postSubmittedEvent(callbackRequest);

        String childLastName = childrenInCurrentCaseData.get(0).getValue().getParty().getLastName();
        assertAsyncActionsHappenToAddedLegalCounsellor(addedLegalCounsellor, childLastName);
        assertAsyncActionsHappenToRemovedLegalCounsellor(removedLegalCounsellor, childLastName);
    }

    private CaseDetails buildCaseDetailsWithGivenChildren(List<Element<Child>> children) {
        return asCaseDetails(
            CaseData.builder()
                .children1(children)
                .caseName("testCaseName")
                .id(TEST_CASE_ID_AS_LONG)
                .build()
        );
    }

    private void assertAsyncActionsHappenToAddedLegalCounsellor(Pair<String, LegalCounsellor> addedLegalCounsellor,
                                                                String childLastName) {
        LegalCounsellor legalCounsellor = addedLegalCounsellor.getValue();
        checkUntil(() -> {
            verify(caseAccessService).grantCaseRoleToUser(TEST_CASE_ID_AS_LONG, "testUserId1", BARRISTER);
            verify(notificationClient).sendEmail(any(),
                eq(legalCounsellor.getEmail()),
                emailVariablesCaptor.capture(),
                argThat(endsWith(TEST_CASE_ID)));
            assertThat(emailVariablesCaptor.getValue())
                .containsEntry("childLastName", childLastName)
                .containsEntry("caseID", TEST_FORMATTED_CASE_ID)
                .hasEntrySatisfying("caseUrl", matching(endsWith("/cases/case-details/" + TEST_CASE_ID)));
        });
    }

    private void assertAsyncActionsHappenToRemovedLegalCounsellor(Pair<String, LegalCounsellor> removedLegalCounsellor,
                                                                  String childLastName) {
        LegalCounsellor legalCounsellor = removedLegalCounsellor.getValue();
        checkUntil(() -> {
            verify(caseAccessService).revokeCaseRoleFromUser(TEST_CASE_ID_AS_LONG, "testUserId3", BARRISTER);
            verify(notificationClient).sendEmail(any(),
                eq(legalCounsellor.getEmail()),
                emailVariablesCaptor.capture(),
                argThat(endsWith(TEST_CASE_ID)));
            assertThat(emailVariablesCaptor.getValue())
                .containsEntry("caseName", "testCaseName")
                .containsEntry("childLastName", childLastName)
                .containsEntry("salutation",
                    format("Dear %s %s", legalCounsellor.getFirstName(), legalCounsellor.getLastName()))
                .containsEntry("clientFullName", "Solicitors Ltd")
                .containsEntry("ccdNumber", TEST_FORMATTED_CASE_ID);
        });
    }

}
