package uk.gov.hmcts.reform.fpl.handlers;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class FurtherEvidenceUploadedEventTestData {
    public static final Long CASE_ID = 12345L;
    public static final String CONFIDENTIAL_MARKER = "CONFIDENTIAL";
    public static final String LA_USER = "LA";
    public static final String HMCTS_USER = "HMCTS";
    public static final String REP_USER = "REP";
    public static final String LA_USER_EMAIL = "la@examaple.com";
    public static final String HMCTS_USER_EMAIL = "hmcts@examaple.com";
    public static final String REP_SOLICITOR_USER_EMAIL = "rep@examaple.com";
    public static final String SENDER_FORENAME = "The";
    public static final String SENDER_SURNAME = "Sender";
    public static final String SENDER = SENDER_FORENAME + " " + SENDER_SURNAME;
    public static final String REP_SOLICITOR_1_EMAIL = "rep_solicitor1@example.com";
    public static final String REP_SOLICITOR_2_EMAIL = "rep_solicitor2@example.com";
    public static final LocalDateTime HEARING_DATE = LocalDateTime.now().plusMonths(3);
    public static final String NON_CONFIDENTIAL_1 = "non-confidential-1";
    public static final String NON_CONFIDENTIAL_2 = "non-confidential-2";
    public static final String CONFIDENTIAL_1 = "confidential-doc-1";
    public static final DocumentReference PDF_DOCUMENT_1 = getPDFDocument();
    public static final DocumentReference PDF_DOCUMENT_2 = getPDFDocument();
    public static final DocumentReference NON_PDF_DOCUMENT_1 = getNonPDFDocument();

    public FurtherEvidenceUploadedEventTestData() {};

    public static CaseData buildCaseDataWithNonConfidentialLADocuments() {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsLA(
                buildNonConfidentialPdfDocumentList(LA_USER))
            .build();
    }

    public static CaseData buildCaseDataWithConfidentialLADocuments() {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsLA(
                buildConfidentialDocumentList(LA_USER))
            .build();
    }

    public static CaseData buildCaseDataWithNonConfidentialDocuments(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocuments(
                buildNonConfidentialPdfDocumentList(uploadedBy))
            .build();
    }

    public static CaseData buildCaseDataWithConfidentialDocuments(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocuments(
                buildConfidentialDocumentList(uploadedBy))
            .build();
    }

    public static CaseData buildCaseDataWithNonConfidentialPDFDocumentsSolicitor(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsSolicitor(
                buildNonConfidentialPdfDocumentList(uploadedBy))
            .build();
    }

    public static CaseData buildCaseDataWithNonConfidentialNonPdfDocumentsSolicitor(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsSolicitor(
                buildNonConfidentialNonPDFDocumentList(uploadedBy))
            .build();
    }

    public static CaseData buildCaseDataWithConfidentialDocumentsSolicitor(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsSolicitor(
                buildConfidentialDocumentList(uploadedBy))
            .build();
    }

    public static List<Element<SupportingEvidenceBundle>> buildConfidentialDocumentList(final String uploadedBy) {
        return wrapElements(
            createDummyEvidenceBundle("confidential-1", uploadedBy, true, PDF_DOCUMENT_1),
            createDummyEvidenceBundle("confidential-2", uploadedBy, true, PDF_DOCUMENT_2));
    }

    public static List<Element<SupportingEvidenceBundle>> buildNonConfidentialPdfDocumentList(final String uploadedBy) {
        return wrapElements(
            createDummyEvidenceBundle(NON_CONFIDENTIAL_1, uploadedBy, false, PDF_DOCUMENT_1),
            createDummyEvidenceBundle(NON_CONFIDENTIAL_2, uploadedBy, false, PDF_DOCUMENT_2));
    }

    public static List<Element<SupportingEvidenceBundle>> buildNonConfidentialNonPDFDocumentList(final String uploadedBy) {
        return wrapElements(
            createDummyEvidenceBundle(NON_CONFIDENTIAL_1, uploadedBy, false, NON_PDF_DOCUMENT_1));
    }

    public static SupportingEvidenceBundle createDummyEvidenceBundle(final String name, final String uploadedBy,
                                                                      boolean confidential, DocumentReference docRef) {
        SupportingEvidenceBundle.SupportingEvidenceBundleBuilder document
            = SupportingEvidenceBundle.builder()
            .name(name)
            .uploadedBy(uploadedBy)
            .dateTimeUploaded(LocalDateTime.now())
            .document(docRef);

        if (confidential) {
            document.confidential(List.of(CONFIDENTIAL_MARKER));
        }

        return document.build();
    }

    public static CaseData.CaseDataBuilder commonCaseBuilder() {
        return CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(CASE_ID.toString())
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()));
    }

    public static UserDetails userDetailsLA() {
        return UserDetails.builder().email(LA_USER_EMAIL).forename(SENDER_FORENAME).surname(SENDER_SURNAME).build();
    }

    public static UserDetails userDetailsHMCTS() {
        return UserDetails.builder().email(HMCTS_USER_EMAIL).forename(SENDER_FORENAME).surname(SENDER_SURNAME).build();
    }

    public static UserDetails userDetailsRespondentSolicitor() {
        return UserDetails.builder()
            .email(REP_SOLICITOR_USER_EMAIL)
            .forename(SENDER_FORENAME)
            .surname(SENDER_SURNAME)
            .build();
    }

    public static DocumentReference getPDFDocument() {
        return TestDataHelper.testDocumentReference(randomAlphanumeric(10).concat(".pdf"));
    }

    public static DocumentReference getNonPDFDocument() {
        return TestDataHelper.testDocumentReference(randomAlphanumeric(10));
    }
}
