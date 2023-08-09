package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class DocumentsCheckerIsStartedTest {

    @InjectMocks
    private DocumentsChecker documentsChecker;

    @Test
    void shouldReturnFalseWhenNoDocumentAdded() {
        final CaseData caseData = CaseData.builder().build();

        assertThat(documentsChecker.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("addedDocuments")
    void shouldReturnTrueWhenAtLeastOneDocumentAdded(CaseData caseData) {
        assertThat(documentsChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> addedDocuments() {
        return Stream.of(
                CaseData.builder().otherSocialWorkDocuments(
                        wrapElements(DocumentSocialWorkOther.builder().build())).build(),
                CaseData.builder().socialWorkCarePlanDocument(document()).build(),
                CaseData.builder().socialWorkStatementDocument(document()).build(),
                CaseData.builder().socialWorkAssessmentDocument(document()).build(),
                CaseData.builder().socialWorkChronologyDocument(document()).build())
                .map(Arguments::of);
    }

    private static Document document() {
        return Document.builder().build();
    }
}
