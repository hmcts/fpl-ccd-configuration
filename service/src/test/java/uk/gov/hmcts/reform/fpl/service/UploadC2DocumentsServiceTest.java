package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.List;
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
    UploadC2DocumentsService.class,
    ValidateGroupService.class,
    SupportingEvidenceValidatorService.class,
    LocalValidatorFactoryBean.class,
    FixedTimeConfiguration.class,
    DocumentUploadHelper.class
})
class UploadC2DocumentsServiceTest {
    private static final String ERROR_MESSAGE = "Date received cannot be in the future";
    private static final String USER_ID = "1";
    public static final String HMCTS = "HMCTS";
    public static final DocumentReference DOCUMENT = testDocumentReference();

    @Autowired
    private UploadC2DocumentsService service;

    @Autowired
    private Time time;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach()
    void init() {
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(UserInfo.builder().name("Emma Taylor").build());
        given(idamClient.getUserDetails(eq(USER_AUTH_TOKEN))).willReturn(createUserDetailsWithHmctsRole());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
    }

    @Test
    void shouldBuildExpectedC2DocumentBundleWhenAdditionalApplicationsToggledOff() {
        given(featureToggleService.isUploadAdditionalApplicationsEnabled()).willReturn(false);

        List<Element<C2DocumentBundle>> actualC2DocumentBundleList = service
            .buildC2DocumentBundle(createCaseDataWithC2DocumentBundle(createC2DocumentBundle()));
        C2DocumentBundle firstC2DocumentBundle = actualC2DocumentBundleList.get(0).getValue();
        C2DocumentBundle expectedC2Bundle = createC2DocumentBundle();
        assertThat(firstC2DocumentBundle).usingRecursiveComparison().isEqualTo(expectedC2Bundle);
    }

    @Test
    void shouldBuildExpectedC2DocumentBundleWhenAdditionalApplicationsToggledOn() {
        given(featureToggleService.isUploadAdditionalApplicationsEnabled()).willReturn(true);

        C2DocumentBundle c2DocumentBundle = createC2DocumentBundleWithSupplements();
        List<Element<C2DocumentBundle>> actualC2DocumentBundleList = service
            .buildC2DocumentBundle(createCaseDataWithC2DocumentBundle(c2DocumentBundle));

        C2DocumentBundle actualC2DocumentBundle = actualC2DocumentBundleList.get(0).getValue();

        assertThat(actualC2DocumentBundle.getType()).isEqualTo(c2DocumentBundle.getType());
        assertThat(actualC2DocumentBundle.getAuthor()).isEqualTo(HMCTS);

        SupplementsBundle supplementBundle = c2DocumentBundle.getSupplementsBundle().get(0).getValue();
        assertThat(actualC2DocumentBundle.getSupplementsBundle().get(0).getValue())
            .extracting("name", "notes", "document", "uploadedBy")
            .containsExactly(
                supplementBundle.getName(), supplementBundle.getNotes(), supplementBundle.getDocument(), HMCTS);

        SupportingEvidenceBundle supportingDocument =
            c2DocumentBundle.getSupportingEvidenceBundle().get(0).getValue();

        assertThat(actualC2DocumentBundle.getSupportingEvidenceBundle().get(0).getValue())
            .extracting("name", "notes", "document", "uploadedBy")
            .containsExactly(
                supportingDocument.getName(), supportingDocument.getNotes(), supportingDocument.getDocument(), HMCTS);
    }

    @Test
    void shouldReturnErrorsWhenTheDateOfIssueIsInFutureAndWhenAdditionalApplicationsToggledOff() {
        given(featureToggleService.isUploadAdditionalApplicationsEnabled()).willReturn(false);
        assertThat(service.validate(createC2DocumentBundle()).toArray()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnEmptyListWhenNoSupportingDocumentsAndWhenAdditionalApplicationsToggledOff() {
        given(featureToggleService.isUploadAdditionalApplicationsEnabled()).willReturn(false);
        assertThat(service.validate(createC2DocumentBundleWithNoSupportingDocuments())).isEmpty();
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
            .author(HMCTS)
            .type(WITH_NOTICE)
            .supportingEvidenceBundle(wrapElements(createSupportingEvidenceBundleWithInvalidDateReceived()))
            .supplementsBundle(wrapElements(createSupplementsBundle()))
            .build();
    }

    private C2DocumentBundle createC2DocumentBundle() {
        return C2DocumentBundle.builder()
            .author(HMCTS)
            .type(WITH_NOTICE)
            .supportingEvidenceBundle(wrapElements(createSupportingEvidenceBundleWithInvalidDateReceived()))
            .build();
    }

    private C2DocumentBundle createC2DocumentBundleWithNoSupportingDocuments() {
        return C2DocumentBundle.builder()
            .supportingEvidenceBundle(null)
            .build();
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

    private CaseData createCaseDataWithC2DocumentBundle(C2DocumentBundle c2DocumentBundle) {
        return CaseData.builder()
            .c2DocumentBundle(wrapElements(c2DocumentBundle))
            .temporaryC2Document(c2DocumentBundle)
            .c2ApplicationType(Map.of("type", WITH_NOTICE))
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
