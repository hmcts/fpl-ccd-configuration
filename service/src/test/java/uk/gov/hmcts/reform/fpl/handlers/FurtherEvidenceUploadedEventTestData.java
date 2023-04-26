package uk.gov.hmcts.reform.fpl.handlers;

import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
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
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
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
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithRandomUUID;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

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
    public static final DocumentReference PDF_DOCUMENT_3 = getPDFDocument();
    public static final DocumentReference PDF_DOCUMENT_4 = getPDFDocument();
    public static final DocumentReference NON_PDF_DOCUMENT_1 = getNonPDFDocument();
    private static final FurtherEvidenceType DEFAULT_FURTHER_EVIDENCE_TYPE = GUARDIAN_REPORTS;

    private static final UUID DOC_ELEMENT_1_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID DOC_ELEMENT_2_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DOC_ELEMENT_3_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID DOC_ELEMENT_4_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private FurtherEvidenceUploadedEventTestData() {
    }

    public static CaseData buildSubmittedCaseData() {
        return commonCaseBuilder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .correspondenceDocuments(new ArrayList<>())
            .correspondenceDocumentsSolicitor(new ArrayList<>())
            .correspondenceDocumentsLA(new ArrayList<>())
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
        return buildCaseDataWithNonConfidentialLADocuments(DEFAULT_FURTHER_EVIDENCE_TYPE);
    }

    public static CaseData buildCaseDataWithNonConfidentialLADocuments(FurtherEvidenceType type) {
        return commonCaseBuilder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .furtherEvidenceDocumentsLA(buildNonConfidentialDocumentList(LA_USER, type))
            .build();
    }

    public static CaseData buildCaseDataWithConfidentialLADocuments() {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsLA(buildConfidentialDocumentList(LA_USER))
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
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .furtherEvidenceDocumentsSolicitor(
                removeEvidenceBundleType(buildNonConfidentialDocumentList(uploadedBy)))
            .build();
    }

    public static CaseData buildCaseDataWithNonConfidentialPDFDocumentsSolicitor(final String uploadedBy,
                                                                                 FurtherEvidenceType type) {
        return commonCaseBuilder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .furtherEvidenceDocumentsSolicitor(
                buildNonConfidentialDocumentList(uploadedBy, type))
            .build();
    }

    public static List<Element<SupportingEvidenceBundle>> removeEvidenceBundleType(
            List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle
    ) {
        return supportingEvidenceBundle.stream()
                .map(evidenceBundle -> element(evidenceBundle.getId(),
                    evidenceBundle.getValue().toBuilder().type(null).build()))
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
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondentStatements(buildRespondentStatementsList(
                removeEvidenceBundleType(buildNonConfidentialDocumentList(REP_USER))))
            .build();
    }

    public static CaseData buildCaseDataWithAdditionalApplicationBundle() {
        return buildCaseDataWithAdditionalApplicationBundle(LA_USER, false);
    }

    public static CaseData buildCaseDataWithAdditionalApplicationBundle(final String uploadedBy, boolean confidential) {
        OtherApplicationsBundle otherBundle = OtherApplicationsBundle.builder()
                .id(UUID.randomUUID())
                .applicationType(OtherApplicationType.C1_WITH_SUPPLEMENT)
                .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(now().plusDays(1), DATE_TIME))
                .supportingEvidenceBundle(confidential
                    ? buildConfidentialDocumentList(uploadedBy)
                    : buildNonConfidentialDocumentList(uploadedBy))
                .build();


        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
                .otherApplicationsBundle(otherBundle)
                .build();


        return commonCaseBuilder()
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .additionalApplicationsBundle(wrapElementsWithUUIDs(additionalApplicationsBundle))
                .build();
    }

    public static CaseData buildCaseDataWithC2AdditionalApplicationBundle() {
        return buildCaseDataWithC2AdditionalApplicationBundle(LA_USER, false);
    }

    public static CaseData buildCaseDataWithC2AdditionalApplicationBundle(final String uploadedBy, boolean confidential) {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
                .id(UUID.randomUUID())
                .type(WITH_NOTICE)
                .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(now().plusDays(1), DATE_TIME))
                .supportingEvidenceBundle(confidential
                    ? buildConfidentialDocumentList(uploadedBy)
                    : buildNonConfidentialDocumentList(uploadedBy))
                .build();


        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(c2DocumentBundle)
                .build();


        return commonCaseBuilder()
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .additionalApplicationsBundle(wrapElementsWithUUIDs(additionalApplicationsBundle))
                .build();
    }

    public static CaseData buildCaseDataWithNonConfidentialNonPDFRespondentStatementsSolicitor() {
        return commonCaseBuilder()
            .respondentStatements(buildRespondentStatementsList(
                removeEvidenceBundleType(buildNonConfidentialNonPDFDocumentList(REP_USER))))
            .build();
    }

    public static List<Element<SupportingEvidenceBundle>> buildConfidentialDocumentList(final String uploadedBy) {
        return List.of(
            element(DOC_ELEMENT_1_ID, createDummyEvidenceBundle(CONFIDENTIAL_1, uploadedBy, true, PDF_DOCUMENT_1)),
            element(DOC_ELEMENT_2_ID, createDummyEvidenceBundle(CONFIDENTIAL_2, uploadedBy, true, PDF_DOCUMENT_2))
        );
    }

    public static List<Element<SupportingEvidenceBundle>> buildConfidentialDocumentList(final String uploadedBy,
                                                                                        FurtherEvidenceType type) {
        return List.of(
            element(DOC_ELEMENT_1_ID,
                createDummyEvidenceBundle(CONFIDENTIAL_1, uploadedBy, true, PDF_DOCUMENT_1, type)),
            element(DOC_ELEMENT_2_ID,
                createDummyEvidenceBundle(CONFIDENTIAL_2, uploadedBy, true, PDF_DOCUMENT_2, type))
        );
    }

    public static List<Element<SupportingEvidenceBundle>> buildNonConfidentialDocumentList(final String uploadedBy) {
        return buildNonConfidentialDocumentList(uploadedBy, DEFAULT_FURTHER_EVIDENCE_TYPE);
    }

    public static List<Element<SupportingEvidenceBundle>> buildNonConfidentialDocumentList(final String uploadedBy,
                                                                                           FurtherEvidenceType type) {
        return List.of(
            element(DOC_ELEMENT_3_ID,
                createDummyEvidenceBundle(NON_CONFIDENTIAL_1, uploadedBy, false, PDF_DOCUMENT_1, type)),
            element(DOC_ELEMENT_4_ID,
                createDummyEvidenceBundle(NON_CONFIDENTIAL_2, uploadedBy, false, PDF_DOCUMENT_2, type))
        );
    }

    public static List<Element<SupportingEvidenceBundle>> buildNonConfidentialNonPDFDocumentList(
        final String uploadedBy) {
        return wrapElementsWithUUIDs(
            createDummyEvidenceBundle(NON_CONFIDENTIAL_1, uploadedBy, false, NON_PDF_DOCUMENT_1));
    }

    public static SupportingEvidenceBundle  createDummyEvidenceBundle(final String name, final String uploadedBy,
                                                                      boolean confidential, DocumentReference docRef) {
        return createDummyEvidenceBundle(name, uploadedBy, confidential, docRef, DEFAULT_FURTHER_EVIDENCE_TYPE);
    }

    public static SupportingEvidenceBundle  createDummyEvidenceBundle(final String name, final String uploadedBy,
                                                                      boolean confidential, DocumentReference docRef,
                                                                      FurtherEvidenceType type) {
        SupportingEvidenceBundle.SupportingEvidenceBundleBuilder document
            = SupportingEvidenceBundle.builder()
            .name(name)
            .uploadedBy(uploadedBy)
            .dateTimeUploaded(LocalDateTime.now())
            .document(docRef)
            .type(type);

        if (confidential) {
            document.confidential(List.of(CONFIDENTIAL_MARKER));
        }

        return document.build();
    }

    public static ApplicationDocument createDummyApplicationDocument(String name, String uploadedBy,
                                                                     DocumentReference docRef) {
        return createDummyApplicationDocument(name, uploadedBy, docRef, false);
    }

    public static ApplicationDocument createDummyApplicationDocument(final String name, final String uploadedBy,
                                                                     DocumentReference docRef, boolean confidential) {
        return createDummyApplicationDocument(name, uploadedBy, docRef, confidential, BIRTH_CERTIFICATE);
    }

    public static ApplicationDocument createDummyApplicationDocument(final String name, final String uploadedBy,
                                                                     DocumentReference docRef, boolean confidential,
                                                                     ApplicationDocumentType documentType) {
        return ApplicationDocument.builder()
            .documentName(name)
            .documentType(documentType)
            .uploadedBy(uploadedBy)
            .document(docRef)
            .dateTimeUploaded(LocalDateTime.now())
            .confidential(confidential ? List.of("CONFIDENTIAL") : List.of())
            .build();
    }

    public static CaseData buildCaseDataWithCourtBundleList(int count, String hearing, String uploadedBy) {
        return commonCaseBuilder()
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(
                    createCourtBundleList(count, hearing, uploadedBy)
                ).build())
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .build();
    }

    public static List<Element<HearingCourtBundle>> createCourtBundleList(int count, String hearing,
                                                                          String uploadedBy) {
        return IntStream.rangeClosed(1, count)
            .boxed()
            .map(value -> {
                Element<CourtBundle> courtBundleElement = element(createDummyCourtBundle(uploadedBy));
                return element(HearingCourtBundle.builder()
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
        return wrapElementsWithUUIDs(HearingFurtherEvidenceBundle.builder()
            .hearingName("hearingName-" + UUID.randomUUID().toString())
            .supportingEvidenceBundle(bundle)
            .build());
    }

    public static List<Element<RespondentStatement>> buildRespondentStatementsList(
        List<Element<SupportingEvidenceBundle>> bundle
    ) {
        return buildRespondentStatementsList(UUID.randomUUID(), bundle);
    }

    public static List<Element<RespondentStatement>> buildRespondentStatementsList(
        UUID respondentId, List<Element<SupportingEvidenceBundle>> bundle
    ) {
        return wrapElementsWithRandomUUID(RespondentStatement.builder()
            .respondentId(respondentId)
            .respondentName("name")
            .supportingEvidenceBundle(bundle)
            .build());
    }

    public static CaseData.CaseDataBuilder<?,?> commonCaseBuilder() {
        return CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(CASE_ID.toString())
            .hearingDetails(wrapElementsWithUUIDs(HearingBooking.builder().startDate((HEARING_DATE)).build()));
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
