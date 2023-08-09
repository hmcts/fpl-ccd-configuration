package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class UploadDocumentTransformerTest {

    private static final ApplicationDocumentType APPLICATION_DOCUMENT_TYPE = ApplicationDocumentType.THRESHOLD;
    private static final String UPLOADED_BY = "UploadedBy";
    private static final String ANOTHER_UPLOADED_BY = "UploadedBy";
    private static final DocumentReference TYPE_OF_DOCUMENT = mock(DocumentReference.class);
    private static final DocumentReference ANOTHER_TYPE_OF_DOCUMENT = mock(DocumentReference.class);
    private static final LocalDateTime DATE_TIME_UPLOADED = mock(LocalDateTime.class);
    private static final LocalDateTime ANOTHER_DATE_TIME_UPLOADED = mock(LocalDateTime.class);
    private static final String DOCUMENT_TITLE = "documentTitle";
    private static final String ANOTHER_DOCUMENT_TITLE = "anotherDocumentTitle";
    private static final UUID RANDOM_UUID = UUID.randomUUID();
    private static final UUID ELEMENT_ID = UUID.randomUUID();
    private static final UUID ANOTHER_ELEMENT_ID = UUID.randomUUID();

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private UploadDocumentTransformer underTest;


    @Test
    void testConvertDocument() {
        when(identityService.generateId()).thenReturn(RANDOM_UUID);

        Element<ApplicationDocument> actual = underTest.convert(Document.builder()
                .uploadedBy(UPLOADED_BY)
                .typeOfDocument(TYPE_OF_DOCUMENT)
                .dateTimeUploaded(DATE_TIME_UPLOADED)
                .build(),
            APPLICATION_DOCUMENT_TYPE);

        assertThat(actual).isEqualTo(element(RANDOM_UUID,
            ApplicationDocument.builder()
                .uploadedBy(UPLOADED_BY)
                .documentType(APPLICATION_DOCUMENT_TYPE)
                .document(TYPE_OF_DOCUMENT)
                .dateTimeUploaded(DATE_TIME_UPLOADED)
                .includedInSWET(null)
                .build())
        );
    }

    @Test
    void testConvertDocumentSocialWorkerOthersIfEmpty() {
        List<Element<ApplicationDocument>> actual = underTest.convert(Collections.emptyList());

        assertThat(actual).isEqualTo(Collections.emptyList());
    }

    @Test
    void testConvertDocumentSocialWorkerOthersOneElement() {
        List<Element<ApplicationDocument>> actual = underTest.convert(List.of(
            element(
                ELEMENT_ID,
                DocumentSocialWorkOther.builder()
                    .uploadedBy(UPLOADED_BY)
                    .typeOfDocument(TYPE_OF_DOCUMENT)
                    .dateTimeUploaded(DATE_TIME_UPLOADED)
                    .documentTitle(DOCUMENT_TITLE)
                    .build())
            )
        );

        assertThat(actual).isEqualTo(List.of(
            element(
                ELEMENT_ID,
                ApplicationDocument.builder()
                    .uploadedBy(UPLOADED_BY)
                    .document(TYPE_OF_DOCUMENT)
                    .dateTimeUploaded(DATE_TIME_UPLOADED)
                    .documentType(OTHER)
                    .documentName(DOCUMENT_TITLE)
                    .includedInSWET(null)
                    .build()
            )));
    }

    @Test
    void testConvertDocumentSocialWorkerOthersMultipleElement() {
        List<Element<ApplicationDocument>> actual = underTest.convert(List.of(
            element(
                ELEMENT_ID,
                DocumentSocialWorkOther.builder()
                    .uploadedBy(UPLOADED_BY)
                    .typeOfDocument(TYPE_OF_DOCUMENT)
                    .dateTimeUploaded(DATE_TIME_UPLOADED)
                    .documentTitle(DOCUMENT_TITLE)
                    .build()
            ),
            element(
                ANOTHER_ELEMENT_ID,
                DocumentSocialWorkOther.builder()
                    .uploadedBy(ANOTHER_UPLOADED_BY)
                    .typeOfDocument(ANOTHER_TYPE_OF_DOCUMENT)
                    .dateTimeUploaded(ANOTHER_DATE_TIME_UPLOADED)
                    .documentTitle(ANOTHER_DOCUMENT_TITLE)
                    .build())
        ));

        assertThat(actual).isEqualTo(List.of(
            element(
                ELEMENT_ID,
                ApplicationDocument.builder()
                    .uploadedBy(UPLOADED_BY)
                    .document(TYPE_OF_DOCUMENT)
                    .dateTimeUploaded(DATE_TIME_UPLOADED)
                    .documentType(OTHER)
                    .documentName(DOCUMENT_TITLE)
                    .includedInSWET(null)
                    .build()
            ),
            element(
                ANOTHER_ELEMENT_ID,
                ApplicationDocument.builder()
                    .uploadedBy(ANOTHER_UPLOADED_BY)
                    .document(ANOTHER_TYPE_OF_DOCUMENT)
                    .dateTimeUploaded(ANOTHER_DATE_TIME_UPLOADED)
                    .documentType(OTHER)
                    .documentName(ANOTHER_DOCUMENT_TITLE)
                    .includedInSWET(null)
                    .build()
            )

            ));
    }
}
