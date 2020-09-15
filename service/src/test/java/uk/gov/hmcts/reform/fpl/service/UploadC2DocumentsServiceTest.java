package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    UploadC2DocumentsService.class,
    SupportingEvidenceValidatorService.class,
    ValidateGroupService.class,
    LocalValidatorFactoryBean.class,
    FixedTimeConfiguration.class
})
class UploadC2DocumentsServiceTest {
    private static final String ERROR_MESSAGE = "Date cannot be in the future";

    @Autowired
    private UploadC2DocumentsService service;

    @Test
    void shouldReturnErrorsWhenTheDateOfIssueIsInFuture() {
        assertThat(service.validate(createC2DocumentBundle())).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnEmptyListWhenNoSupportingDocuments() {
        assertThat(service.validate(createC2DocumentBundleWithNoSupportingDocuments())).isEmpty();
    }

    private C2DocumentBundle createC2DocumentBundle() {
        return C2DocumentBundle.builder()
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
            .dateTimeReceived(LocalDateTime.now().plusDays(1))
            .build();
    }
}
