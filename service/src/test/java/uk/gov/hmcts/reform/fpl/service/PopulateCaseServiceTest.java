package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class, JacksonAutoConfiguration.class,
    PopulateCaseService.class})
class PopulateCaseServiceTest {

    private static final DocumentReference.DocumentReferenceBuilder MOCK_DOCUMENT_BUILDER = DocumentReference.builder()
        .url("http://fakeUrl")
        .binaryUrl("http://fakeBinaryUrl");

    @Autowired
    private Time time;

    @Autowired
    private PopulateCaseService service;

    @Test
    void shouldReturnTimeBasedAndDocumentData() {
        var expectedMockDocument = Map.of("documentStatus",
            "Attached",
            "typeOfDocument",
            MOCK_DOCUMENT_BUILDER.filename("mockFile.txt").build());
        var expectedSubmittedForm = MOCK_DOCUMENT_BUILDER
            .filename("mockSubmittedApplication.pdf")
            .build();

        Map<String, Object> data = service.getTimeBasedAndDocumentData();

        assertThat(data).extracting("dateAndTimeSubmitted").isEqualTo(time.now().toString());
        assertThat(data).extracting("dateSubmitted").isEqualTo(time.now().toLocalDate().toString());
        assertThat(data).extracting("submittedForm").isEqualTo(expectedSubmittedForm);
        assertThat(data).extracting("documents_checklist_document").isEqualTo(expectedMockDocument);
        assertThat(data).extracting("documents_threshold_document").isEqualTo(expectedMockDocument);
        assertThat(data).extracting("documents_socialWorkCarePlan_document").isEqualTo(expectedMockDocument);
        assertThat(data).extracting("documents_socialWorkAssessment_document").isEqualTo(expectedMockDocument);
        assertThat(data).extracting("documents_socialWorkEvidenceTemplate_document").isEqualTo(expectedMockDocument);
    }

    @Test
    void shouldReturnUpdatedSDODataWithoutMutatingOriginalMap() {
        Map<String, Object> originalMap = Map.of("standardDirectionOrder", Map.of("orderDoc", "initialValue"));

        var updatedSDOData = service.getUpdatedSDOData(originalMap);

        assertThat(updatedSDOData).extracting("orderDoc")
            .isEqualTo(MOCK_DOCUMENT_BUILDER.filename("mockSDO.pdf").build());
        assertThat(originalMap).extracting("standardDirectionOrder").extracting("orderDoc").isEqualTo("initialValue");
    }
}
