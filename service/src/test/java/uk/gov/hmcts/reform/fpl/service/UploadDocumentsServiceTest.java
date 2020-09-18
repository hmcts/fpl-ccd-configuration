package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    UploadDocumentsService.class,
    FixedTimeConfiguration.class
})
class UploadDocumentsServiceTest {

    private CaseData givenCaseData;

    @Autowired
    private Time time;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UploadDocumentsService uploadDocumentsService;

    @MockBean
    private DocumentUploadHelper documentUploadHelper;

    @BeforeEach
    void setup() {
        givenCaseData = prepareCaseData();
    }

    @ParameterizedTest
    @ValueSource(strings = {"HMCTS", "someLA@la.co.uk"})
    void shouldUpdateOtherSocialWorkDocumentsListWithUpdatedDetailsAndUser(String user) {
        when(documentUploadHelper.getUploadedDocumentUserDetails()).thenReturn(user);

        List<DocumentSocialWorkOther> list = unwrapElements(
            uploadDocumentsService.setUpdatedByAndDateAndTimeForDocuments(
                createCaseDataWithUpdatedDocumentSocialWorkOther(),
                createCaseDataWithOldDocumentSocialWorkOther()));

        assertThat(list).first()
            .extracting(DocumentSocialWorkOther::getTypeOfDocument)
            .extracting(DocumentReference::getUrl)
            .isEqualTo("/new_test.doc");

        assertThat(list).first()
            .extracting(DocumentSocialWorkOther::getDocumentTitle)
            .isEqualTo("New Additional Doc 1");

        assertThat(list).first()
            .extracting(DocumentSocialWorkOther::getDateTimeUploaded)
            .isEqualTo(time.now());

        assertThat(list).first()
            .extracting(DocumentSocialWorkOther::getUploadedBy)
            .isEqualTo(user);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HMCTS", "someLA@la.co.uk"})
    void shouldUpdateDocumentsListWithUpdatedDetailsAndUser(String user) {
        when(documentUploadHelper.getUploadedDocumentUserDetails()).thenReturn(user);

        Document list =
            uploadDocumentsService.setUpdatedByAndDateAndTimeForDocuments(
                createCaseDataWithUpdatedDocument(),
                createCaseDataWithOldDocument());

        assertThat(list)
            .extracting(Document::getTypeOfDocument)
            .extracting(DocumentReference::getUrl)
            .isEqualTo("/new_test.doc");

        assertThat(list)
            .extracting(Document::getDateTimeUploaded)
            .isEqualTo(time.now());

        assertThat(list)
            .extracting(Document::getUploadedBy)
            .isEqualTo(user);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HMCTS", "someLA@la.co.uk"})
    void shouldUpdateOtherSocialWorkDocumentsListWithNewDocument(String user) {
        when(documentUploadHelper.getUploadedDocumentUserDetails()).thenReturn(user);

        List<DocumentSocialWorkOther> list = unwrapElements(
            uploadDocumentsService.setUpdatedByAndDateAndTimeForDocuments(
                createCaseDataWithCurrentDocumentSocialWorkOther(),
                createCaseDataWithOldDocumentSocialWorkOther()));

        assertThat(list)
            .extracting(DocumentSocialWorkOther::getDocumentTitle)
            .containsExactly("Additional Doc 1 - changed", "Additional Doc 2");

        assertThat(list)
            .extracting(DocumentSocialWorkOther::getTypeOfDocument)
            .extracting(DocumentReference::getUrl)
            .containsExactly("/test1 - changed.doc", "/test2.doc");

        assertThat(list)
            .extracting(DocumentSocialWorkOther::getUploadedBy)
            .containsExactly(user, user);
    }

    private List<Element<DocumentSocialWorkOther>> createCaseDataWithCurrentDocumentSocialWorkOther() {
        return givenCaseData.toBuilder()
            .otherSocialWorkDocuments(wrapElements(DocumentSocialWorkOther.builder()
                    .documentTitle("Additional Doc 1 - changed")
                    .typeOfDocument(DocumentReference.builder()
                        .url("/test1 - changed.doc")
                        .build())
                    .uploadedBy(null)
                    .build(),
                DocumentSocialWorkOther.builder()
                    .documentTitle("Additional Doc 2")
                    .typeOfDocument(DocumentReference.builder()
                        .url("/test2.doc")
                        .build())
                    .build()))
            .build()
            .getOtherSocialWorkDocuments();
    }

    private List<Element<DocumentSocialWorkOther>> createCaseDataWithOldDocumentSocialWorkOther() {
        return givenCaseData.toBuilder()
            .otherSocialWorkDocuments(wrapElements(DocumentSocialWorkOther.builder()
                .documentTitle("Additional Doc 1")
                .typeOfDocument(DocumentReference.builder()
                    .url("/test.doc")
                    .build())
                .uploadedBy("OldLAUser")
                .build()))
            .build()
            .getOtherSocialWorkDocuments();
    }

    private List<Element<DocumentSocialWorkOther>> createCaseDataWithUpdatedDocumentSocialWorkOther() {
        return givenCaseData.toBuilder()
            .otherSocialWorkDocuments(wrapElements(DocumentSocialWorkOther.builder()
                .documentTitle("New Additional Doc 1")
                .typeOfDocument(DocumentReference.builder()
                    .url("/new_test.doc")
                    .build())
                .build()))
            .build()
            .getOtherSocialWorkDocuments();
    }

    private Document createCaseDataWithOldDocument() {
        return givenCaseData.toBuilder()
            .socialWorkChronologyDocument(Document.builder()
                .typeOfDocument(DocumentReference.builder()
                    .url("/test.doc")
                    .build())
                .uploadedBy("OldLAUser")
                .build())
            .build()
            .getSocialWorkChronologyDocument();
    }

    private Document createCaseDataWithUpdatedDocument() {
        return givenCaseData.toBuilder()
            .socialWorkChronologyDocument(Document.builder()
                .typeOfDocument(DocumentReference.builder()
                    .url("/new_test.doc")
                    .build())
                .build())
            .build()
            .getSocialWorkChronologyDocument();
    }

    private CaseData prepareCaseData() {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(now());
        return caseData;
    }
}
