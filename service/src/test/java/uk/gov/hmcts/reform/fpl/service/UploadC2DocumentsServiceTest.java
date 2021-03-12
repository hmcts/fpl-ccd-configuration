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
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
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
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

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
        assertThat(firstC2DocumentBundle).isEqualToComparingFieldByField(expectedC2Bundle);
    }

    @Test
    void shouldBuildExpectedC2DocumentBundleWhenAdditionalApplicationsToggledOn() {
        given(featureToggleService.isUploadAdditionalApplicationsEnabled()).willReturn(true);

        CaseData caseData = CaseData.builder()
            .temporaryC2Document(createC2DocumentBundleWithSupplements())
            .c2ApplicationType(Map.of("type", WITH_NOTICE))
            .build();

        List<Element<C2DocumentBundle>> actualC2DocumentBundleList = service
            .buildC2DocumentBundle(caseData);
        C2DocumentBundle actualC2DocumentBundle = actualC2DocumentBundleList.get(0).getValue();


        assertC2Bundle(actualC2DocumentBundle);
        assertThat(actualC2DocumentBundle.getSupplementsBundle().get(0).getValue())
            .extracting("name", "notes")
            .containsExactly(C13A_SPECIAL_GUARDIANSHIP, "Document notes");
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
    }

    private C2DocumentBundle createC2DocumentBundle() {
        return C2DocumentBundle.builder()
            .author("Elon Musk")
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
