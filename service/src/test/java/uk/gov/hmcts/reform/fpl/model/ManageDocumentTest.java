package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

class ManageDocumentTest {

    @Test
    void shouldReturnTrueIfManageDocumentIsRelatedToHearing() {
        ManageDocument manageDocument = buildManageDocument(YES);
        assertThat(manageDocument.isDocumentRelatedToHearing()).isTrue();
    }

    @Test
    void shouldReturnFalseIfManageDocumentIsNotRelatedToHearing() {
        ManageDocument manageDocument = buildManageDocument(NO);
        assertThat(manageDocument.isDocumentRelatedToHearing()).isFalse();
    }

    @Test
    void shouldReturnFalseIfManageDocumentIsEmpty() {
        ManageDocument manageDocument = ManageDocument.builder().build();
        assertThat(manageDocument.isDocumentRelatedToHearing()).isFalse();
    }

    private ManageDocument buildManageDocument(YesNo yesNo) {
        return ManageDocument.builder().relatedToHearing(yesNo.getValue()).build();
    }
}
