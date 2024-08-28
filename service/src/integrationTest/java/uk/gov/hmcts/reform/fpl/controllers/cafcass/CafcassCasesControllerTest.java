package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.BOY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS_SYSTEM_UPDATE;

@Deprecated
@WebMvcTest(CafcassCasesController.class)
@OverrideAutoConfiguration(enabled = true)
public class CafcassCasesControllerTest extends AbstractTest {
    private static final UserInfo CAFCASS_SYSTEM_UPDATE_USER_INFO = UserInfo.builder()
        .roles(List.of(CAFCASS_SYSTEM_UPDATE.getRoleName()))
            .name("Cafcass")
            .familyName("System User")
        .build();
    private static final UserDetails CAFCASS_SYSTEM_UPDATE_USER_DETAIL = UserDetails.builder()
            .roles(List.of(CAFCASS_SYSTEM_UPDATE.getRoleName()))
            .forename("Cafcass")
            .surname("System User")
            .build();
    private static final long CASE_ID = 1583841721773828L;
    private static final  byte[] FILE_BYTES = "This is a file. Trust me!".getBytes();
    private static final MockMultipartFile FILE = new MockMultipartFile(
        "file", "MOCK_FILE.pdf", MediaType.APPLICATION_PDF_VALUE, FILE_BYTES);
    private static final DocumentReference DOCUMENT_REFERENCE = testDocumentReference();
    private static final Document DOCUMENT = testDocument();
    private final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
        .id("ORG1")
        .name("Designated LA")
        .email("designated@test.com")
        .designated("Yes")
        .build();
    private final OrganisationPolicy designatedPolicy = organisationPolicy("ORG1", "Designated LA", LASOLICITOR);
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UploadDocumentService uploadDocumentService;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private UserService userService;


    @BeforeEach
    void setUp() {
        givenCurrentUser(CAFCASS_SYSTEM_UPDATE_USER_INFO);
        givenCurrentUser(CAFCASS_SYSTEM_UPDATE_USER_DETAIL);
    }

    @Test
    void uploadDocument() throws Exception {
        UUID caseId = UUID.randomUUID();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseName("Test case")
            .localAuthorityPolicy(designatedPolicy)
            .localAuthorities(wrapElements(designatedLocalAuthority))
            .children1(List.of(testChild("Alex", "White", BOY, LocalDate.now())))
            .build();

        CaseDetails caseDetailsBefore = asCaseDetails(caseData);

        CaseDetails caseDetailsUpdated = asCaseDetails(
            caseData.toBuilder().guardianReportsList(wrapElementsWithUUIDs(List.of(
                    ManagedDocument.builder()
                        .document(DOCUMENT_REFERENCE)
                        .uploaderCaseRoles(List.of(CaseRole.CREATOR))
                        .uploaderType(DocumentUploaderType.CAFCASS)
                        .build()))).build());

        when(uploadDocumentService.uploadDocument(any(), any(), any())).thenReturn(DOCUMENT);
        when(coreCaseDataService.performPostSubmitCallback(any(), eq("internal-upload-document"), any()))
            .thenReturn(caseDetailsUpdated);
        when(coreCaseDataService.findCaseDetailsById(any())).thenReturn(caseDetailsBefore);
        when(userService.getCaseRoles(any())).thenReturn((Set.of(CaseRole.CREATOR)));
        when(userService.getIdamRoles()).thenReturn(Set.of(UserRole.CAFCASS_SYSTEM_UPDATE.getRoleName()));
        when(userService.getUserDetails()).thenReturn(CAFCASS_SYSTEM_UPDATE_USER_DETAIL);

        MvcResult response = mockMvc
            .perform(MockMvcRequestBuilders.multipart("/cases/%s/document".formatted(caseId))
                .file(FILE)
                .param("typeOfDocument", "GUARDIAN_REPORT")
                .header("authorization", USER_AUTH_TOKEN))
            .andExpect(status().is(200))
            .andReturn();

        assertEquals("uploadDocument - caseId: [%s], file length: [%s], typeOfDocument: [%s]"
                .formatted(caseId, FILE_BYTES.length, "GUARDIAN_REPORT"),
            response.getResponse().getContentAsString());
    }

    @Test
    void uploadDocument400() throws Exception {
        UUID caseId = UUID.randomUUID();

        MvcResult response = mockMvc
            .perform(MockMvcRequestBuilders.multipart("/cases/%s/document".formatted(caseId))
                .param("typeOfDocument", "type Of Document")
                .header("authorization", USER_AUTH_TOKEN))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);


        byte[] fileBytes = "This is a file. Trust me!".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "MOCK_FILE.pdf", MediaType.TEXT_PLAIN_VALUE, fileBytes);

        response = mockMvc
            .perform(MockMvcRequestBuilders.multipart("/cases/%s/document".formatted(caseId))
                .file(file)
                .header("authorization", USER_AUTH_TOKEN))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);
    }
}
