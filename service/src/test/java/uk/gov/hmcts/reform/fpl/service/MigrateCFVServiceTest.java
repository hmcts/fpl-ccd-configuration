package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocument;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.BIRTH_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CHECKLIST_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.GENOGRAM;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_CHRONOLOGY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.THRESHOLD;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.OTHER_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType.PROFESSIONAL_DRUG;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType.PROFESSIONAL_HAIR;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType.TOXICOLOGY_REPORT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class MigrateCFVServiceTest {

    private static final String MIGRATION_ID = "test-migration";

    @InjectMocks
    private MigrateCFVService underTest;

    @Nested
    class CaseFileViewMigrations {

        private final Map<FurtherEvidenceType, String> furtherEvidenceTypeToFieldNameMap = Map.of(
            APPLICANT_STATEMENT, "applicantWitnessStmtList",
            GUARDIAN_REPORTS, "guardianEvidenceList",
            OTHER_REPORTS, "expertReportList",
            NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE, "noticeOfActingOrIssueList"
        );

        private final Map<FurtherEvidenceType, String> furtherEvidenceTypeToMigrateMethodMap = Map.of(
            APPLICANT_STATEMENT, "migrateApplicantWitnessStatements",
            GUARDIAN_REPORTS, "migrateGuardianReports",
            OTHER_REPORTS, "migrateExpertReports",
            NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE, "migrateNoticeOfActingOrIssue"
        );

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = FurtherEvidenceType.class, names = {
            "APPLICANT_STATEMENT",
            "GUARDIAN_REPORTS",
            "OTHER_REPORTS",
            "NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE"})
        void shouldMigrateAnyOtherDocumentsUploadedByCTSC(FurtherEvidenceType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocuments(List.of(element(doc1Id, sebOne)))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> furtherEvidenceTypeToMigrateMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = FurtherEvidenceType.class, names = {
            "APPLICANT_STATEMENT",
            "GUARDIAN_REPORTS",
            "OTHER_REPORTS",
            "NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE"})
        void shouldMigrateLinkedHearingAnyOtherDocumentsUploadedByCTSC(FurtherEvidenceType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(type)
                .document(document1)
                .uploadedBy("HMCTS")
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build())))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> furtherEvidenceTypeToMigrateMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = FurtherEvidenceType.class, names = {
            "APPLICANT_STATEMENT",
            "GUARDIAN_REPORTS",
            "OTHER_REPORTS",
            "NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE"})
        void shouldMigrateConfidentialAnyOtherDocumentsUploadedByCTSC(FurtherEvidenceType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(type)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocuments(List.of(element(doc1Id, sebOne)))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> furtherEvidenceTypeToMigrateMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type)).asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "CTSC").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = FurtherEvidenceType.class, names = {
            "APPLICANT_STATEMENT",
            "GUARDIAN_REPORTS",
            "OTHER_REPORTS",
            "NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE"})
        void shouldMigrateConfidentialHearingAnyOtherDocumentsUploadedByCTSC(FurtherEvidenceType type)
            throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(type)
                .document(document1)
                .uploadedBy("HMCTS")
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build())))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> furtherEvidenceTypeToMigrateMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type)).asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "CTSC").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = FurtherEvidenceType.class, names = {
            "APPLICANT_STATEMENT",
            "GUARDIAN_REPORTS",
            "OTHER_REPORTS",
            "NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE"})
        void shouldMigrateAnyOtherDocumentUploadedByLA(FurtherEvidenceType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsLA(List.of(element(doc1Id, sebOne)))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> furtherEvidenceTypeToMigrateMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = FurtherEvidenceType.class, names = {
            "APPLICANT_STATEMENT",
            "GUARDIAN_REPORTS",
            "OTHER_REPORTS",
            "NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE"})
        void shouldMigrateHearingAnyOtherDocumentUploadedByLA(FurtherEvidenceType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build())))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> furtherEvidenceTypeToMigrateMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = FurtherEvidenceType.class, names = {
            "APPLICANT_STATEMENT",
            "GUARDIAN_REPORTS",
            "OTHER_REPORTS",
            "NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE"})
        void shouldMigrateConfidentialAnyOtherDocumentsUploadedByLA(FurtherEvidenceType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(type)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsLA(List.of(element(doc1Id, sebOne)))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> furtherEvidenceTypeToMigrateMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type)).asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "LA").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = FurtherEvidenceType.class, names = {
            "APPLICANT_STATEMENT",
            "GUARDIAN_REPORTS",
            "OTHER_REPORTS",
            "NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE"})
        void shouldMigrateConfidentialHearingAnyOtherDocumentsUploadedByLA(FurtherEvidenceType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(type)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build())))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> furtherEvidenceTypeToMigrateMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type)).asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "LA").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = FurtherEvidenceType.class, names = {
            "APPLICANT_STATEMENT",
            "GUARDIAN_REPORTS",
            "OTHER_REPORTS",
            "NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE"})
        void shouldMigrateAnyOtherDocumentsUploadedBySolicitor(FurtherEvidenceType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsSolicitor(List.of(element(doc1Id, sebOne)))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> furtherEvidenceTypeToMigrateMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = FurtherEvidenceType.class, names = {
            "APPLICANT_STATEMENT",
            "GUARDIAN_REPORTS",
            "OTHER_REPORTS",
            "NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE"})
        void shouldMigrateHearingAnyOtherDocumentsUploadedBySolicitor(FurtherEvidenceType type)
            throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build())))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> furtherEvidenceTypeToMigrateMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(furtherEvidenceTypeToFieldNameMap.get(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        private final Map<ExpertReportType, String> expertReportTypeToFieldNameMap = Map.of(
            PROFESSIONAL_DRUG, "drugAndAlcoholReportList",
            PROFESSIONAL_HAIR,"drugAndAlcoholReportList",
            TOXICOLOGY_REPORT, "drugAndAlcoholReportList"
        );

        private String getExpertReportFieldName(ExpertReportType type) {
            if (expertReportTypeToFieldNameMap.containsKey(type)) {
                return expertReportTypeToFieldNameMap.get(type);
            } else {
                return "expertReportList";
            }
        }

        @ParameterizedTest
        @EnumSource(value = ExpertReportType.class, names = {
            "PROFESSIONAL_DRUG", "PROFESSIONAL_HAIR", "TOXICOLOGY_REPORT", "NEUROSURGEON"})
        void shouldMigrateExpertReportUploadedByCTSC(ExpertReportType type) {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocuments(List.of(element(doc1Id, sebOne)))
                .build();

            Map<String, Object> updatedFields = underTest.migrateExpertReports(caseData);

            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @ParameterizedTest
        @EnumSource(value = ExpertReportType.class, names = {
            "PROFESSIONAL_DRUG", "PROFESSIONAL_HAIR", "TOXICOLOGY_REPORT", "NEUROSURGEON"})
        void shouldMigrateHearingExpertReportUploadedByCTSC(ExpertReportType type) {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(type)
                .document(document1)
                .uploadedBy("HMCTS")
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build())))
                .build();

            Map<String, Object> updatedFields = underTest.migrateExpertReports(caseData);

            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @ParameterizedTest
        @EnumSource(value = ExpertReportType.class, names = {
            "PROFESSIONAL_DRUG", "PROFESSIONAL_HAIR", "TOXICOLOGY_REPORT", "NEUROSURGEON"})
        void shouldMigrateConfidentialExpertReportUploadedByCTSC(ExpertReportType type) {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(type)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocuments(List.of(element(doc1Id, sebOne)))
                .build();

            Map<String, Object> updatedFields = underTest.migrateExpertReports(caseData);

            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type)).asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "CTSC").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @ParameterizedTest
        @EnumSource(value = ExpertReportType.class, names = {
            "PROFESSIONAL_DRUG", "PROFESSIONAL_HAIR", "TOXICOLOGY_REPORT", "NEUROSURGEON"})
        void shouldMigrateHearingConfidentialExpertReportUploadedByCTSC(ExpertReportType type) {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(type)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .uploadedBy("HMCTS")
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build())))
                .build();

            Map<String, Object> updatedFields = underTest.migrateExpertReports(caseData);

            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type)).asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "CTSC").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @ParameterizedTest
        @EnumSource(value = ExpertReportType.class, names = {
            "PROFESSIONAL_DRUG", "PROFESSIONAL_HAIR", "TOXICOLOGY_REPORT", "NEUROSURGEON"})
        void shouldMigrateExpertReportsUploadedByLA(ExpertReportType type) {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsLA(List.of(element(doc1Id, sebOne)))
                .build();

            Map<String, Object> updatedFields = underTest.migrateExpertReports(caseData);

            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @ParameterizedTest
        @EnumSource(value = ExpertReportType.class, names = {
            "PROFESSIONAL_DRUG", "PROFESSIONAL_HAIR", "TOXICOLOGY_REPORT", "NEUROSURGEON"})
        void shouldMigrateHearingExpertReportsUploadedByLA(ExpertReportType type) {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build())))
                .build();

            Map<String, Object> updatedFields = underTest.migrateExpertReports(caseData);

            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @ParameterizedTest
        @EnumSource(value = ExpertReportType.class, names = {
            "PROFESSIONAL_DRUG", "PROFESSIONAL_HAIR", "TOXICOLOGY_REPORT", "NEUROSURGEON"})
        void shouldMigrateConfidentialExpertReportsUploadedByLA(ExpertReportType type) {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(type)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsLA(List.of(element(doc1Id, sebOne)))
                .build();

            Map<String, Object> updatedFields = underTest.migrateExpertReports(caseData);

            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type)).asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "LA").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @ParameterizedTest
        @EnumSource(value = ExpertReportType.class, names = {
            "PROFESSIONAL_DRUG", "PROFESSIONAL_HAIR", "TOXICOLOGY_REPORT", "NEUROSURGEON"})
        void shouldMigrateHearingConfidentialExpertReportsUploadedByLA(ExpertReportType type) {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(type)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build())))
                .build();

            Map<String, Object> updatedFields = underTest.migrateExpertReports(caseData);

            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type)).asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "LA").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @ParameterizedTest
        @EnumSource(value = ExpertReportType.class, names = {
            "PROFESSIONAL_DRUG", "PROFESSIONAL_HAIR", "TOXICOLOGY_REPORT", "NEUROSURGEON"})
        void shouldMigrateExpertReportUploadedBySolicitor(ExpertReportType type) {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsSolicitor(List.of(element(doc1Id, sebOne)))
                .build();

            Map<String, Object> updatedFields = underTest.migrateExpertReports(caseData);

            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @ParameterizedTest
        @EnumSource(value = ExpertReportType.class, names = {
            "PROFESSIONAL_DRUG", "PROFESSIONAL_HAIR", "TOXICOLOGY_REPORT", "NEUROSURGEON"})
        void shouldMigrateHearingExpertReportUploadedBySolicitor(ExpertReportType type) {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build())))
                .build();

            Map<String, Object> updatedFields = underTest.migrateExpertReports(caseData);

            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "CTSC").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(getExpertReportFieldName(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @Test
        void shouldMigrateMixedAnyOtherDocuments() {
            UUID doc1Id = UUID.randomUUID();
            UUID doc2Id = UUID.randomUUID();
            UUID doc3Id = UUID.randomUUID();
            UUID doc4Id = UUID.randomUUID();
            UUID doc5Id = UUID.randomUUID();
            UUID doc6Id = UUID.randomUUID();
            UUID doc7Id = UUID.randomUUID();
            UUID doc8Id = UUID.randomUUID();
            UUID doc9Id = UUID.randomUUID();
            UUID doc10Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            DocumentReference document2 = DocumentReference.builder().build();
            DocumentReference document3 = DocumentReference.builder().build();
            DocumentReference document4 = DocumentReference.builder().build();
            DocumentReference document5 = DocumentReference.builder().build();
            DocumentReference document6 = DocumentReference.builder().build();
            DocumentReference document7 = DocumentReference.builder().build();
            DocumentReference document8 = DocumentReference.builder().build();
            DocumentReference document9 = DocumentReference.builder().build();
            DocumentReference document10 = DocumentReference.builder().build();

            SupportingEvidenceBundle seb1 = SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();
            SupportingEvidenceBundle seb2 = SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .document(document2)
                .build();
            SupportingEvidenceBundle seb3 = SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .document(document3)
                .build();
            SupportingEvidenceBundle seb4 = SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .document(document4)
                .confidential(List.of("CONFIDENTIAL"))
                .build();
            SupportingEvidenceBundle seb5 = SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .document(document5)
                .build();
            SupportingEvidenceBundle seb6 = SupportingEvidenceBundle.builder()
                .type(GUARDIAN_REPORTS)
                .document(document6)
                .build();
            SupportingEvidenceBundle seb7 = SupportingEvidenceBundle.builder()
                .type(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)
                .document(document7)
                .confidential(List.of("CONFIDENTIAL"))
                .build();
            SupportingEvidenceBundle seb8 = SupportingEvidenceBundle.builder()
                .type(OTHER_REPORTS)
                .document(document8)
                .confidential(List.of("CONFIDENTIAL"))
                .build();
            SupportingEvidenceBundle seb9 = SupportingEvidenceBundle.builder()
                .type(EXPERT_REPORTS)
                .expertReportType(TOXICOLOGY_REPORT)
                .document(document9)
                .build();
            SupportingEvidenceBundle seb10 = SupportingEvidenceBundle.builder()
                .type(null)
                .document(document10)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsLA(List.of(element(doc1Id, seb1),
                    element(doc2Id, seb2),
                    element(doc7Id, seb7),
                    element(doc9Id, seb9),
                    element(doc10Id, seb10)))
                .furtherEvidenceDocuments(List.of(element(doc3Id, seb3),
                    element(doc4Id, seb4),
                    element(doc6Id, seb6),
                    element(doc8Id, seb8)))
                .furtherEvidenceDocumentsSolicitor(List.of(element(doc5Id, seb5)))
                .build();

            Map<String, Object> updatedFields = underTest.migrateApplicantWitnessStatements(caseData);
            updatedFields.putAll(underTest.migrateGuardianReports(caseData));
            updatedFields.putAll(underTest.migrateExpertReports(caseData));
            updatedFields.putAll(underTest.migrateNoticeOfActingOrIssue(caseData));
            updatedFields.putAll(underTest.migrateArchivedDocuments(caseData));

            assertThat(updatedFields).extracting("applicantWitnessStmtList").asList()
                .contains(element(doc2Id, ManagedDocument.builder().document(document2).build()),
                    element(doc3Id, ManagedDocument.builder().document(document3).build()),
                    element(doc5Id, ManagedDocument.builder().document(document5).build()));
            assertThat(updatedFields).extracting("applicantWitnessStmtListLA").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
            assertThat(updatedFields).extracting("applicantWitnessStmtListCTSC").asList()
                .contains(element(doc4Id, ManagedDocument.builder().document(document4).build()));

            assertThat(updatedFields).extracting("guardianEvidenceList").asList()
                .contains(element(doc6Id, ManagedDocument.builder().document(document6).build()));
            assertThat(updatedFields).extracting("guardianEvidenceListLA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting("guardianEvidenceListCTSC").asList()
                .isEmpty();

            assertThat(updatedFields).extracting("noticeOfActingOrIssueList").asList()
                .isEmpty();
            assertThat(updatedFields).extracting("noticeOfActingOrIssueListLA").asList()
                .contains(element(doc7Id, ManagedDocument.builder().document(document7).build()));
            assertThat(updatedFields).extracting("noticeOfActingOrIssueListCTSC").asList()
                .isEmpty();

            assertThat(updatedFields).extracting("expertReportList").asList()
                .isEmpty();
            assertThat(updatedFields).extracting("expertReportListLA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting("expertReportListCTSC").asList()
                .contains(element(doc8Id, ManagedDocument.builder().document(document8).build()));

            assertThat(updatedFields).extracting("drugAndAlcoholReportList").asList()
                .contains(element(doc9Id, ManagedDocument.builder().document(document9).build()));
            assertThat(updatedFields).extracting("drugAndAlcoholReportListLA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting("drugAndAlcoholReportListCTSC").asList()
                .isEmpty();

            assertThat(updatedFields).extracting("archivedDocumentsList").asList()
                .contains(element(doc10Id, ManagedDocument.builder().document(document10).build()));
            assertThat(updatedFields).extracting("archivedDocumentsListLA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting("archivedDocumentsListCTSC").asList()
                .isEmpty();
        }

        @Test
        void shouldRollbackMigrateAnyOtherDocuments() {
            assertThat(underTest.rollbackApplicantWitnessStatements())
                .extracting("applicantWitnessStmtList").isEqualTo(List.of());
            assertThat(underTest.rollbackApplicantWitnessStatements())
                .extracting("applicantWitnessStmtListLA").isEqualTo(List.of());
            assertThat(underTest.rollbackApplicantWitnessStatements())
                .extracting("applicantWitnessStmtListCTSC").isEqualTo(List.of());

            assertThat(underTest.rollbackGuardianReports()).extracting("guardianEvidenceList")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackGuardianReports()).extracting("guardianEvidenceListLA")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackGuardianReports()).extracting("guardianEvidenceListCTSC")
                .isEqualTo(List.of());

            assertThat(underTest.rollbackExpertReports()).extracting("drugAndAlcoholReportList")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackExpertReports()).extracting("drugAndAlcoholReportListLA")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackExpertReports()).extracting("drugAndAlcoholReportListCTSC")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackExpertReports()).extracting("lettersOfInstructionList")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackExpertReports()).extracting("lettersOfInstructionListLA")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackExpertReports()).extracting("lettersOfInstructionListCTSC")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackExpertReports()).extracting("expertReportList")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackExpertReports()).extracting("expertReportListLA")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackExpertReports()).extracting("expertReportListCTSC")
                .isEqualTo(List.of());

            assertThat(underTest.rollbackNoticeOfActingOrIssue()).extracting("noticeOfActingOrIssueList")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackNoticeOfActingOrIssue()).extracting("noticeOfActingOrIssueListLA")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackNoticeOfActingOrIssue()).extracting("noticeOfActingOrIssueListCTSC")
                .isEqualTo(List.of());

            assertThat(underTest.rollbackArchivedDocumentsList()).extracting("archivedDocumentsList")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackArchivedDocumentsList()).extracting("archivedDocumentsListLA")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackArchivedDocumentsList()).extracting("archivedDocumentsListCTSC")
                .isEqualTo(List.of());
        }

        @Test
        void shouldMigratePositionStatementChild() {
            Element<PositionStatementChild> positionStatementOne = element(UUID.randomUUID(),
                PositionStatementChild.builder().build());
            Element<PositionStatementChild> positionStatementTwo = element(UUID.randomUUID(),
                PositionStatementChild.builder().build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementChildListV2(List.of(positionStatementOne, positionStatementTwo)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migratePositionStatementChild(caseData);
            assertThat(updatedFields).extracting("posStmtChildListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("posStmtChildList").asList()
                .contains(positionStatementTwo, positionStatementOne);
        }

        @Test
        void shouldMigratePositionStatementChildWithConfidentialAddress() {
            Element<PositionStatementChild> positionStatementWithConfidentialAddress = element(UUID.randomUUID(),
                PositionStatementChild.builder().hasConfidentialAddress(YesNo.YES.getValue()).build());
            Element<PositionStatementChild> positionStatementChildElement = element(UUID.randomUUID(),
                PositionStatementChild.builder().hasConfidentialAddress(YesNo.NO.getValue()).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementChildListV2(List.of(positionStatementWithConfidentialAddress,
                        positionStatementChildElement)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migratePositionStatementChild(caseData);
            assertThat(updatedFields).extracting("posStmtChildListLA").asList()
                .contains(positionStatementWithConfidentialAddress);
            assertThat(updatedFields).extracting("posStmtChildList").asList()
                .contains(positionStatementChildElement);
        }

        @Test
        void shouldMigratePositionStatementRespondent() {
            Element<PositionStatementRespondent> positionStatementOne = element(UUID.randomUUID(),
                PositionStatementRespondent.builder().build());
            Element<PositionStatementRespondent> positionStatementTwo = element(UUID.randomUUID(),
                PositionStatementRespondent.builder().build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementRespondentListV2(List.of(positionStatementOne, positionStatementTwo)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migratePositionStatementRespondent(caseData);
            assertThat(updatedFields).extracting("posStmtRespListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("posStmtRespList").asList()
                .contains(positionStatementTwo, positionStatementOne);
        }

        @Test
        void shouldMigratePositionStatementRespondentWithConfidentialAddress() {
            Element<PositionStatementRespondent> positionStatementWithConfidentialAddress = element(UUID.randomUUID(),
                PositionStatementRespondent.builder().hasConfidentialAddress(YesNo.YES.getValue()).build());
            Element<PositionStatementRespondent> positionStatementRespoondentElement = element(UUID.randomUUID(),
                PositionStatementRespondent.builder().hasConfidentialAddress(YesNo.NO.getValue()).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementRespondentListV2(List.of(positionStatementWithConfidentialAddress,
                        positionStatementRespoondentElement)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migratePositionStatementRespondent(caseData);
            assertThat(updatedFields).extracting("posStmtRespListLA").asList()
                .contains(positionStatementWithConfidentialAddress);
            assertThat(updatedFields).extracting("posStmtRespList").asList()
                .contains(positionStatementRespoondentElement);
        }

        @Test
        void shouldRollbackMigratedRespondentStatement() {
            Map<String, Object> updatedFields = underTest.rollbackRespondentStatement();
            assertThat(updatedFields).extracting("respStmtList", "respStmtListLA", "respStmtListCTSC")
                .contains(List.of(), List.of(), List.of());
        }

        @Test
        void shouldMigrateNonConfidentialRespondentStatement() {
            UUID respondentOneId = UUID.randomUUID();
            UUID respondentTwoId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .build()));
            assertThat(updatedFields).extracting("respStmtListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListCTSC").asList().isEmpty();
        }

        @Test
        void shouldMigrateNonConfidentialMultipleRespondentStatements() {
            UUID respondentOneId = UUID.randomUUID();
            UUID respondentTwoId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();
            UUID doc2Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .build();

            DocumentReference document2 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebTwo = SupportingEvidenceBundle.builder()
                .document(document2)
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());
            Element<RespondentStatement> respondentStatementTwo = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentTwoId).respondentName("NAME 2")
                    .supportingEvidenceBundle(List.of(element(doc2Id, sebTwo)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne, respondentStatementTwo))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .build()),
                    element(doc2Id, RespondentStatementV2.builder()
                        .respondentId(respondentTwoId)
                        .respondentName("NAME 2")
                        .document(document2)
                        .build()));
            assertThat(updatedFields).extracting("respStmtListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListCTSC").asList().isEmpty();
        }

        @Test
        void shouldMigrateConfidentialRespondentStatementByLA() {
            UUID respondentOneId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListLA").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .confidential(List.of("CONFIDENTIAL"))
                        .build()));
            assertThat(updatedFields).extracting("respStmtListCTSC").asList().isEmpty();
        }

        @Test
        void shouldMigrateRespondentStatementContainsConfidentialAddressByLA() {
            UUID respondentOneId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .hasConfidentialAddress("Yes")
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListLA").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .hasConfidentialAddress("Yes")
                        .build()));
            assertThat(updatedFields).extracting("respStmtListCTSC").asList().isEmpty();
        }

        @Test
        void shouldMigrateConfidentialRespondentStatementByCTSC() {
            UUID respondentOneId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .uploadedBy("HMCTS")
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListCTSC").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .confidential(List.of("CONFIDENTIAL"))
                        .uploadedBy("HMCTS")
                        .build()));
        }

        @Test
        void shouldMigrateRespondentStatementContainsConfidentialAddressByCTSC() {
            UUID respondentOneId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .hasConfidentialAddress("Yes")
                .uploadedBy("HMCTS")
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListCTSC").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .hasConfidentialAddress("Yes")
                        .uploadedBy("HMCTS")
                        .build()));
        }

        void shouldMoveSingleCaseSummaryWithConfidentialAddressToCaseSummaryListLA() {
            Element<CaseSummary> caseSummaryListElement = element(UUID.randomUUID(), CaseSummary.builder()
                .hasConfidentialAddress(YesNo.YES.getValue())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .caseSummaryList(List.of(caseSummaryListElement))
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(
                caseData);
            assertThat(updatedFields).extracting("caseSummaryListBackup").asList()
                .containsExactly(caseSummaryListElement);
            assertThat(updatedFields).extracting("caseSummaryList").asList().isEmpty();
            assertThat(updatedFields).extracting("caseSummaryListLA").asList()
                .containsExactly(caseSummaryListElement);
        }

        @Test
        void shouldMoveOneOfCaseSummariesWithConfidentialAddressToCaseSummaryListLA() {
            Element<CaseSummary> caseSummaryListElementWithConfidentialAddress = element(UUID.randomUUID(),
                CaseSummary.builder().hasConfidentialAddress(YesNo.YES.getValue()).build());
            Element<CaseSummary> caseSummaryListElement = element(UUID.randomUUID(), CaseSummary.builder()
                .hasConfidentialAddress(YesNo.NO.getValue())
                .build());
            Element<CaseSummary> caseSummaryListElementTwo = element(UUID.randomUUID(), CaseSummary.builder()
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .caseSummaryList(List.of(caseSummaryListElement, caseSummaryListElementWithConfidentialAddress,
                        caseSummaryListElementTwo))
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(
                caseData);
            assertThat(updatedFields).extracting("caseSummaryListBackup").asList()
                .containsExactly(caseSummaryListElement, caseSummaryListElementWithConfidentialAddress,
                    caseSummaryListElementTwo);
            assertThat(updatedFields).extracting("caseSummaryList").asList()
                .containsExactly(caseSummaryListElement, caseSummaryListElementTwo);
            assertThat(updatedFields).extracting("caseSummaryListLA").asList()
                .containsExactly(caseSummaryListElementWithConfidentialAddress);
        }

        @Test
        void shouldRollbackMigratedCaseSummaryList() {
            Element<CaseSummary> caseSummaryListElementWithConfidentialAddress = element(UUID.randomUUID(),
                CaseSummary.builder().hasConfidentialAddress(YesNo.YES.getValue()).build());
            Element<CaseSummary> caseSummaryListElement = element(UUID.randomUUID(), CaseSummary.builder()
                .hasConfidentialAddress(YesNo.NO.getValue())
                .build());
            Element<CaseSummary> caseSummaryListElementTwo = element(UUID.randomUUID(), CaseSummary.builder()
                .build());

            Map<String, Object> caseDataMap = new HashMap<String, Object>();
            caseDataMap.put("caseSummaryListBackup",List.of(caseSummaryListElementWithConfidentialAddress,
                caseSummaryListElement, caseSummaryListElementTwo));
            caseDataMap.put("caseSummaryListLA", List.of(caseSummaryListElementWithConfidentialAddress));
            caseDataMap.put("caseSummaryList", List.of(caseSummaryListElement, caseSummaryListElementTwo));

            CaseDetails caseDetails = CaseDetails.builder().data(caseDataMap).build();

            assertThat(underTest.rollbackCaseSummaryMigration(caseDetails))
                .extracting("caseSummaryListLA")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackCaseSummaryMigration(caseDetails))
                .extracting("caseSummaryList").asList()
                .containsExactlyInAnyOrder(caseSummaryListElementWithConfidentialAddress, caseSummaryListElement,
                    caseSummaryListElementTwo);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldRollbackMigratedPositionStatementList(boolean isChild) {
            Element<? extends HearingDocument> positionStatementElement = element(
                isChild ? PositionStatementChild.builder().build() :
                    PositionStatementRespondent.builder().build());
            Element<? extends HearingDocument> positionStatementElementLA = element(
                isChild ? PositionStatementChild.builder().build() :
                    PositionStatementRespondent.builder().build());

            Map<String, Object> caseDataMap = new HashMap<String, Object>();
            caseDataMap.put(format("posStmt%sList", isChild ? "Child" : "Resp"), List.of(positionStatementElement));
            caseDataMap.put(format("posStmt%sListLA", isChild ? "Child" : "Resp"), List.of(positionStatementElementLA));

            CaseDetails caseDetails = CaseDetails.builder().data(caseDataMap).build();

            Map<String, Object> changes = null;
            if (isChild) {
                changes = underTest.rollbackPositionStatementChild(caseDetails);
            } else {
                changes = underTest.rollbackPositionStatementRespondent(caseDetails);
            }

            assertThat(changes).extracting(format("posStmt%sList", isChild ? "Child" : "Resp"))
                .isEqualTo(List.of());
            assertThat(changes).extracting(format("posStmt%sListLA", isChild ? "Child" : "Resp"))
                .isEqualTo(List.of());
        }

        private Element<CourtBundle> buildCourtBundle() {
            return element(
                CourtBundle.builder()
                    .document(testDocumentReference())
                    .confidential(List.of(""))
                    .hasConfidentialAddress(YesNo.NO.getValue())
                    .uploadedBy("LA")
                    .build());
        }

        private Element<CourtBundle> buildCTSCCourtBundle() {
            return element(
                CourtBundle.builder()
                    .document(testDocumentReference())
                    .confidential(List.of("CONFIDENTIAL"))
                    .hasConfidentialAddress(YesNo.YES.getValue())
                    .uploadedBy("HMCTS")
                    .build());
        }

        private Element<CourtBundle> buildLACourtBundle() {
            return element(
                CourtBundle.builder()
                    .document(testDocumentReference())
                    .confidential(List.of("CONFIDENTIAL"))
                    .hasConfidentialAddress(YesNo.YES.getValue())
                    .uploadedBy("LA")
                    .build());
        }

        @Test
        void nonConfidentialCourtBundlesShouldRemainInCourtBundleList() {
            UUID hearingId = UUID.randomUUID();

            Element<HearingCourtBundle> courtBundleOne = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCourtBundle())).build());

            Element<HearingCourtBundle> courtBundleTwo = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCourtBundle())).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(courtBundleOne, courtBundleTwo)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migrateCourtBundle(caseData);
            assertThat(updatedFields).extracting("courtBundleListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("courtBundleListCTSC").asList().isEmpty();
            assertThat(updatedFields).extracting("courtBundleListV2").asList().size().isEqualTo(2);
            assertThat(updatedFields).extracting("courtBundleListV2").asList().contains(courtBundleOne, courtBundleTwo);
            assertThat(updatedFields).extracting("courtBundleListV2Backup").asList()
                .contains(courtBundleOne, courtBundleTwo);
        }

        @Test
        void confidentialCourtBundlesUploadedByCTSCShouldGoIntoCourtBundleListCTSC() {
            UUID hearingId = UUID.randomUUID();

            Element<HearingCourtBundle> confidentialBundle = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCTSCCourtBundle())).build());

            Element<HearingCourtBundle> confidentialBundleTwo = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCTSCCourtBundle())).build());

            Element<HearingCourtBundle> nonConfidentialBundle = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCourtBundle())).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(confidentialBundle,
                        confidentialBundleTwo, nonConfidentialBundle)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migrateCourtBundle(caseData);
            assertThat(updatedFields).extracting("courtBundleListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("courtBundleListCTSC").asList().contains(confidentialBundle,
                confidentialBundleTwo);
            assertThat(updatedFields).extracting("courtBundleListV2").asList().contains(nonConfidentialBundle);
            assertThat(updatedFields).extracting("courtBundleListV2Backup").asList().contains(confidentialBundle,
                confidentialBundleTwo, nonConfidentialBundle);
        }

        @Test
        void confidentialCourtBundlesUploadedByLAShouldGoIntoCourtBundleListLA() {
            UUID hearingId = UUID.randomUUID();

            Element<HearingCourtBundle> confidentialBundleLA = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildLACourtBundle())).build());

            Element<HearingCourtBundle> confidentialBundleCTSC = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCTSCCourtBundle())).build());

            Element<HearingCourtBundle> nonConfidentialBundle = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCourtBundle())).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(confidentialBundleLA,
                        confidentialBundleCTSC, nonConfidentialBundle)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migrateCourtBundle(caseData);
            assertThat(updatedFields).extracting("courtBundleListLA").asList().contains(confidentialBundleLA);
            assertThat(updatedFields).extracting("courtBundleListCTSC").asList().contains(confidentialBundleCTSC);
            assertThat(updatedFields).extracting("courtBundleListV2").asList().contains(nonConfidentialBundle);
            assertThat(updatedFields).extracting("courtBundleListV2Backup").asList().contains(confidentialBundleLA,
                confidentialBundleCTSC, nonConfidentialBundle);
        }

        @Test
        void courtBundlesShouldBeInSameListAfterRollback() {
            UUID hearingId = UUID.randomUUID();

            Element<HearingCourtBundle> confidentialBundleLA = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildLACourtBundle())).build());

            Element<HearingCourtBundle> confidentialBundleCTSC = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCTSCCourtBundle())).build());

            Element<HearingCourtBundle> nonConfidentialBundle = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCourtBundle())).build());

            Map<String, Object> caseDataMap = new HashMap<String, Object>();
            caseDataMap.put("courtBundleListV2Backup", List.of(nonConfidentialBundle, confidentialBundleLA,
                confidentialBundleCTSC));
            caseDataMap.put("courtBundleListV2", List.of(nonConfidentialBundle));
            caseDataMap.put("courtBundleListLA", List.of(confidentialBundleLA));
            caseDataMap.put("courtBundleListCTSC", List.of(confidentialBundleCTSC));

            CaseDetails caseDetails = CaseDetails.builder().data(caseDataMap).build();

            assertThat(underTest.rollbackCourtBundleMigration(caseDetails))
                .containsOnlyKeys("courtBundleListV2", "courtBundleListLA", "courtBundleListCTSC",
                    "courtBundleListV2Backup");
            assertThat(underTest.rollbackCourtBundleMigration(caseDetails))
                .extracting("courtBundleListLA", "courtBundleListCTSC", "courtBundleListV2Backup")
                .containsExactly(List.of(), List.of(), List.of());
            assertThat(underTest.rollbackCourtBundleMigration(caseDetails))
                .extracting("courtBundleListV2").asList()
                .contains(nonConfidentialBundle, confidentialBundleLA, confidentialBundleCTSC);
        }
    }

    @Nested
    class MigrateApplicationDocuments {
        private final Map<ApplicationDocumentType, String> applicationDocumentTypeFieldNameMap = Map.of(
            THRESHOLD, "thresholdList",
            SWET, "documentsFiledOnIssueList",
            CARE_PLAN, "carePlanList",
            SOCIAL_WORK_CHRONOLOGY, "documentsFiledOnIssueList",
            SOCIAL_WORK_STATEMENT, "documentsFiledOnIssueList",
            GENOGRAM, "documentsFiledOnIssueList",
            CHECKLIST_DOCUMENT, "documentsFiledOnIssueList",
            BIRTH_CERTIFICATE, "documentsFiledOnIssueList",
            OTHER, "documentsFiledOnIssueList"
        );

        private final Map<ApplicationDocumentType, String> applicationDocumentTypeMethodMap = Map.of(
            THRESHOLD, "migrateApplicationDocuments",
            SWET, "migrateApplicationDocuments",
            CARE_PLAN, "migrateApplicationDocuments",
            SOCIAL_WORK_CHRONOLOGY, "migrateApplicationDocuments",
            SOCIAL_WORK_STATEMENT, "migrateApplicationDocuments",
            GENOGRAM, "migrateApplicationDocuments",
            CHECKLIST_DOCUMENT, "migrateApplicationDocuments",
            BIRTH_CERTIFICATE, "migrateApplicationDocuments",
            OTHER, "migrateApplicationDocuments"
        );

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = ApplicationDocumentType.class, names = {
            "THRESHOLD", "SWET", "CARE_PLAN",
            "SOCIAL_WORK_CHRONOLOGY", "SOCIAL_WORK_STATEMENT",
            "GENOGRAM", "CHECKLIST_DOCUMENT", "BIRTH_CERTIFICATE", "OTHER"})
        void shouldMigrateApplicationDocumentUploaded(ApplicationDocumentType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            ApplicationDocument ad1 = ApplicationDocument.builder()
                .documentType(type)
                .document(document1)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .applicationDocuments(List.of(element(doc1Id, ad1)))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> applicationDocumentTypeMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(applicationDocumentTypeFieldNameMap.get(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(applicationDocumentTypeFieldNameMap.get(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = ApplicationDocumentType.class, names = {
            "THRESHOLD", "SWET", "CARE_PLAN",
            "SOCIAL_WORK_CHRONOLOGY", "SOCIAL_WORK_STATEMENT",
            "GENOGRAM", "CHECKLIST_DOCUMENT", "BIRTH_CERTIFICATE", "OTHER"})
        void shouldMigrateConfidentialApplicationDocumentUploaded(ApplicationDocumentType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            ApplicationDocument ad1 = ApplicationDocument.builder()
                .documentType(type)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .applicationDocuments(List.of(element(doc1Id, ad1)))
                .build();

            Map<String, Object> updatedFields = (Map<String, Object>) stream(MigrateCFVService.class.getMethods())
                .filter(m -> applicationDocumentTypeMethodMap.get(type).equals(m.getName()))
                .findFirst().get()
                .invoke(underTest, caseData);

            assertThat(updatedFields).extracting(applicationDocumentTypeFieldNameMap.get(type)).asList()
                .isEmpty();
            assertThat(updatedFields).extracting(applicationDocumentTypeFieldNameMap.get(type) + "LA").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @Test
        void shouldMigrateMixedApplicationDocuments() {
            UUID doc1Id = UUID.randomUUID();
            UUID doc2Id = UUID.randomUUID();
            UUID doc3Id = UUID.randomUUID();
            UUID doc4Id = UUID.randomUUID();
            UUID doc5Id = UUID.randomUUID();
            UUID doc6Id = UUID.randomUUID();
            UUID doc7Id = UUID.randomUUID();
            UUID doc8Id = UUID.randomUUID();
            UUID doc9Id = UUID.randomUUID();
            UUID doc10Id = UUID.randomUUID();
            UUID doc11Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            DocumentReference document2 = DocumentReference.builder().build();
            DocumentReference document3 = DocumentReference.builder().build();
            DocumentReference document4 = DocumentReference.builder().build();
            DocumentReference document5 = DocumentReference.builder().build();
            DocumentReference document6 = DocumentReference.builder().build();
            DocumentReference document7 = DocumentReference.builder().build();
            DocumentReference document8 = DocumentReference.builder().build();
            DocumentReference document9 = DocumentReference.builder().build();
            DocumentReference document10 = DocumentReference.builder().build();
            DocumentReference document11 = DocumentReference.builder().build();

            ApplicationDocument ad1 = ApplicationDocument.builder()
                .documentType(THRESHOLD)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();
            ApplicationDocument ad2 = ApplicationDocument.builder()
                .documentType(THRESHOLD)
                .document(document2)
                .build();
            ApplicationDocument ad3 = ApplicationDocument.builder()
                .documentType(SWET)
                .document(document3)
                .build();
            ApplicationDocument ad4 = ApplicationDocument.builder()
                .documentType(CARE_PLAN)
                .document(document4)
                .build();
            ApplicationDocument ad5 = ApplicationDocument.builder()
                .documentType(SOCIAL_WORK_CHRONOLOGY)
                .document(document5)
                .build();
            ApplicationDocument ad6 = ApplicationDocument.builder()
                .documentType(SOCIAL_WORK_STATEMENT)
                .document(document6)
                .build();
            ApplicationDocument ad7 = ApplicationDocument.builder()
                .documentType(GENOGRAM)
                .document(document7)
                .build();
            ApplicationDocument ad8 = ApplicationDocument.builder()
                .documentType(CHECKLIST_DOCUMENT)
                .document(document8)
                .build();
            ApplicationDocument ad9 = ApplicationDocument.builder()
                .documentType(BIRTH_CERTIFICATE)
                .document(document9)
                .confidential(List.of("CONFIDENTIAL"))
                .build();
            ApplicationDocument ad10 = ApplicationDocument.builder()
                .documentType(OTHER)
                .document(document10)
                .build();
            ApplicationDocument ad11 = ApplicationDocument.builder()
                .documentType(CARE_PLAN)
                .document(document11)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .applicationDocuments(List.of(
                    element(doc1Id, ad1), element(doc2Id, ad2), element(doc3Id, ad3), element(doc4Id, ad4),
                    element(doc5Id, ad5), element(doc6Id, ad6), element(doc7Id, ad7), element(doc8Id, ad8),
                    element(doc9Id, ad9), element(doc10Id, ad10), element(doc11Id, ad11)
                ))
                .build();

            Map<String, Object> updatedFields = underTest.migrateApplicationDocuments(caseData);

            assertThat(updatedFields).extracting("thresholdListLA").asList().contains(
                element(doc1Id, ManagedDocument.builder().document(document1).build()));
            assertThat(updatedFields).extracting("thresholdList").asList().contains(
                element(doc2Id, ManagedDocument.builder().document(document2).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc3Id, ManagedDocument.builder().document(document3).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc5Id, ManagedDocument.builder().document(document5).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc6Id, ManagedDocument.builder().document(document6).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc7Id, ManagedDocument.builder().document(document7).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc8Id, ManagedDocument.builder().document(document8).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc10Id, ManagedDocument.builder().document(document10).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueListLA").asList().contains(
                element(doc9Id, ManagedDocument.builder().document(document9).build()));

            assertThat(updatedFields).extracting("carePlanList").asList()
                .contains(element(doc4Id, ManagedDocument.builder().document(document4).build()));
            assertThat(updatedFields).extracting("carePlanListLA").asList()
                .contains(element(doc11Id, ManagedDocument.builder().document(document11).build()));
        }

        @Test
        void shouldRollbackMigratedApplicationDocuments() {
            assertThat(underTest.rollbackApplicationDocuments()).containsOnlyKeys(
                "thresholdList",
                "thresholdListLA",
                "documentsFiledOnIssueList",
                "documentsFiledOnIssueListLA",
                "carePlanList",
                "carePlanListLA"
            );
            assertThat(underTest.rollbackApplicationDocuments()).extracting("thresholdList")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackApplicationDocuments()).extracting("thresholdListLA")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackApplicationDocuments()).extracting("documentsFiledOnIssueList")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackApplicationDocuments()).extracting("documentsFiledOnIssueListLA")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackApplicationDocuments()).extracting("carePlanList")
                .isEqualTo(List.of());
            assertThat(underTest.rollbackApplicationDocuments()).extracting("carePlanListLA")
                .isEqualTo(List.of());
        }
    }

    @Nested
    class MigrateSkeletonArgumentList {
        @Test
        void shouldMigrateSkeletonArgumentList() {
            Element<SkeletonArgument> skeletonArgumentNC = element(SkeletonArgument.builder()
                .hasConfidentialAddress(YesNo.NO.getValue()).build());
            Element<SkeletonArgument> skeletonArgumentConf = element(SkeletonArgument.builder()
                .hasConfidentialAddress(YesNo.YES.getValue())
                .build());

            List<Element<SkeletonArgument>> skeletonArgument = List.of(skeletonArgumentNC, skeletonArgumentConf);

            CaseData caseData = CaseData.builder()
                .hearingDocuments(HearingDocuments.builder()
                    .skeletonArgumentList(skeletonArgument).build())
                .build();

            Map<String, Object> updatedFields = underTest.migrateSkeletonArgumentList(caseData);
            assertThat(updatedFields).extracting("skeletonArgumentList").asList()
                .containsExactly(skeletonArgumentNC);
            assertThat(updatedFields).extracting("skeletonArgumentListLA").asList()
                .containsExactly(skeletonArgumentConf);
        }

        @Test
        void shouldDoNothingWhenNoSkeletonArgument() {
            CaseData caseData = CaseData.builder().build();

            Map<String, Object> updatedFields = underTest.migrateSkeletonArgumentList(caseData);

            assertThat(updatedFields).extracting("skeletonArgumentList").asList().isEmpty();
            assertThat(updatedFields).extracting("skeletonArgumentListLA").asList().isEmpty();
        }

        @Test
        void shouldRollbackMigratedSkeletonArgumentList() {
            Element<SkeletonArgument> skeletonArgument = element(SkeletonArgument.builder().build());
            Element<SkeletonArgument> skeletonArgumentLA = element(SkeletonArgument.builder().build());

            Map<String, Object> caseDataMap = new HashMap<String, Object>();
            caseDataMap.put("skeletonArgumentList", List.of(skeletonArgument));
            caseDataMap.put("skeletonArgumentListLA", List.of(skeletonArgumentLA));
            caseDataMap.put("skeletonArgumentListBackup", List.of(skeletonArgument, skeletonArgumentLA));

            CaseDetails caseDetails = CaseDetails.builder().data(caseDataMap).build();

            underTest.rollbackSkeletonArgumentList(caseDetails);

            assertThat(underTest.rollbackSkeletonArgumentList(caseDetails))
                .extracting("skeletonArgumentList").asList()
                .containsExactlyInAnyOrder(skeletonArgument, skeletonArgumentLA);
            assertThat(underTest.rollbackSkeletonArgumentList(caseDetails))
                .extracting("skeletonArgumentListLA").isEqualTo(List.of());
        }
    }
  
    @Nested
    class MigrateCorrespondence {
        @Test
        void shouldMigrateCorrespondenceDocuments() {
            Element<SupportingEvidenceBundle> correspondenceAdmin = element(SupportingEvidenceBundle.builder()
                .document(DocumentReference.builder().build()).confidential(List.of("CONFIDENTIAL"))
                .build());

            Element<SupportingEvidenceBundle> correspondenceAdminNC = element(SupportingEvidenceBundle.builder()
                .document(DocumentReference.builder().build())
                .build());

            Element<SupportingEvidenceBundle> correspondenceLA = element(SupportingEvidenceBundle.builder()
                .document(DocumentReference.builder().build()).confidential(List.of("CONFIDENTIAL"))
                .build());

            Element<SupportingEvidenceBundle> correspondenceLANC = element(SupportingEvidenceBundle.builder()
                .document(DocumentReference.builder().build())
                .build());

            Element<SupportingEvidenceBundle> correspondenceSolicitor = element(SupportingEvidenceBundle.builder()
                .document(DocumentReference.builder().build()).confidential(List.of("CONFIDENTIAL"))
                .build());

            Element<SupportingEvidenceBundle> correspondenceSolicitorNC = element(SupportingEvidenceBundle.builder()
                .document(DocumentReference.builder().build())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .correspondenceDocuments(List.of(correspondenceAdmin, correspondenceAdminNC))
                .correspondenceDocumentsLA(List.of(correspondenceLA, correspondenceLANC))
                .correspondenceDocumentsSolicitor(List.of(correspondenceSolicitor, correspondenceSolicitorNC))
                .build();

            Map<String, Object> updatedFields = underTest.migrateCorrespondenceDocuments(caseData);

            List<Element<ManagedDocument>> expectedCorrespondenceDocList =
                List.of(correspondenceAdminNC, correspondenceLANC, correspondenceSolicitorNC).stream()
                    .map(docElm -> element(docElm.getId(),
                        ManagedDocument.builder().document(docElm.getValue().getDocument()).build()))
                    .collect(Collectors.toList());

            assertThat(updatedFields).extracting("correspondenceDocList").asList()
                .containsExactlyInAnyOrderElementsOf(expectedCorrespondenceDocList);

            List<Element<ManagedDocument>> expectedCorrespondenceDocListLA =
                List.of(correspondenceLA, correspondenceSolicitor).stream()
                    .map(docElm -> element(docElm.getId(),
                        ManagedDocument.builder().document(docElm.getValue().getDocument()).build()))
                    .collect(Collectors.toList());

            assertThat(updatedFields).extracting("correspondenceDocListLA").asList()
                .containsExactlyInAnyOrderElementsOf(expectedCorrespondenceDocListLA);

            assertThat(updatedFields).extracting("correspondenceDocListCTSC").asList()
                .containsExactly(element(correspondenceAdmin.getId(),
                    ManagedDocument.builder().document(correspondenceAdmin.getValue().getDocument()).build()));
        }

        @Test
        void shouldRollbackMigrateCorrespondenceDocuments() {
            assertThat(underTest.rollbackCorrespondenceDocuments())
                .extracting("correspondenceDocList").isEqualTo(List.of());
            assertThat(underTest.rollbackCorrespondenceDocuments())
                .extracting("correspondenceDocListLA").isEqualTo(List.of());
            assertThat(underTest.rollbackCorrespondenceDocuments())
                .extracting("correspondenceDocListCTSC").isEqualTo(List.of());
        }
    }

    @Nested
    class DoHasCFVMigratedCheck {
        @Test
        void shouldDoHasCFVMigratedCheck() {
            assertDoesNotThrow(() -> underTest.doHasCFVMigratedCheck(1L, "NO", MIGRATION_ID));
            assertDoesNotThrow(() -> underTest.doHasCFVMigratedCheck(1L, "no", MIGRATION_ID));
            assertDoesNotThrow(() -> underTest.doHasCFVMigratedCheck(1L, "No", MIGRATION_ID));
            assertDoesNotThrow(() -> underTest.doHasCFVMigratedCheck(1L, "", MIGRATION_ID));
            assertDoesNotThrow(() -> underTest.doHasCFVMigratedCheck(1L, null, MIGRATION_ID));
        }

        @Test
        void shouldDoHasCFVMigratedCheckOnRollback() {
            assertDoesNotThrow(() -> underTest.doHasCFVMigratedCheck(1L, "YES", MIGRATION_ID, true));
            assertDoesNotThrow(() -> underTest.doHasCFVMigratedCheck(1L, "Yes", MIGRATION_ID, true));
            assertDoesNotThrow(() -> underTest.doHasCFVMigratedCheck(1L, "yes", MIGRATION_ID, true));
        }

        @Test
        void shouldThrowExceptionWhenDoHasCFVMigratedCheckFails() {
            assertThrows(AssertionError.class, () -> underTest.doHasCFVMigratedCheck(1L, "YES", MIGRATION_ID));
            assertThrows(AssertionError.class, () -> underTest.doHasCFVMigratedCheck(1L, "Yes", MIGRATION_ID));
        }

        @Test
        void shouldThrowExceptionWhenDoHasCFVMigratedCheckOnRollback() {
            assertThrows(AssertionError.class, () -> underTest.doHasCFVMigratedCheck(1L, "NO", MIGRATION_ID, true));
            assertThrows(AssertionError.class, () -> underTest.doHasCFVMigratedCheck(1L, "No", MIGRATION_ID, true));
            assertThrows(AssertionError.class, () -> underTest.doHasCFVMigratedCheck(1L, null, MIGRATION_ID, true));
            assertThrows(AssertionError.class, () -> underTest.doHasCFVMigratedCheck(1L, "", MIGRATION_ID, true));
        }
    }

    @Nested
    class ValidateFurtherEvidenceDocumentMigrationTest {

        @ParameterizedTest
        @ValueSource(strings = {
            "noticeOfActingOrIssueList",
            "noticeOfActingOrIssueListLA",
            "noticeOfActingOrIssueListCTSC",
            "guardianEvidenceList",
            "guardianEvidenceListLA",
            "guardianEvidenceListCTSC",
            "applicantWitnessStmtList",
            "applicantWitnessStmtListLA",
            "applicantWitnessStmtListCTSC",
            "expertReportList",
            "expertReportListLA",
            "expertReportListCTSC",
            "drugAndAlcoholReportList",
            "drugAndAlcoholReportListLA",
            "drugAndAlcoholReportListCTSC",
            "archivedDocumentsList",
            "archivedDocumentsListLA",
            "archivedDocumentsListCTSC"
        })
        public void shouldNotThrowExceptionWhenValidatingMigratedSingleFurtherEvidenceDocument(
            String migratedProperty) {
            Element<SupportingEvidenceBundle> doc1 = element(SupportingEvidenceBundle.builder()
                .type(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)
                .uploadedBy("solicitor@solicitor1.uk")
                .document(DocumentReference.builder().build())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocuments(List.of(doc1))
                .build();

            assertDoesNotThrow(() -> underTest.validateFurtherEvidenceDocument(MIGRATION_ID, caseData, Map.of(
                migratedProperty, List.of(element(ManageDocument.builder().build()))
            )));
        }

        @Test
        public void shouldNotThrowExceptionWhenValidatingMigratedFurtherEvidenceDocumentsNotUploadedByCTSC() {
            Element<SupportingEvidenceBundle> doc1 = element(SupportingEvidenceBundle.builder()
                .type(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)
                .uploadedBy("solicitor@solicitor1.uk")
                .document(DocumentReference.builder().build())
                .build());

            Element<SupportingEvidenceBundle> doc2 = element(SupportingEvidenceBundle.builder()
                .type(GUARDIAN_REPORTS)
                .uploadedBy("kurt@swansea.gov.uk")
                .document(DocumentReference.builder().build())
                .build());

            Element<SupportingEvidenceBundle> doc3 = element(SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .uploadedBy("solicitor@solicitor1.uk")
                .document(DocumentReference.builder().build())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsLA(List.of(doc2))
                .furtherEvidenceDocumentsSolicitor(List.of(doc1, doc3))
                .build();

            assertDoesNotThrow(() -> underTest.validateFurtherEvidenceDocument(MIGRATION_ID, caseData, Map.of(
                "noticeOfActingOrIssueList", List.of(element(ManageDocument.builder().build())),
                "guardianEvidenceList", List.of(element(ManageDocument.builder().build())),
                "applicantWitnessStmtList", List.of(element(ManageDocument.builder().build()))
            )));
        }

        @Test
        public void shouldNotThrowExceptionWhenValidatingMigratedFurtherEvidenceDocumentsWithoutDocumentType() {
            Element<SupportingEvidenceBundle> doc1 = element(SupportingEvidenceBundle.builder()
                .type(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)
                .uploadedBy("HMCTS")
                .document(DocumentReference.builder().build())
                .confidential(List.of("CONFIDENTIAL"))
                .build());

            Element<SupportingEvidenceBundle> doc2 = element(SupportingEvidenceBundle.builder()
                .uploadedBy("kurt@swansea.gov.uk")
                .document(DocumentReference.builder().build())
                .build());

            Element<SupportingEvidenceBundle> doc3 = element(SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .uploadedBy("solicitor@solicitor1.uk")
                .document(DocumentReference.builder().build())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocuments(List.of(doc1))
                .furtherEvidenceDocumentsLA(List.of(doc2))
                .furtherEvidenceDocumentsSolicitor(List.of(doc3))
                .build();

            assertDoesNotThrow(() -> underTest.validateFurtherEvidenceDocument(MIGRATION_ID, caseData, Map.of(
                "noticeOfActingOrIssueListCTSC", List.of(element(ManageDocument.builder().build())),
                "archivedDocumentsList", List.of(element(ManageDocument.builder().build())),
                "applicantWitnessStmtList", List.of(element(ManageDocument.builder().build()))
            )));
        }

        @Test
        public void shouldThrowExceptionWhenExpectedMigratedDocumentCountDoesNotMatch() {
            Element<SupportingEvidenceBundle> doc1 = element(SupportingEvidenceBundle.builder()
                .type(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)
                .uploadedBy("HMCTS")
                .document(DocumentReference.builder().build())
                .confidential(List.of("CONFIDENTIAL"))
                .build());

            Element<SupportingEvidenceBundle> doc2 = element(SupportingEvidenceBundle.builder()
                .uploadedBy("kurt@swansea.gov.uk")
                .document(DocumentReference.builder().build())
                .build());

            Element<SupportingEvidenceBundle> doc3 = element(SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .uploadedBy("solicitor@solicitor1.uk")
                .document(DocumentReference.builder().build())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocuments(List.of(doc1))
                .furtherEvidenceDocumentsLA(List.of(doc2))
                .furtherEvidenceDocumentsSolicitor(List.of(doc3))
                .build();

            assertThatThrownBy(() -> underTest.validateFurtherEvidenceDocument(MIGRATION_ID, caseData,
                Map.of("noticeOfActingOrIssueListCTSC", List.of(element(ManageDocument.builder().build())),
                    "archivedDocumentsList", List.of(element(ManageDocument.builder().build())))))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "FurtherEvidenceDocument/HearingFurtherEvidenceDocument (%s/%s)", MIGRATION_ID, 1L, 3, 2));
        }
    }

    @Nested
    class ValidateHearingFurtherEvidenceDocumentMigrationTest {

        @ParameterizedTest
        @ValueSource(strings = {
            "noticeOfActingOrIssueList",
            "noticeOfActingOrIssueListLA",
            "noticeOfActingOrIssueListCTSC",
            "guardianEvidenceList",
            "guardianEvidenceListLA",
            "guardianEvidenceListCTSC",
            "applicantWitnessStmtList",
            "applicantWitnessStmtListLA",
            "applicantWitnessStmtListCTSC",
            "expertReportList",
            "expertReportListLA",
            "expertReportListCTSC",
            "drugAndAlcoholReportList",
            "drugAndAlcoholReportListLA",
            "drugAndAlcoholReportListCTSC",
            "archivedDocumentsList",
            "archivedDocumentsListLA",
            "archivedDocumentsListCTSC"
        })
        public void shouldNotThrowExceptionWhenValidatingMigratedSingleHearingFurtherEvidenceDocument(
            String migratedProperty) {
            Element<SupportingEvidenceBundle> doc1 = element(SupportingEvidenceBundle.builder()
                .type(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)
                .uploadedBy("solicitor@solicitor1.uk")
                .document(DocumentReference.builder().build())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(doc1))
                    .build())))
                .build();

            assertDoesNotThrow(() -> underTest.validateFurtherEvidenceDocument(MIGRATION_ID, caseData, Map.of(
                migratedProperty, List.of(element(ManageDocument.builder().build()))
            )));
        }

        @Test
        public void shouldNotThrowExceptionWhenValidatingMigratedHearingFurtherEvidenceDocumentsNotUploadedByCTSC() {
            Element<SupportingEvidenceBundle> doc1 = element(SupportingEvidenceBundle.builder()
                .type(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)
                .uploadedBy("solicitor@solicitor1.uk")
                .document(DocumentReference.builder().build())
                .build());

            Element<SupportingEvidenceBundle> doc2 = element(SupportingEvidenceBundle.builder()
                .type(GUARDIAN_REPORTS)
                .uploadedBy("kurt@swansea.gov.uk")
                .document(DocumentReference.builder().build())
                .build());

            Element<SupportingEvidenceBundle> doc3 = element(SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .uploadedBy("solicitor@solicitor1.uk")
                .document(DocumentReference.builder().build())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(doc1, doc2, doc3))
                    .build())))
                .build();

            assertDoesNotThrow(() -> underTest.validateFurtherEvidenceDocument(MIGRATION_ID, caseData, Map.of(
                "noticeOfActingOrIssueList", List.of(element(ManageDocument.builder().build())),
                "guardianEvidenceList", List.of(element(ManageDocument.builder().build())),
                "applicantWitnessStmtList", List.of(element(ManageDocument.builder().build()))
            )));
        }

        @Test
        public void shouldNotThrowExceptionWhenValidatingMigratedHearingFurtherEvidenceDocumentsWithoutDocumentType() {
            Element<SupportingEvidenceBundle> doc1 = element(SupportingEvidenceBundle.builder()
                .type(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)
                .uploadedBy("HMCTS")
                .document(DocumentReference.builder().build())
                .confidential(List.of("CONFIDENTIAL"))
                .build());

            Element<SupportingEvidenceBundle> doc2 = element(SupportingEvidenceBundle.builder()
                .uploadedBy("kurt@swansea.gov.uk")
                .document(DocumentReference.builder().build())
                .build());

            Element<SupportingEvidenceBundle> doc3 = element(SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .uploadedBy("solicitor@solicitor1.uk")
                .document(DocumentReference.builder().build())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(doc1, doc2, doc3))
                    .build())))
                .build();

            assertDoesNotThrow(() -> underTest.validateFurtherEvidenceDocument(MIGRATION_ID, caseData, Map.of(
                "noticeOfActingOrIssueListCTSC", List.of(element(ManageDocument.builder().build())),
                "archivedDocumentsList", List.of(element(ManageDocument.builder().build())),
                "applicantWitnessStmtList", List.of(element(ManageDocument.builder().build()))
            )));
        }

        @Test
        public void shouldThrowExceptionWhenExpectedMigratedDocumentCountDoesNotMatch() {
            Element<SupportingEvidenceBundle> doc1 = element(SupportingEvidenceBundle.builder()
                .type(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)
                .uploadedBy("HMCTS")
                .document(DocumentReference.builder().build())
                .confidential(List.of("CONFIDENTIAL"))
                .build());

            Element<SupportingEvidenceBundle> doc2 = element(SupportingEvidenceBundle.builder()
                .uploadedBy("kurt@swansea.gov.uk")
                .document(DocumentReference.builder().build())
                .build());

            Element<SupportingEvidenceBundle> doc3 = element(SupportingEvidenceBundle.builder()
                .type(APPLICANT_STATEMENT)
                .uploadedBy("solicitor@solicitor1.uk")
                .document(DocumentReference.builder().build())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(element(HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(doc1, doc2, doc3))
                    .build())))
                .build();

            assertThatThrownBy(() -> underTest.validateFurtherEvidenceDocument(MIGRATION_ID, caseData,
                Map.of("noticeOfActingOrIssueListCTSC", List.of(element(ManageDocument.builder().build())),
                    "archivedDocumentsList", List.of(element(ManageDocument.builder().build())))))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "FurtherEvidenceDocument/HearingFurtherEvidenceDocument (%s/%s)", MIGRATION_ID, 1L, 3, 2));
        }
    }

    @Nested
    class ValidateCaseSummaryMigrationTest {

        @ParameterizedTest
        @ValueSource(strings = {"caseSummaryList", "caseSummaryListLA"})
        public void shouldNotThrowExceptionWhenValidatingSingleMigratedCaseSummary(String migratedProperty) {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder().caseSummaryList(
                    List.of(element(CaseSummary.builder()
                        .document(DocumentReference.builder().build())
                        .hasConfidentialAddress("YES")
                        .build()))
                ).build())
                .build();

            assertDoesNotThrow(() -> underTest.validateMigratedCaseSummary(MIGRATION_ID, caseData, Map.of(
                migratedProperty, List.of(element(CaseSummary.builder()
                    .hasConfidentialAddress("YES")
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @Test
        public void shouldNotThrowExceptionWhenValidatingMigratedMultipleCaseSummaries() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder().caseSummaryList(
                    List.of(
                        element(CaseSummary.builder()
                            .document(DocumentReference.builder().build())
                            .hasConfidentialAddress("YES")
                            .build()),
                        element(CaseSummary.builder()
                            .document(DocumentReference.builder().build())
                            .build()))).build())
                .build();

            assertDoesNotThrow(() -> underTest.validateMigratedCaseSummary(MIGRATION_ID, caseData, Map.of(
                "caseSummaryList", List.of(element(CaseSummary.builder()
                    .document(DocumentReference.builder().build())
                    .build())),
                "caseSummaryListLA", List.of(element(CaseSummary.builder()
                    .hasConfidentialAddress("YES")
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @Test
        public void shouldThrowExceptionWhenExpectedMigratedDocumentCountDoesNotMatch() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder().caseSummaryList(
                    List.of(
                        element(CaseSummary.builder()
                            .document(DocumentReference.builder().build())
                            .hasConfidentialAddress("YES")
                            .build()),
                        element(CaseSummary.builder()
                            .document(DocumentReference.builder().build())
                            .build()))).build())
                .build();

            assertThatThrownBy(() -> underTest.validateMigratedCaseSummary(MIGRATION_ID, caseData,
                Map.of("caseSummaryList", List.of(element(CaseSummary.builder()
                        .document(DocumentReference.builder().build())
                        .build())))))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "CaseSummary (%s/%s)", MIGRATION_ID, 1L, 2, 1));
        }
    }

    @Nested
    class ValidatePositionStatementMigrationTest {

        @ParameterizedTest
        @ValueSource(strings = {"posStmtRespList", "posStmtRespListLA", "posStmtRespListCTSC"})
        public void shouldNotThrowExceptionWhenValidatingSingleMigratedPositionStatementResp(String migratedProperty) {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder().positionStatementRespondentListV2(
                    List.of(element(PositionStatementRespondent.builder()
                        .document(DocumentReference.builder().build())
                        .build()))
                ).build())
                .build();

            assertDoesNotThrow(() -> underTest.validateMigratedPositionStatement(MIGRATION_ID, caseData, Map.of(
                migratedProperty, List.of(element(PositionStatementRespondent.builder()
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @ParameterizedTest
        @ValueSource(strings = {"posStmtChildList", "posStmtChildListLA", "posStmtChildListCTSC"})
        public void shouldNotThrowExceptionWhenValidatingSingleMigratedPositionStatementChild(String migratedProperty) {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder().positionStatementChildListV2(
                    List.of(element(PositionStatementChild.builder()
                        .document(DocumentReference.builder().build())
                        .build()))
                ).build())
                .build();

            assertDoesNotThrow(() -> underTest.validateMigratedPositionStatement(MIGRATION_ID, caseData, Map.of(
                migratedProperty, List.of(element(PositionStatementChild.builder()
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @Test
        public void shouldNotThrowExceptionWhenValidatingMigratedMultiplePositionStatements() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementChildListV2(List.of(
                        element(PositionStatementChild.builder().document(DocumentReference.builder().build()).build())
                    ))
                    .positionStatementRespondentListV2(List.of(
                        element(PositionStatementRespondent.builder().document(DocumentReference.builder().build())
                            .build())
                    ))
                    .build()
                ).build();

            assertDoesNotThrow(() -> underTest.validateMigratedPositionStatement(MIGRATION_ID, caseData, Map.of(
                "posStmtRespList", List.of(element(PositionStatementRespondent.builder()
                        .document(DocumentReference.builder().build())
                        .build())),
                "posStmtChildList", List.of(element(PositionStatementChild.builder()
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @Test
        public void shouldThrowExceptionWhenExpectedMigratedDocumentCountDoesNotMatch() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementChildListV2(List.of(
                        element(PositionStatementChild.builder().document(DocumentReference.builder().build()).build())
                    ))
                    .build()
                ).build();
            
            assertThatThrownBy(() -> underTest.validateMigratedPositionStatement(MIGRATION_ID, caseData,
                Map.of(
                    "posStmtRespList", List.of(element(PositionStatementRespondent.builder()
                        .document(DocumentReference.builder().build())
                        .build())),
                    "posStmtChildList", List.of(element(PositionStatementChild.builder()
                        .document(DocumentReference.builder().build())
                        .build()))
                )))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "PositionStatement(Child/Respondent) (%s/%s)", MIGRATION_ID, 1L, 1, 2));
        }
    }

    @Nested
    class ValidateRespondentStatementMigrationTest {

        @ParameterizedTest
        @ValueSource(strings = {"respStmtList", "respStmtListLA", "respStmtListCTSC"})
        public void shouldNotThrowExceptionWhenValidatingSingleMigratedRespondentStatement(String migratedProperty) {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(element(RespondentStatement.builder()
                    .supportingEvidenceBundle(List.of(
                        element(SupportingEvidenceBundle.builder()
                            .document(DocumentReference.builder().build())
                            .build())
                    )).build())))
                .build();

            assertDoesNotThrow(() -> underTest.validateMigratedRespondentStatement(MIGRATION_ID, caseData, Map.of(
                migratedProperty, List.of(element(RespondentStatementV2.builder()
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @Test
        public void shouldNotThrowExceptionWhenValidatingMigratedMultipleRespondentStatements() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(element(RespondentStatement.builder()
                    .supportingEvidenceBundle(List.of(
                        element(SupportingEvidenceBundle.builder()
                            .document(DocumentReference.builder().build())
                            .build()),
                        element(SupportingEvidenceBundle.builder()
                            .document(DocumentReference.builder().build())
                            .build())
                    )).build())))
                .build();

            assertDoesNotThrow(() -> underTest.validateMigratedRespondentStatement(MIGRATION_ID, caseData, Map.of(
                "respStmtList", List.of(element(RespondentStatementV2.builder()
                    .document(DocumentReference.builder().build())
                    .build())),
                "respStmtListLA", List.of(element(RespondentStatementV2.builder()
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @Test
        public void shouldThrowExceptionWhenExpectedMigratedDocumentCountDoesNotMatch() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(element(RespondentStatement.builder()
                    .supportingEvidenceBundle(List.of(
                        element(SupportingEvidenceBundle.builder()
                            .document(DocumentReference.builder().build())
                            .build()),
                        element(SupportingEvidenceBundle.builder()
                            .document(DocumentReference.builder().build())
                            .build())
                    )).build())))
                .build();

            assertThatThrownBy(() -> underTest.validateMigratedRespondentStatement(MIGRATION_ID, caseData,
                Map.of(
                    "respStmtListLA", List.of(element(RespondentStatementV2.builder()
                        .document(DocumentReference.builder().build())
                        .build()))
                )))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "respondent statements (%s/%s)", MIGRATION_ID, 1L, 2, 1));
        }
    }

    @Nested
    class ValidateCorrespondenceDocumentMigrationTest {

        private CaseData resolveCaseDataByMigratedProperty(String migratedProperty) {
            CaseData caseData = null;
            switch (migratedProperty) {
                case "correspondenceDocList":
                    caseData = CaseData.builder()
                        .id(1L)
                        .correspondenceDocumentsSolicitor(List.of(element(SupportingEvidenceBundle.builder()
                            .document(DocumentReference.builder().build())
                            .build())))
                        .build();
                    break;
                case "correspondenceDocListLA":
                    caseData = CaseData.builder()
                        .id(1L)
                        .correspondenceDocumentsLA(List.of(element(SupportingEvidenceBundle.builder()
                            .document(DocumentReference.builder().build())
                            .build())))
                        .build();
                    break;
                case "correspondenceDocListCTSC":
                    caseData = CaseData.builder()
                        .id(1L)
                        .correspondenceDocuments(List.of(element(SupportingEvidenceBundle.builder()
                            .document(DocumentReference.builder().build())
                            .build())))
                        .build();
                    break;
                default:
                    throw new IllegalArgumentException("unable to resolve migrated property");
            }
            return caseData;
        }

        @ParameterizedTest
        @ValueSource(strings = {"correspondenceDocList", "correspondenceDocListLA", "correspondenceDocListCTSC"})
        public void shouldNotThrowExceptionWhenValidatingSingleMigratedCorrespondenceDocument(String migratedProperty) {
            CaseData caseData = resolveCaseDataByMigratedProperty(migratedProperty);
            assertDoesNotThrow(() -> underTest.validateMigratedCorrespondenceDocuments(MIGRATION_ID, caseData, Map.of(
                migratedProperty, List.of(element(ManagedDocument.builder()
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @Test
        public void shouldNotThrowExceptionWhenValidatingMigratedMultipleCorrespondenceDocuments() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .correspondenceDocuments(List.of(
                    element(SupportingEvidenceBundle.builder().document(DocumentReference.builder().build()).build()),
                    element(SupportingEvidenceBundle.builder().document(DocumentReference.builder().build()).build())
                ))
                .build();

            assertDoesNotThrow(() -> underTest.validateMigratedCorrespondenceDocuments(MIGRATION_ID, caseData, Map.of(
                "correspondenceDocList", List.of(element(ManagedDocument.builder()
                    .document(DocumentReference.builder().build())
                    .build())),
                "correspondenceDocListLA", List.of(element(ManagedDocument.builder()
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @Test
        public void shouldThrowExceptionWhenExpectedMigratedDocumentCountDoesNotMatch() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .correspondenceDocuments(List.of(
                    element(SupportingEvidenceBundle.builder().document(DocumentReference.builder().build()).build()),
                    element(SupportingEvidenceBundle.builder().document(DocumentReference.builder().build()).build())
                ))
                .build();

            assertThatThrownBy(() -> underTest.validateMigratedCorrespondenceDocuments(MIGRATION_ID, caseData,
                Map.of(
                    "correspondenceDocListCTSC", List.of(element(ManagedDocument.builder()
                        .document(DocumentReference.builder().build())
                        .build()))
                )))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "correspondence documents (%s/%s)", MIGRATION_ID, 1L, 2, 1));
        }
    }

    @Nested
    class ValidateApplicationDocumentMigrationTest {

        private CaseData resolveCaseDataByMigratedProperty(String migratedProperty) {
            CaseData caseData = null;
            switch (migratedProperty) {
                case "documentsFiledOnIssueList":
                case "documentsFiledOnIssueListLA":
                case "documentsFiledOnIssueListCTSC":
                    caseData = CaseData.builder()
                        .id(1L)
                        .applicationDocuments(List.of(element(ApplicationDocument.builder()
                            .documentType(CHECKLIST_DOCUMENT)
                            .document(DocumentReference.builder().build())
                            .build())))
                        .build();
                    break;
                case "carePlanList":
                case "carePlanListLA":
                case "carePlanListCTSC":
                    caseData = CaseData.builder()
                        .id(1L)
                        .applicationDocuments(List.of(element(ApplicationDocument.builder()
                            .documentType(CARE_PLAN)
                            .document(DocumentReference.builder().build())
                            .build())))
                        .build();
                    break;
                case "thresholdList":
                case "thresholdListLA":
                case "thresholdListCTSC":
                    caseData = CaseData.builder()
                        .id(1L)
                        .applicationDocuments(List.of(element(ApplicationDocument.builder()
                            .documentType(THRESHOLD)
                            .document(DocumentReference.builder().build())
                            .build())))
                        .build();
                    break;
                default:
                    throw new IllegalArgumentException("unable to resolve migrated property");
            }
            return caseData;
        }

        @ParameterizedTest
        @ValueSource(strings = {"documentsFiledOnIssueList", "documentsFiledOnIssueListLA",
            "documentsFiledOnIssueListCTSC",
            "carePlanList", "carePlanListLA", "carePlanListCTSC",
            "thresholdList", "thresholdListLA", "thresholdListCTSC"})
        public void shouldNotThrowExceptionWhenValidatingSingleMigratedApplicationDocument(String migratedProperty) {
            CaseData caseData = resolveCaseDataByMigratedProperty(migratedProperty);
            assertDoesNotThrow(() -> underTest.validateMigratedApplicationDocuments(MIGRATION_ID, caseData, Map.of(
                migratedProperty, List.of(element(ManagedDocument.builder()
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @Test
        public void shouldNotThrowExceptionWhenValidatingMigratedMultipleApplicationDocuments() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .applicationDocuments(List.of(
                    element(ApplicationDocument.builder()
                        .documentType(THRESHOLD)
                        .document(DocumentReference.builder().build())
                        .build()),
                    element(ApplicationDocument.builder()
                        .documentType(SWET)
                        .document(DocumentReference.builder().build())
                        .build())
                ))
                .build();

            assertDoesNotThrow(() -> underTest.validateMigratedApplicationDocuments(MIGRATION_ID, caseData, Map.of(
                "carePlanList", List.of(element(ManagedDocument.builder()
                    .document(DocumentReference.builder().build())
                    .build())),
                "thresholdListLA", List.of(element(ManagedDocument.builder()
                    .document(DocumentReference.builder().build())
                    .build()))
            )));
        }

        @Test
        public void shouldThrowExceptionWhenExpectedMigratedDocumentCountDoesNotMatch() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .applicationDocuments(List.of(
                    element(ApplicationDocument.builder()
                        .documentType(THRESHOLD)
                        .document(DocumentReference.builder().build())
                        .build()),
                    element(ApplicationDocument.builder()
                        .documentType(SWET)
                        .document(DocumentReference.builder().build())
                        .build())
                ))
                .build();

            assertThatThrownBy(() -> underTest.validateMigratedApplicationDocuments(MIGRATION_ID, caseData,
                Map.of(
                    "carePlanList", List.of(element(ManagedDocument.builder()
                        .document(DocumentReference.builder().build())
                        .build()))
                )))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "application documents (%s/%s)", MIGRATION_ID, 1L, 2, 1));
        }
    }

    @Nested
    class ValidateCourtBundleMigrationTest {

        private CaseData resolveCaseDataByMigratedProperty(String migratedProperty) {
            CaseData caseData = null;
            switch (migratedProperty) {
                case "courtBundleListV2":
                case "courtBundleListLA":
                case "courtBundleListCTSC":
                    caseData = CaseData.builder()
                        .id(1L)
                        .hearingDocuments(HearingDocuments.builder()
                            .courtBundleListV2(List.of(
                                element(HearingCourtBundle.builder()
                                    .courtBundle(List.of(element(CourtBundle.builder()
                                        .document(DocumentReference.builder().build())
                                        .build())))
                                    .build())
                            ))
                            .build())
                        .build();
                    break;
                default:
                    throw new IllegalArgumentException("unable to resolve migrated property");
            }
            return caseData;
        }

        @ParameterizedTest
        @ValueSource(strings = {"courtBundleListV2", "courtBundleListLA", "courtBundleListCTSC"})
        public void shouldNotThrowExceptionWhenValidatingSingleMigratedCourtBundle(String migratedProperty) {
            CaseData caseData = resolveCaseDataByMigratedProperty(migratedProperty);
            assertDoesNotThrow(() -> underTest.validateMigratedCourtBundle(MIGRATION_ID, caseData, Map.of(
                migratedProperty, List.of(element(HearingCourtBundle.builder()
                    .courtBundle(List.of(element(CourtBundle.builder()
                        .document(DocumentReference.builder().build())
                        .build())))
                    .build()))
            )));
        }

        @ParameterizedTest
        @ValueSource(strings = {"courtBundleListV2", "courtBundleListLA", "courtBundleListCTSC"})
        public void shouldNotThrowExceptionWhenValidatingMigratedMultipleCourtBundles(String migratedProperty) {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(
                        element(HearingCourtBundle.builder()
                            .courtBundle(List.of(
                                element(CourtBundle.builder().document(DocumentReference.builder().build()).build()),
                                element(CourtBundle.builder().document(DocumentReference.builder().build()).build())
                            ))
                            .build())
                    ))
                    .build())
                .build();

            assertDoesNotThrow(() -> underTest.validateMigratedCourtBundle(MIGRATION_ID, caseData, Map.of(
                migratedProperty,  List.of(element(HearingCourtBundle.builder()
                    .courtBundle(List.of(
                        element(CourtBundle.builder().document(DocumentReference.builder().build()).build()),
                        element(CourtBundle.builder().document(DocumentReference.builder().build()).build())
                    ))
                    .build()))
            )));
        }

        @ParameterizedTest
        @ValueSource(strings = {"courtBundleListV2", "courtBundleListLA", "courtBundleListCTSC"})
        public void shouldNotThrowExceptionWhenValidatingMigratedMultipleHearingCourtBundles(String migratedProperty) {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(
                        element(HearingCourtBundle.builder()
                            .courtBundle(List.of(
                                element(CourtBundle.builder().document(DocumentReference.builder().build()).build())
                            ))
                            .build()),
                        element(HearingCourtBundle.builder()
                            .courtBundle(List.of(
                                element(CourtBundle.builder().document(DocumentReference.builder().build()).build())
                            ))
                            .build())
                    ))
                    .build())
                .build();

            assertDoesNotThrow(() -> underTest.validateMigratedCourtBundle(MIGRATION_ID, caseData, Map.of(
                migratedProperty,  List.of(
                    element(HearingCourtBundle.builder().courtBundle(List.of(
                        element(CourtBundle.builder().document(DocumentReference.builder().build()).build())
                    )).build()),
                    element(HearingCourtBundle.builder().courtBundle(List.of(
                        element(CourtBundle.builder().document(DocumentReference.builder().build()).build())
                    )).build())
                )
            )));
        }

        @Test
        public void shouldThrowExceptionWhenExpectedMigratedDocumentCountDoesNotMatch() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(
                        element(HearingCourtBundle.builder()
                            .courtBundle(List.of(
                                element(CourtBundle.builder().document(DocumentReference.builder().build()).build()),
                                element(CourtBundle.builder().document(DocumentReference.builder().build()).build())
                            ))
                            .build())
                    ))
                    .build())
                .build();

            assertThatThrownBy(() -> underTest.validateMigratedCourtBundle(MIGRATION_ID, caseData,
                Map.of(
                    "courtBundleListV2",List.of(element(HearingCourtBundle.builder()
                        .courtBundle(List.of(
                            element(CourtBundle.builder().document(DocumentReference.builder().build()).build())
                        ))
                        .build()))
                )))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "court bundles (%s/%s)", MIGRATION_ID, 1L, 2, 1));
        }
    }
}
