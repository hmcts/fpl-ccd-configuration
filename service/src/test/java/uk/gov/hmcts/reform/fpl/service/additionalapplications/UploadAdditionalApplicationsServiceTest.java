package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.USER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C12_WARRANT_TO_ASSIST_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    UploadAdditionalApplicationsService.class,
    FixedTimeConfiguration.class,
    DocumentUploadHelper.class
})
class UploadAdditionalApplicationsServiceTest {
    private static final String USER_ID = "1";
    public static final String HMCTS = "HMCTS";
    public static final DocumentReference DOCUMENT = testDocumentReference();

    @Autowired
    private UploadAdditionalApplicationsService service;

    @Autowired
    private Time time;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @BeforeEach()
    void init() {
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(UserInfo.builder().name("Emma Taylor").build());
        given(idamClient.getUserDetails(eq(USER_AUTH_TOKEN))).willReturn(createUserDetailsWithHmctsRole());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
    }

    @Test
    @Disabled
    void shouldBuildExpectedC2DocumentBundle() {
        //TODO: fix
        CaseData caseData = CaseData.builder()
            .temporaryC2Document(createC2DocumentBundleWithSupplements())
            .c2ApplicationType(Map.of("type", WITH_NOTICE))
            .temporaryPbaPayment(PBAPayment.builder()
                .pbaNumber("PBA1234567")
                .clientCode("123")
                .fileReference("456").build())
            .build();

        C2DocumentBundle actualC2DocumentBundle = service.buildC2DocumentBundle(caseData);

        assertC2Bundle(actualC2DocumentBundle);
        assertThat(actualC2DocumentBundle.getSupplementsBundle().get(0).getValue())
            .extracting("name", "notes")
            .containsExactly(C13A_SPECIAL_GUARDIANSHIP, "Document notes");
    }

    @Test
    void shouldBuildOtherApplicationsBundle() {
        SupplementsBundle supplementsBundle = createSupplementsBundle();
        SupportingEvidenceBundle supportingDocument = buildSupportingEvidenceBundle();

        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationsBundle(
            supplementsBundle, supportingDocument);

        OtherApplicationsBundle actualOtherApplicationsBundle = service.buildOtherApplicationsBundle(
            CaseData.builder().temporaryOtherApplicationsBundle(otherApplicationsBundle).build());

        assertThat(actualOtherApplicationsBundle).isNotNull();
        assertThat(actualOtherApplicationsBundle.getApplicationType())
            .isEqualTo(otherApplicationsBundle.getApplicationType());
        assertThat(actualOtherApplicationsBundle.getAuthor()).isEqualTo(HMCTS);

        assertThat(actualOtherApplicationsBundle.getSupplementsBundle().get(0).getValue())
            .extracting("name", "notes", "document", "uploadedBy")
            .containsExactly(
                supplementsBundle.getName(), supplementsBundle.getNotes(), supplementsBundle.getDocument(), HMCTS);

        assertThat(actualOtherApplicationsBundle.getSupportingEvidenceBundle().get(0).getValue())
            .extracting("name", "notes", "document", "uploadedBy")
            .containsExactly(
                supportingDocument.getName(), supportingDocument.getNotes(), supportingDocument.getDocument(), HMCTS);
    }

    @Test
    void shouldBuildAdditionalApplicationsBundle() {
        SupplementsBundle supplementsBundle = createSupplementsBundle();
        SupportingEvidenceBundle supportingDocument = buildSupportingEvidenceBundle();

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .type(WITH_NOTICE)
            .supplementsBundle(wrapElements(supplementsBundle))
            .supportingEvidenceBundle(wrapElements(supportingDocument)).build();

        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationsBundle(
            supplementsBundle, supportingDocument);

        PBAPayment pbaPayment = PBAPayment.builder().usePbaPayment("Yes").usePbaPayment("PBA12345").build();

        AdditionalApplicationsBundle actual = service.buildAdditionalApplicationsBundle(
            CaseData.builder().temporaryPbaPayment(pbaPayment).build(),
            c2DocumentBundle,
            otherApplicationsBundle);

        assertThat(actual).extracting("author", "c2Document", "otherApplications", "pbaPayment")
            .containsExactly(HMCTS, c2DocumentBundle, otherApplicationsBundle, pbaPayment);
    }

    private OtherApplicationsBundle buildOtherApplicationsBundle(
        SupplementsBundle supplementsBundle, SupportingEvidenceBundle supportingDocument1) {
        return OtherApplicationsBundle.builder()
            .applicationType(C12_WARRANT_TO_ASSIST_PERSON)
            .supplementsBundle(wrapElements(supplementsBundle))
            .supportingEvidenceBundle(wrapElements(supportingDocument1))
            .build();
    }

    private SupportingEvidenceBundle buildSupportingEvidenceBundle() {
        return SupportingEvidenceBundle.builder()
            .name("supporting document1")
            .notes("note1")
            .document(DOCUMENT).build();
    }

    private C2DocumentBundle createC2DocumentBundleWithSupplements() {
        return C2DocumentBundle.builder()
            .type(WITH_NOTICE)
            .document(DocumentReference.builder()
                .filename("Test")
                .build())
            .supportingEvidenceBundle(wrapElements(createSupportingEvidenceBundleWithInvalidDateReceived()))
            .supplementsBundle(wrapElements(createSupplementsBundle()))
            .build();
    }

    private void assertC2Bundle(C2DocumentBundle documentBundle) {
        assertThat(documentBundle.getDocument().getFilename()).isEqualTo("Test");
        assertThat(documentBundle.getType()).isEqualTo(WITH_NOTICE);
        assertThat(documentBundle.getSupportingEvidenceBundle()).hasSize(1);
        assertThat(documentBundle.getSupplementsBundle()).hasSize(1);
        assertThat(documentBundle.getPbaNumber()).isEqualTo("PBA1234567");
        assertThat(documentBundle.getClientCode()).isEqualTo("123");
        assertThat(documentBundle.getFileReference()).isEqualTo("456");
    }

    private SupportingEvidenceBundle createSupportingEvidenceBundleWithInvalidDateReceived() {
        return SupportingEvidenceBundle.builder()
            .name("Supporting document")
            .notes("Document notes")
            .dateTimeReceived(time.now().plusDays(1))
            .build();
    }

    private SupplementsBundle createSupplementsBundle() {
        return SupplementsBundle.builder()
            .name(C13A_SPECIAL_GUARDIANSHIP)
            .notes("Document notes")
            .build();
    }

    private UserDetails createUserDetailsWithHmctsRole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("steve.hudson@gov.uk")
            .roles(Arrays.asList("caseworker-publiclaw-courtadmin", "caseworker-publiclaw-judiciary"))
            .build();
    }
}
