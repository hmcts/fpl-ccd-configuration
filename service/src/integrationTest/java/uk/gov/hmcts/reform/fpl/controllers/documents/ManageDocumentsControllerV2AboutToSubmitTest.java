package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageDocumentEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ManageDocumentsControllerV2.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerV2AboutToSubmitTest extends AbstractCallbackTest {

    private static final long CASE_ID = 12345L;

    @MockBean
    private UserService userService;

    @MockBean
    private ManageDocumentService manageDocumentService;

    ManageDocumentsControllerV2AboutToSubmitTest() {
        super("manage-documentsv2");
    }

    private UserDetails buildUserDetailsWithLARole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Swansea")
            .forename("Kurt")
            .email("kurt@swansea.gov.uk")
            .roles(List.of("caseworker-publiclaw-solicitor"))
            .build();
    }

    private UserDetails buildUserDetailsWithCourtAdminRole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("steve.hudson@gov.uk")
            .roles(List.of("caseworker-publiclaw-courtadmin"))
            .build();
    }

    private UserDetails buildUserDetailsWithSolicitorRole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("solicitor1@solicitors.uk")
            .roles(List.of("caseworker-publiclaw-solicitor"))
            .build();
    }

    @ParameterizedTest
    @EnumSource(value = ManageDocumentAction.class, names = {"UPLOAD_DOCUMENTS", "REMOVE_DOCUMENTS"})
    void shouldNotPopulateTemporaryFieldsByLA(ManageDocumentAction action) {
        givenCurrentUser(buildUserDetailsWithLARole());
        given(userService.getCaseRoles(eq(CASE_ID))).willReturn(newHashSet(LASOLICITOR));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(action)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        CaseData responseData = extractCaseData(response);

        assertExpectedFieldsAreRemoved(responseData);
    }

    @ParameterizedTest
    @EnumSource(value = ManageDocumentAction.class, names = {"UPLOAD_DOCUMENTS", "REMOVE_DOCUMENTS"})
    void shouldNotPopulateTemporaryFieldsByCourtAdmin(ManageDocumentAction action) {
        givenCurrentUser(buildUserDetailsWithCourtAdminRole());
        given(userService.getCaseRoles(eq(CASE_ID))).willReturn(newHashSet());
        given(userService.isHmctsUser()).willReturn(true);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(action)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        CaseData responseData = extractCaseData(response);

        assertExpectedFieldsAreRemoved(responseData);
    }

    @ParameterizedTest
    @EnumSource(value = ManageDocumentAction.class, names = {"UPLOAD_DOCUMENTS", "REMOVE_DOCUMENTS"})
    void shouldNotPopulateTemporaryFieldsBySolicitor(ManageDocumentAction action) {
        givenCurrentUser(buildUserDetailsWithSolicitorRole());
        given(userService.getCaseRoles(eq(CASE_ID))).willReturn(newHashSet(SOLICITORA));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(action)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        CaseData responseData = extractCaseData(response);

        assertExpectedFieldsAreRemoved(responseData);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldPopulateFieldsInUploadDocumentAction(int testingType) {
        switch (testingType) {
            case 1:
                givenCurrentUser(buildUserDetailsWithLARole());
                given(userService.getCaseRoles(eq(CASE_ID))).willReturn(newHashSet(LASOLICITOR));
                given(userService.isHmctsUser()).willReturn(false);
                break;
            case 2:
                givenCurrentUser(buildUserDetailsWithCourtAdminRole());
                given(userService.getCaseRoles(eq(CASE_ID))).willReturn(newHashSet());
                given(userService.isHmctsUser()).willReturn(true);
                break;
            case 3:
                givenCurrentUser(buildUserDetailsWithSolicitorRole());
                given(userService.getCaseRoles(eq(CASE_ID))).willReturn(newHashSet(SOLICITORA));;
                given(userService.isHmctsUser()).willReturn(false);
                break;
        }

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.UPLOAD_DOCUMENTS)
                .build())
            .build();

        Element<ManagedDocument> expectedElement = element(ManagedDocument.builder()
            .document(TestDataHelper.testDocumentReference()).build());
        when(manageDocumentService.uploadDocuments(any()))
            .thenReturn(Map.of("parentAssessmentList", List.of(expectedElement)));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        assertThat((List) response.getData().get("parentAssessmentList")).isNotEmpty();

        CaseData extractedCaseData = extractCaseData(response);
        assertThat(extractedCaseData.getParentAssessmentList()).contains(expectedElement);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldPopulateFieldsInRemoveDocumentAction(int testingType) {
        switch (testingType) {
            case 1:
                givenCurrentUser(buildUserDetailsWithLARole());
                given(userService.getCaseRoles(eq(CASE_ID))).willReturn(newHashSet(LASOLICITOR));
                given(userService.isHmctsUser()).willReturn(false);
                break;
            case 2:
                givenCurrentUser(buildUserDetailsWithCourtAdminRole());
                given(userService.getCaseRoles(eq(CASE_ID))).willReturn(newHashSet());
                given(userService.isHmctsUser()).willReturn(true);
                break;
            case 3:
                givenCurrentUser(buildUserDetailsWithSolicitorRole());
                given(userService.getCaseRoles(eq(CASE_ID))).willReturn(newHashSet(SOLICITORA));;
                given(userService.isHmctsUser()).willReturn(false);
                break;
        }

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .build())
            .build();

        Element<ManagedDocument> expectedElement = element(ManagedDocument.builder()
            .document(TestDataHelper.testDocumentReference()).build());
        when(manageDocumentService.removeDocuments(any()))
            .thenReturn(Map.of("parentAssessmentListRemoved", List.of(expectedElement)));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        assertThat((List) response.getData().get("parentAssessmentListRemoved")).isNotEmpty();

        CaseData extractedCaseData = extractCaseData(response);
        assertThat(extractedCaseData.getParentAssessmentListRemoved()).contains(expectedElement);
    }

    private void assertExpectedFieldsAreRemoved(CaseData caseData) {
        assertThat(caseData.getManageDocumentEventData().getManageDocumentAction()).isNull();
        assertThat(caseData.getManageDocumentEventData().getUploadableDocumentBundle()).isEmpty();
        assertThat(caseData.getManageDocumentEventData().getHasConfidentialParty()).isNull();
        assertThat(caseData.getManageDocumentEventData().getAskForPlacementNoticeRecipientType()).isNull();
        assertThat(caseData.getManageDocumentEventData().getDocumentAcknowledge()).isNull();
        assertThat(caseData.getManageDocumentEventData().getAllowMarkDocumentConfidential()).isNull();
    }
}
