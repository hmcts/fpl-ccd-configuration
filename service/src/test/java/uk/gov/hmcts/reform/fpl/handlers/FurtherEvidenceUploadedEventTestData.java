package uk.gov.hmcts.reform.fpl.handlers;

import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.BIRTH_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
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
    public static final String CONFIDENTIAL_2 = "confidential-doc-2";
    public static final DocumentReference PDF_DOCUMENT_1 = getPDFDocument();
    public static final DocumentReference PDF_DOCUMENT_2 = getPDFDocument();
    public static final DocumentReference NON_PDF_DOCUMENT_1 = getNonPDFDocument();

    private FurtherEvidenceUploadedEventTestData() {
    }

    public static CaseData buildSubmittedCaseData() {
        return commonCaseBuilder()
            .applicationDocuments(new ArrayList<>())
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(new ArrayList<>())
                .caseSummaryList(new ArrayList<>())
                .positionStatementChildList(new ArrayList<>())
                .positionStatementRespondentList(new ArrayList<>()).build())
            .furtherEvidenceDocuments(new ArrayList<>())
            .furtherEvidenceDocumentsLA(new ArrayList<>())
            .furtherEvidenceDocumentsSolicitor(new ArrayList<>())
            .hearingFurtherEvidenceDocuments(new ArrayList<>())
            .respondentStatements(new ArrayList<>())
            .build();
    }

    public static CaseData buildCaseDataWithNonConfidentialLADocuments() {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsLA(buildNonConfidentialPdfDocumentList(LA_USER))
            .build();
    }

    public static CaseData buildCaseDataWithConfidentialLADocuments() {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsLA(buildConfidentialDocumentList(LA_USER))
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
                removeEvidenceBundleType(buildNonConfidentialPdfDocumentList(uploadedBy)))
            .build();
    }

    private static List<Element<SupportingEvidenceBundle>> removeEvidenceBundleType(
            List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle
    ) {
        return unwrapElements(supportingEvidenceBundle).stream()
                .map(evidenceBundle -> element(evidenceBundle.toBuilder().type(null).build()))
                .collect(Collectors.toList());
    }

    public static CaseData buildCaseDataWithNonConfidentialNonPdfDocumentsSolicitor(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsSolicitor(
                removeEvidenceBundleType(buildNonConfidentialNonPDFDocumentList(uploadedBy)))
            .build();
    }

    public static CaseData buildCaseDataWithConfidentialDocumentsSolicitor(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsSolicitor(
                removeEvidenceBundleType(buildConfidentialDocumentList(uploadedBy)))
            .build();
    }

    public static CaseData buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor() {
        return commonCaseBuilder()
            .respondentStatements(buildRespondentStatementsList(
                removeEvidenceBundleType(buildNonConfidentialPdfDocumentList(REP_USER))))
            .build();
    }

    public static CaseData buildCaseDataWithCorrespondencesByHmtcs() {
        return commonCaseBuilder()
                .correspondenceDocuments((buildNonConfidentialPdfDocumentList(HMCTS_USER)))
                .build();
    }

    public static CaseData buildCaseDataWithCorrespondencesByLA() {
        return commonCaseBuilder()
                .correspondenceDocumentsLA((buildNonConfidentialPdfDocumentList(LA_USER)))
                .build();
    }

    public static CaseData buildCaseDataWithCorrespondencesBySolicitor() {
        return commonCaseBuilder()
                .correspondenceDocumentsSolicitor(
                    removeEvidenceBundleType((buildNonConfidentialPdfDocumentList(REP_SOLICITOR_USER_EMAIL))))
                .build();
    }

    public static CaseData buildCaseDataWithHearingFurtherEvidenceBundle() {
        HearingFurtherEvidenceBundle bundle = HearingFurtherEvidenceBundle.builder()
                .hearingName("Case management hearing, 1 April 2022")
                .supportingEvidenceBundle(buildNonConfidentialPdfDocumentList(LA_USER))
                .build();
        return commonCaseBuilder()
                .hearingFurtherEvidenceDocuments(wrapElements(bundle))
                .build();
    }

    public static CaseData buildCaseDataWithAdditionalApplicationBundle() {
        OtherApplicationsBundle otherBundle = OtherApplicationsBundle.builder()
                .id(UUID.randomUUID())
                .applicationType(OtherApplicationType.C1_WITH_SUPPLEMENT)
                .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(now().plusDays(1), DATE_TIME))
                .supportingEvidenceBundle(buildNonConfidentialPdfDocumentList(LA_USER))
                .build();


        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
                .otherApplicationsBundle(otherBundle)
                .build();


        return commonCaseBuilder()
                .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
                .build();
    }

    public static CaseData buildCaseDataWithNonConfidentialNonPDFRespondentStatementsSolicitor() {
        return commonCaseBuilder()
            .respondentStatements(buildRespondentStatementsList(
                removeEvidenceBundleType(buildNonConfidentialNonPDFDocumentList(REP_USER))))
            .build();
    }

    public static CaseData buildCaseDataWithConfidentialRespondentStatementsSolicitor() {
        return commonCaseBuilder()
            .respondentStatements(buildRespondentStatementsList(
                removeEvidenceBundleType(buildConfidentialDocumentList(REP_USER))))
            .build();
    }

    public static CaseData buildCaseDataWithApplicationDocuments() {
        return commonCaseBuilder()
                .applicationDocuments(
                    wrapElements(
                            createDummyApplicationDocument(NON_CONFIDENTIAL_1, LA_USER, PDF_DOCUMENT_1),
                            createDummyApplicationDocument(NON_CONFIDENTIAL_1, LA_USER, PDF_DOCUMENT_1)
                    )
                )
            .build();
    }

    public static List<Element<SupportingEvidenceBundle>> buildConfidentialDocumentList(final String uploadedBy) {
        return wrapElements(
            createDummyEvidenceBundle(CONFIDENTIAL_1, uploadedBy, true, PDF_DOCUMENT_1),
            createDummyEvidenceBundle(CONFIDENTIAL_2, uploadedBy, true, PDF_DOCUMENT_2));
    }

    public static List<Element<SupportingEvidenceBundle>> buildNonConfidentialPdfDocumentList(final String uploadedBy) {
        return wrapElements(
            createDummyEvidenceBundle(NON_CONFIDENTIAL_1, uploadedBy, false, PDF_DOCUMENT_1),
            createDummyEvidenceBundle(NON_CONFIDENTIAL_2, uploadedBy, false, PDF_DOCUMENT_2));
    }


    public static List<Element<SupportingEvidenceBundle>> buildNonConfidentialNonPDFDocumentList(
        final String uploadedBy) {
        return wrapElements(
            createDummyEvidenceBundle(NON_CONFIDENTIAL_1, uploadedBy, false, NON_PDF_DOCUMENT_1));
    }

    public static SupportingEvidenceBundle  createDummyEvidenceBundle(final String name, final String uploadedBy,
                                                                      boolean confidential, DocumentReference docRef) {
        SupportingEvidenceBundle.SupportingEvidenceBundleBuilder document
            = SupportingEvidenceBundle.builder()
            .name(name)
            .uploadedBy(uploadedBy)
            .dateTimeUploaded(LocalDateTime.now())
            .document(docRef)
            .type(GUARDIAN_REPORTS);

        if (confidential) {
            document.confidential(List.of(CONFIDENTIAL_MARKER));
        }

        return document.build();
    }

    public static ApplicationDocument createDummyApplicationDocument(final String name, final String uploadedBy,
                                                                     DocumentReference docRef) {
        return ApplicationDocument.builder()
                .documentName(name)
                .documentType(BIRTH_CERTIFICATE)
                .uploadedBy(uploadedBy)
                .document(docRef)
                .dateTimeUploaded(LocalDateTime.now())
                .build();
    }

    public static CaseData buildCaseDataWithCourtBundleList(int count, String hearing, String uploadedBy) {
        return commonCaseBuilder()
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(
                    createCourtBundleList(count, hearing, uploadedBy)
                ).build())
            .build();
    }

    public static List<Element<HearingCourtBundle>> createCourtBundleList(int count, String hearing,
                                                                          String uploadedBy) {
        return IntStream.rangeClosed(1, count)
            .boxed()
            .map(value -> {
                Element<CourtBundle> courtBundleElement = ElementUtils.element(createDummyCourtBundle(uploadedBy));
                return ElementUtils.element(HearingCourtBundle.builder()
                        .hearing(hearing)
                        .courtBundle(List.of(courtBundleElement))
                        .build()
                );

            })
            .collect(Collectors.toList());
    }

    public static CourtBundle createDummyCourtBundle(String uploadedBy) {
        return CourtBundle.builder()
            .document(getPDFDocument())
            .dateTimeUploaded(LocalDateTime.now())
            .uploadedBy(uploadedBy)
            .build();
    }

    public static List<Element<HearingFurtherEvidenceBundle>> buildHearingFurtherEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> bundle
    ) {
        return wrapElements(HearingFurtherEvidenceBundle.builder()
            .hearingName(UUID.randomUUID().toString())
            .supportingEvidenceBundle(bundle)
            .build());
    }

    public static List<Element<RespondentStatement>> buildRespondentStatementsList(
        List<Element<SupportingEvidenceBundle>> bundle
    ) {
        return wrapElements(RespondentStatement.builder()
            .respondentId(UUID.randomUUID())
            .respondentName("name")
            .supportingEvidenceBundle(bundle)
            .build());
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
