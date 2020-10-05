package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
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

    @Test
    void shouldBuildExpectedC2DocumentBundle() {
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(UserInfo.builder().name("Emma Taylor").build());
        given(idamClient.getUserDetails(eq(USER_AUTH_TOKEN))).willReturn(createUserDetailsWithHmctsRole());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);

        List<Element<C2DocumentBundle>> actualC2DocumentBundle = service
            .buildC2DocumentBundle(createCaseDataWithC2DocumentBundle());

        List<C2DocumentBundle> actualC2Bundle = unwrapElements(actualC2DocumentBundle);
        C2DocumentBundle expectedC2Bundle = createC2DocumentBundle();

        assertThat(actualC2Bundle).first().isEqualToComparingFieldByField(expectedC2Bundle);
    }

    @Test
    void shouldReturnErrorsWhenTheDateOfIssueIsInFuture() {
        assertThat(service.validate(createC2DocumentBundle()).toArray()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnEmptyListWhenNoSupportingDocuments() {
        assertThat(service.validate(createC2DocumentBundleWithNoSupportingDocuments())).isEmpty();
    }

    private C2DocumentBundle createC2DocumentBundle() {
        return C2DocumentBundle.builder()
            .author("Elon Musk")
            .type(C2ApplicationType.WITH_NOTICE)
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

    private CaseData createCaseDataWithC2DocumentBundle() {
        return CaseData.builder()
            .c2DocumentBundle(wrapElements(createC2DocumentBundle()))
            .temporaryC2Document(createC2DocumentBundle())
            .c2ApplicationType(Map.of("type",C2ApplicationType.WITH_NOTICE))
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
