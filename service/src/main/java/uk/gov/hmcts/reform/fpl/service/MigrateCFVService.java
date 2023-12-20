package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.OTHER_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType.PROFESSIONAL_DRUG;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType.PROFESSIONAL_HAIR;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType.TOXICOLOGY_REPORT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCFVService {

    private final ApplicationDocumentsService applicationDocumentsService;

    private static Element<RespondentStatementV2> toRespondentStatementV2(
        UUID respondentId, String respondentName, Element<SupportingEvidenceBundle> sebElement) {
        SupportingEvidenceBundle seb = sebElement.getValue();
        return element(sebElement.getId(), RespondentStatementV2.builder()
            .respondentId(respondentId)
            .respondentName(respondentName)
            .confidential(seb.getConfidential())
            .dateTimeReceived(seb.getDateTimeReceived())
            .dateTimeUploaded(seb.getDateTimeUploaded())
            .document(seb.getDocument())
            .documentAcknowledge(seb.getDocumentAcknowledge())
            .expertReportType(seb.getExpertReportType())
            .hasConfidentialAddress(seb.getHasConfidentialAddress())
            .name(seb.getName())
            .notes(seb.getNotes())
            .removalReason(seb.getRemovalReason())
            .translatedDocument(seb.getTranslatedDocument())
            .translationRequirements(seb.getTranslationRequirements())
            .translationUploadDateTime(seb.getTranslationUploadDateTime())
            .type(seb.getType())
            .uploadedBy(seb.getUploadedBy())
            .uploadedBySolicitor(seb.getUploadedBySolicitor())
            .build());
    }

    private static Predicate<Element<SupportingEvidenceBundle>> expertReportTypePredicate(
        FurtherEvidenceType furtherEvidenceType, String newFieldName) {
        final List<ExpertReportType> drugAndAlcoholReportTypes = List.of(PROFESSIONAL_DRUG, PROFESSIONAL_HAIR,
            TOXICOLOGY_REPORT);
        return a -> {
            if (EXPERT_REPORTS.equals(furtherEvidenceType)) {
                switch (newFieldName) {
                    case "expertReportList":
                        return !drugAndAlcoholReportTypes.contains(a.getValue().getExpertReportType());
                    case "drugAndAlcoholReportList":
                        return drugAndAlcoholReportTypes.contains(a.getValue().getExpertReportType());
                    default:
                        return true;
                }
            } else {
                return true;
            }
        };
    }

    private static Function<Element<SupportingEvidenceBundle>, Element<ManagedDocument>> toManagedDocumentElement() {
        return seb -> element(seb.getId(), ManagedDocument.builder().document(seb.getValue().getDocument()).build());
    }

    private static Function<Element<CaseSummary>, Element<ManagedDocument>> caseSummaryToManagedDocumentElement() {
        return seb -> element(seb.getId(), ManagedDocument.builder().document(seb.getValue().getDocument()).build());
    }

    private static Function<Element<PositionStatementChild>, Element<ManagedDocument>>
        positionStatementChildToManagedDocumentElement() {
        return seb -> element(seb.getId(), ManagedDocument.builder().document(seb.getValue().getDocument()).build());
    }

    private static Function<Element<PositionStatementRespondent>, Element<ManagedDocument>>
        positionStatementRespondentToManagedDocumentElement() {
        return seb -> element(seb.getId(), ManagedDocument.builder().document(seb.getValue().getDocument()).build());
    }

    private static Function<Element<ApplicationDocument>, Element<ManagedDocument>>
        applicationDocumentToManagedDocumentElement() {
        return seb -> element(seb.getId(), ManagedDocument.builder().document(seb.getValue().getDocument()).build());
    }

    private static Function<Element<CourtBundle>, Element<ManagedDocument>>
        courtBundleToManagedDocumentElement() {
        return seb -> element(seb.getId(), ManagedDocument.builder().document(seb.getValue().getDocument()).build());
    }

    private static Predicate<Element<SupportingEvidenceBundle>> matchFurtherEvidenceType(FurtherEvidenceType type) {
        if (type == null) {
            return e -> Optional.ofNullable(e.getValue()).orElse(SupportingEvidenceBundle.builder().type(null).build())
                .getType() == null;
        }
        return e -> type.equals(e.getValue().getType());
    }

    private static Predicate<Element<SupportingEvidenceBundle>> isConfidentialDocument() {
        return e -> e.getValue().isConfidentialDocument();
    }

    private static Predicate<Element<SupportingEvidenceBundle>> isNonConfidentialDocument() {
        return e -> !e.getValue().isConfidentialDocument();
    }

    private static Predicate<Element<SupportingEvidenceBundle>> isUploadedByHMCTS() {
        return e -> e.getValue().isUploadedByHMCTS();
    }

    private static Predicate<Element<SupportingEvidenceBundle>> isNotUploadedByHMCTS() {
        return e -> !e.getValue().isUploadedByHMCTS();
    }

    private static List<Element<ManagedDocument>> convertManagedDocumentFromFurtherEvidenceDocumentByLA(
        CaseData caseData,
        FurtherEvidenceType furtherEvidenceType,
        Predicate<Element<SupportingEvidenceBundle>> expertReportTypePredicate
    ) {
        return Optional.ofNullable(caseData.getFurtherEvidenceDocumentsLA()).orElse(List.of()).stream()
            .filter(matchFurtherEvidenceType(furtherEvidenceType))
            .filter(expertReportTypePredicate)
            .filter(isConfidentialDocument())
            .map(toManagedDocumentElement())
            .collect(toList());
    }

    private static List<Element<ManagedDocument>> convertManagedDocumentFromFurtherEvidenceDocumentByAdmin(
        CaseData caseData,
        FurtherEvidenceType furtherEvidenceType,
        Predicate<Element<SupportingEvidenceBundle>> expertReportTypePredicate
    ) {
        return Optional.ofNullable(caseData.getFurtherEvidenceDocuments()).orElse(List.of()).stream()
            .filter(matchFurtherEvidenceType(furtherEvidenceType))
            .filter(expertReportTypePredicate)
            .filter(isConfidentialDocument())
            .map(toManagedDocumentElement())
            .collect(toList());
    }

    private static List<Element<ManagedDocument>> convertManagedDocumentFromNonConfidentialFurtherEvidenceDocument(
        CaseData caseData,
        FurtherEvidenceType furtherEvidenceType,
        Predicate<Element<SupportingEvidenceBundle>> expertReportTypePredicate
    ) {
        final List<Element<ManagedDocument>> newDocList =
            Optional.ofNullable(caseData.getFurtherEvidenceDocumentsLA()).orElse(List.of()).stream()
                .filter(matchFurtherEvidenceType(furtherEvidenceType))
                .filter(expertReportTypePredicate)
                .filter(isNonConfidentialDocument())
                .map(toManagedDocumentElement())
                .collect(toList());
        newDocList.addAll(
            Optional.ofNullable(caseData.getFurtherEvidenceDocuments()).orElse(List.of()).stream()
                .filter(matchFurtherEvidenceType(furtherEvidenceType))
                .filter(expertReportTypePredicate)
                .filter(isNonConfidentialDocument())
                .map(toManagedDocumentElement())
                .toList());
        newDocList.addAll(
            Optional.ofNullable(caseData.getFurtherEvidenceDocumentsSolicitor()).orElse(List.of()).stream()
                .filter(matchFurtherEvidenceType(furtherEvidenceType))
                .filter(expertReportTypePredicate)
                .map(toManagedDocumentElement())
                .toList());
        return newDocList;
    }

    private static List<Element<ManagedDocument>> convertManagedDocumentFromHearingFurtherEvidenceDocumentByAdmin(
        CaseData caseData,
        FurtherEvidenceType furtherEvidenceType,
        Predicate<Element<SupportingEvidenceBundle>> expertReportTypePredicate
    ) {
        return Optional.ofNullable(caseData.getHearingFurtherEvidenceDocuments()).orElse(List.of()).stream()
            .flatMap(e -> e.getValue().getSupportingEvidenceBundle().stream())
            .filter(matchFurtherEvidenceType(furtherEvidenceType))
            .filter(expertReportTypePredicate)
            .filter(isConfidentialDocument())
            .filter(isUploadedByHMCTS())
            .map(toManagedDocumentElement())
            .collect(toList());
    }

    private static List<Element<ManagedDocument>> convertManagedDocumentFromHearingFurtherEvidenceDocumentByLA(
        CaseData caseData,
        FurtherEvidenceType furtherEvidenceType,
        Predicate<Element<SupportingEvidenceBundle>> expertReportTypePredicate
    ) {
        return Optional.ofNullable(caseData.getHearingFurtherEvidenceDocuments()).orElse(List.of()).stream()
            .flatMap(e -> e.getValue().getSupportingEvidenceBundle().stream())
            .filter(matchFurtherEvidenceType(furtherEvidenceType))
            .filter(expertReportTypePredicate)
            .filter(isConfidentialDocument()) // Private solicitor cannot upload confidential documents.
            .filter(isNotUploadedByHMCTS())  // i.e. confidential documents are uploaded by LA
            .map(toManagedDocumentElement())
            .collect(toList());
    }

    private static List<Element<ManagedDocument>>
        convertManagedDocumentFromNonConfidentialHearingFurtherEvidenceDocument(
        CaseData caseData,
        FurtherEvidenceType furtherEvidenceType,
        Predicate<Element<SupportingEvidenceBundle>> expertReportTypePredicate
    ) {
        return Optional.ofNullable(caseData.getHearingFurtherEvidenceDocuments()).orElse(List.of()).stream()
            .flatMap(e -> e.getValue().getSupportingEvidenceBundle().stream())
            .filter(matchFurtherEvidenceType(furtherEvidenceType))
            .filter(expertReportTypePredicate)
            .filter(isNonConfidentialDocument())
            .map(toManagedDocumentElement())
            .collect(toList());
    }

    private static Map<String, Object> migrateFurtherEvidenceDocuments(CaseData caseData,
                                                                       FurtherEvidenceType furtherEvidenceType,
                                                                       String newFieldName) {
        final Predicate<Element<SupportingEvidenceBundle>> expertReportTypePredicate = expertReportTypePredicate(
            furtherEvidenceType, newFieldName);

        // uploaded by LA
        final List<Element<ManagedDocument>> newDocListLA = convertManagedDocumentFromFurtherEvidenceDocumentByLA(
            caseData, furtherEvidenceType, expertReportTypePredicate);

        // uploaded by admin
        final List<Element<ManagedDocument>> newDocListCTSC = convertManagedDocumentFromFurtherEvidenceDocumentByAdmin(
            caseData, furtherEvidenceType, expertReportTypePredicate);

        // all non-confidential documents
        final List<Element<ManagedDocument>> newDocList =
            convertManagedDocumentFromNonConfidentialFurtherEvidenceDocument(caseData, furtherEvidenceType,
                expertReportTypePredicate);

        // Relates to hearing
        newDocListLA.addAll(convertManagedDocumentFromHearingFurtherEvidenceDocumentByLA(caseData,
            furtherEvidenceType, expertReportTypePredicate));
        newDocListCTSC.addAll(convertManagedDocumentFromHearingFurtherEvidenceDocumentByAdmin(caseData,
            furtherEvidenceType, expertReportTypePredicate));
        newDocList.addAll(convertManagedDocumentFromNonConfidentialHearingFurtherEvidenceDocument(caseData,
            furtherEvidenceType, expertReportTypePredicate));

        Map<String, Object> ret = new HashMap<>();
        ret.put(newFieldName, newDocList);
        ret.put(newFieldName + "LA", newDocListLA);
        ret.put(newFieldName + "CTSC", newDocListCTSC);
        return ret;
    }

    // Validation methods

    public void doHasCFVMigratedCheck(long caseId, String hasBeenCFVMigrated, String migrationId) {
        doHasCFVMigratedCheck(caseId, hasBeenCFVMigrated, migrationId, false);
    }

    public void doHasCFVMigratedCheck(long caseId, String hasBeenCFVMigrated, String migrationId, boolean rollback)
        throws AssertionError {

        if (!rollback) {
            if (YesNo.YES.equals(YesNo.fromString(hasBeenCFVMigrated))) {
                throw new AssertionError(format(
                    "Migration {id = %s, case reference = %s}, case has already been migrated",
                    migrationId, caseId
                ));
            }
        } else {
            if (ObjectUtils.isEmpty(hasBeenCFVMigrated) || YesNo.NO.equals(YesNo.fromString(hasBeenCFVMigrated))) {
                throw new AssertionError(format(
                    "Migration {id = %s, case reference = %s}, case has not been migrated",
                    migrationId, caseId
                ));
            }
        }
    }

    // Position Statements (child)

    @SuppressWarnings("unchecked")
    public Map<String, Object> rollbackPositionStatementChild(CaseDetails caseDetails) {
        Map<String, Object>  ret = new HashMap<>();
        ret.put("posStmtChildListLA", List.of());
        ret.put("posStmtChildList", List.of());
        return ret;
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public Map<String, Object> migratePositionStatementChild(CaseData caseData) {
        List<Element<PositionStatementChild>> posStmtChildListLA = caseData.getHearingDocuments()
            .getPositionStatementChildListV2().stream()
            .filter(cs -> YesNo.YES.getValue().equals(cs.getValue().getHasConfidentialAddress()))
            .collect(toList());

        List<Element<PositionStatementChild>> posStmtChildList = caseData.getHearingDocuments()
            .getPositionStatementChildListV2().stream()
            .filter(cs -> YesNo.NO.getValue().equals(Optional.ofNullable(cs.getValue().getHasConfidentialAddress())
                .orElse(YesNo.NO.getValue())))
            .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("posStmtChildListLA", posStmtChildListLA);
        ret.put("posStmtChildList", posStmtChildList);
        return ret;
    }

    // Position Statements (Respondent)

    @SuppressWarnings("unchecked")
    public Map<String, Object> rollbackPositionStatementRespondent(CaseDetails caseDetails) {
        Map<String, Object>  ret = new HashMap<>();
        ret.put("posStmtRespListLA", List.of());
        ret.put("posStmtRespList", List.of());
        return ret;
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public Map<String, Object> migratePositionStatementRespondent(CaseData caseData) {
        List<Element<PositionStatementRespondent>> posStmtRespListLA = caseData.getHearingDocuments()
            .getPositionStatementRespondentListV2().stream()
            .filter(cs -> YesNo.YES.getValue().equals(cs.getValue().getHasConfidentialAddress()))
            .collect(toList());

        List<Element<PositionStatementRespondent>> posStmtRespList = caseData.getHearingDocuments()
            .getPositionStatementRespondentListV2().stream()
            .filter(cs -> YesNo.NO.getValue().equals(Optional.ofNullable(cs.getValue().getHasConfidentialAddress())
                .orElse(YesNo.NO.getValue())))
            .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("posStmtRespListLA", posStmtRespListLA);
        ret.put("posStmtRespList", posStmtRespList);
        return ret;
    }

    // Notice of Acting / Issue

    public Map<String, Object> rollbackNoticeOfActingOrIssue() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("noticeOfActingOrIssueList", List.of());
        ret.put("noticeOfActingOrIssueListLA", List.of());
        ret.put("noticeOfActingOrIssueListCTSC", List.of());
        return ret;
    }

    public Map<String, Object> migrateNoticeOfActingOrIssue(CaseData caseData) {
        FurtherEvidenceType furtherEvidenceType = NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE;
        return migrateFurtherEvidenceDocuments(caseData, furtherEvidenceType, "noticeOfActingOrIssueList");
    }

    // Guardian Reports

    public Map<String, Object> rollbackGuardianReports() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("guardianEvidenceList", List.of());
        ret.put("guardianEvidenceListLA", List.of());
        ret.put("guardianEvidenceListCTSC", List.of());
        return ret;
    }

    public Map<String, Object> migrateGuardianReports(CaseData caseData) {
        FurtherEvidenceType furtherEvidenceType = GUARDIAN_REPORTS;
        return migrateFurtherEvidenceDocuments(caseData, furtherEvidenceType, "guardianEvidenceList");
    }

    public Map<String, Object> migrateArchivedDocuments(CaseData caseData) {
        return migrateFurtherEvidenceDocuments(caseData, null, "archivedDocumentsList");
    }

    public Map<String, Object> rollbackArchivedDocumentsList() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("archivedDocumentsList", List.of());
        ret.put("archivedDocumentsListLA", List.of());
        ret.put("archivedDocumentsListCTSC", List.of());
        return ret;
    }

    // Expert Reports

    public Map<String, Object> rollbackExpertReports() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("drugAndAlcoholReportList", List.of());
        ret.put("drugAndAlcoholReportListLA", List.of());
        ret.put("drugAndAlcoholReportListCTSC", List.of());
        ret.put("lettersOfInstructionList", List.of());
        ret.put("lettersOfInstructionListLA", List.of());
        ret.put("lettersOfInstructionListCTSC", List.of());
        ret.put("expertReportList", List.of());
        ret.put("expertReportListLA", List.of());
        ret.put("expertReportListCTSC", List.of());
        return ret;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> migrateExpertReports(CaseData caseData) {
        Map<String, Object> ret = new HashMap<>();

        Map<String, Object> mergedMap = new HashMap<>(migrateFurtherEvidenceDocuments(caseData, OTHER_REPORTS,
            "expertReportList"));
        migrateFurtherEvidenceDocuments(caseData, EXPERT_REPORTS, "expertReportList").forEach((key, value) ->
            mergedMap.merge(key, value, (v1, v2) -> {
                Collection<Element<ManagedDocument>> mergedCollection = new ArrayList<>();
                mergedCollection.addAll((Collection<Element<ManagedDocument>>) v1);
                mergedCollection.addAll((Collection<Element<ManagedDocument>>) v2);
                return mergedCollection;
            }));

        ret.putAll(mergedMap);
        ret.putAll(migrateFurtherEvidenceDocuments(caseData, EXPERT_REPORTS, "drugAndAlcoholReportList"));
        return ret;
    }

    // Applicant's witness statements
    public Map<String, Object> rollbackApplicantWitnessStatements() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("applicantWitnessStmtList", List.of());
        ret.put("applicantWitnessStmtListLA", List.of());
        ret.put("applicantWitnessStmtListCTSC", List.of());
        return ret;
    }

    public Map<String, Object> migrateApplicantWitnessStatements(CaseData caseData) {
        FurtherEvidenceType furtherEvidenceType = APPLICANT_STATEMENT;
        return migrateFurtherEvidenceDocuments(caseData, furtherEvidenceType, "applicantWitnessStmtList");
    }

    /// Respondent Statements
    public Map<String, Object> rollbackRespondentStatement() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("respStmtList", List.of());
        ret.put("respStmtListLA", List.of());
        ret.put("respStmtListCTSC", List.of());
        return ret;
    }

    public Map<String, Object> migrateRespondentStatement(CaseData caseData) {
        List<Element<RespondentStatementV2>> respStmtList =
            caseData.getRespondentStatements().stream()
                .flatMap(rs -> {
                    UUID respondentId = rs.getValue().getRespondentId();
                    String respondentName = rs.getValue().getRespondentName();
                    return rs.getValue().getSupportingEvidenceNC().stream().map(
                        seb -> toRespondentStatementV2(respondentId, respondentName, seb)
                    );
                })
                .collect(toList());
        List<Element<RespondentStatementV2>> respStmtListLA =
            caseData.getRespondentStatements().stream()
                .flatMap(rs -> {
                    UUID respondentId = rs.getValue().getRespondentId();
                    String respondentName = rs.getValue().getRespondentName();
                    return rs.getValue().getSupportingEvidenceLA().stream().map(
                        seb -> toRespondentStatementV2(respondentId, respondentName, seb)
                    );
                })
                .filter(seb -> !respStmtList.contains(seb))
                .collect(toList());
        List<Element<RespondentStatementV2>> respStmtListCTSC =
            caseData.getRespondentStatements().stream()
                .flatMap(rs -> {
                    UUID respondentId = rs.getValue().getRespondentId();
                    String respondentName = rs.getValue().getRespondentName();
                    return rs.getValue().getSupportingEvidenceBundle().stream().map(
                        seb -> toRespondentStatementV2(respondentId, respondentName, seb)
                    );
                })
                .filter(seb -> !respStmtList.contains(seb) && !respStmtListLA.contains(seb))
                .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("respStmtList", respStmtList);
        ret.put("respStmtListLA", respStmtListLA);
        ret.put("respStmtListCTSC", respStmtListCTSC);
        return ret;
    }

    // Skeleton Arguments

    @SuppressWarnings("unchecked")
    public Map<String, Object> rollbackSkeletonArgumentList(CaseDetails caseDetails) {
        Map<String, Object> caseDataMap = caseDetails.getData();

        List<Element<SkeletonArgument>> skeletonArgumentList = new ArrayList<>();
        if (caseDataMap.get("skeletonArgumentList") != null) {
            skeletonArgumentList.addAll((List) caseDataMap.get("skeletonArgumentList"));
        }
        if (caseDataMap.get("skeletonArgumentListLA") != null) {
            skeletonArgumentList.addAll((List) caseDataMap.get("skeletonArgumentListLA"));
        }

        Map<String, Object> ret = new HashMap<>();
        ret.put("skeletonArgumentList", caseDataMap.get("skeletonArgumentListBackup"));
        ret.put("skeletonArgumentListBackup", List.of());
        ret.put("skeletonArgumentListLA", List.of());
        return ret;
    }

    public Map<String, Object> migrateSkeletonArgumentList(CaseData caseData) {
        List<Element<SkeletonArgument>> skeletonArgumentList =
            caseData.getHearingDocuments().getSkeletonArgumentList().stream()
                .filter(skeletonArgument ->
                    YesNo.NO.getValue().equals(skeletonArgument.getValue().getHasConfidentialAddress()))
                .collect(toList());

        List<Element<SkeletonArgument>> skeletonArgumentListLA =
            caseData.getHearingDocuments().getSkeletonArgumentList().stream()
                .filter(skeletonArgument ->
                    YesNo.YES.getValue().equals(skeletonArgument.getValue().getHasConfidentialAddress()))
                .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("skeletonArgumentListBackup", caseData.getHearingDocuments().getSkeletonArgumentList());
        ret.put("skeletonArgumentList", skeletonArgumentList);
        ret.put("skeletonArgumentListLA", skeletonArgumentListLA);
        return ret;
    }

    // Application Document

    public Map<String, Object> rollbackApplicationDocuments() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("thresholdList", List.of());
        ret.put("thresholdListLA", List.of());
        ret.put("documentsFiledOnIssueList", List.of());
        ret.put("documentsFiledOnIssueListLA", List.of());
        ret.put("carePlanList", List.of());
        ret.put("carePlanListLA", List.of());
        return ret;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> migrateApplicationDocuments(CaseData caseData) {
        return applicationDocumentsService.synchroniseToNewFields(caseData.getApplicationDocuments());
    }

    // Correspondence

    public Map<String, Object> rollbackCorrespondenceDocuments() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("correspondenceDocList", List.of());
        ret.put("correspondenceDocListLA", List.of());
        ret.put("correspondenceDocListCTSC", List.of());
        return ret;
    }

    public Map<String, Object> migrateCorrespondenceDocuments(CaseData caseData) {
        List<Element<ManagedDocument>> correspondenceDocList =
            Stream.of(Optional.ofNullable(caseData.getCorrespondenceDocuments()),
                    Optional.ofNullable(caseData.getCorrespondenceDocumentsLA()),
                    Optional.ofNullable(caseData.getCorrespondenceDocumentsSolicitor()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(Collection::stream)
                .filter(bundleElement -> !bundleElement.getValue().isConfidentialDocument())
                .map(bundleElement ->
                    element(bundleElement.getId(),
                        ManagedDocument.builder().document(bundleElement.getValue().getDocument()).build()))
                .toList();

        List<Element<ManagedDocument>> correspondenceDocListLA =
            Stream.of(Optional.ofNullable(caseData.getCorrespondenceDocumentsLA()),
                    Optional.ofNullable(caseData.getCorrespondenceDocumentsSolicitor()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(Collection::stream)
                .filter(bundleElement -> bundleElement.getValue().isConfidentialDocument())
                .map(bundleElement ->
                    element(bundleElement.getId(),
                        ManagedDocument.builder().document(bundleElement.getValue().getDocument()).build()))
                .toList();

        List<Element<ManagedDocument>> correspondenceDocListCTSC =
            Optional.ofNullable(caseData.getCorrespondenceDocuments()).orElse(List.of()).stream()
                .filter(bundleElement -> bundleElement.getValue().isConfidentialDocument())
                .map(bundleElement ->
                    element(bundleElement.getId(),
                        ManagedDocument.builder().document(bundleElement.getValue().getDocument()).build()))
                .toList();

        Map<String, Object> ret = new HashMap<>();
        ret.put("correspondenceDocList", correspondenceDocList);
        ret.put("correspondenceDocListLA", correspondenceDocListLA);
        ret.put("correspondenceDocListCTSC", correspondenceDocListCTSC);
        return ret;
    }

    // Case Summary

    @SuppressWarnings("unchecked")
    public Map<String, Object> rollbackCaseSummaryMigration(CaseDetails caseDetails) {
        Map<String, Object> caseDataMap = caseDetails.getData();
        Map<String, Object> ret = new HashMap<>();
        ret.put("caseSummaryList", caseDataMap.get("caseSummaryListBackup"));
        ret.put("caseSummaryListLA", List.of());
        ret.put("caseSummaryListBackup", List.of());
        return ret;
    }

    public Map<String, Object> moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(CaseData caseData) {
        List<Element<CaseSummary>> caseSummaryListLA = caseData.getHearingDocuments().getCaseSummaryList().stream()
            .filter(cs -> YesNo.YES.getValue().equals(cs.getValue().getHasConfidentialAddress()))
            .collect(toList());

        List<Element<CaseSummary>> newCaseSummaryList = caseData.getHearingDocuments().getCaseSummaryList().stream()
            .filter(cs -> YesNo.NO.getValue().equals(Optional.ofNullable(cs.getValue().getHasConfidentialAddress())
                .orElse(YesNo.NO.getValue())))
            .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("caseSummaryListBackup", caseData.getHearingDocuments().getCaseSummaryList());
        ret.put("caseSummaryListLA", caseSummaryListLA);
        ret.put("caseSummaryList", newCaseSummaryList);
        return ret;
    }

    // Court Bundle

    @SuppressWarnings("unchecked")
    public Map<String, Object> rollbackCourtBundleMigration(CaseDetails caseDetails) {
        Map<String, Object> caseDataMap = caseDetails.getData();
        Map<String, Object> ret = new HashMap<>();
        ret.put("courtBundleListV2", caseDataMap.get("courtBundleListV2Backup"));
        ret.put("courtBundleListLA", List.of());
        ret.put("courtBundleListCTSC", List.of());
        ret.put("courtBundleListV2Backup", List.of());
        return ret;
    }

    public Map<String, Object> migrateCourtBundle(CaseData caseData) {
        List<Element<HearingCourtBundle>> newHearingCourtBundleList = new ArrayList<>();
        List<Element<HearingCourtBundle>> hearingCourtBundleListLA = new ArrayList<>();
        List<Element<HearingCourtBundle>> hearingCourtBundleListCTSC = new ArrayList<>();

        List<Element<HearingCourtBundle>> hearingCourtBundles = caseData.getHearingDocuments().getCourtBundleListV2();

        for (Element<HearingCourtBundle> hearingCourtBundle : hearingCourtBundles) {
            List<Element<CourtBundle>> newCourtBundleList = hearingCourtBundle.getValue().getCourtBundle().stream()
                .filter(cs -> !cs.getValue().isConfidentialDocument())
                .collect(toList());

            List<Element<CourtBundle>> courtBundleListLA = hearingCourtBundle.getValue().getCourtBundle().stream()
                .filter(cs -> !cs.getValue().isUploadedByHMCTS() && cs.getValue().isConfidentialDocument())
                .collect(toList());

            List<Element<CourtBundle>> courtBundleListCTSC = hearingCourtBundle.getValue().getCourtBundle().stream()
                .filter(cs -> cs.getValue().isUploadedByHMCTS() && cs.getValue().isConfidentialDocument())
                .collect(toList());

            if (!newCourtBundleList.isEmpty()) {
                Element<HearingCourtBundle> bundle = element(hearingCourtBundle.getId(),
                    HearingCourtBundle.builder().build());
                bundle.getValue().setHearing(hearingCourtBundle.getValue().getHearing());
                bundle.getValue().setCourtBundle(newCourtBundleList);
                newHearingCourtBundleList.add(bundle);
            }

            if (!courtBundleListLA.isEmpty()) {
                Element<HearingCourtBundle> bundle = element(hearingCourtBundle.getId(),
                    HearingCourtBundle.builder().build());
                bundle.getValue().setHearing(hearingCourtBundle.getValue().getHearing());
                bundle.getValue().setCourtBundle(courtBundleListLA);
                hearingCourtBundleListLA.add(bundle);
            }

            if (!courtBundleListCTSC.isEmpty()) {
                Element<HearingCourtBundle> bundle = element(hearingCourtBundle.getId(),
                    HearingCourtBundle.builder().build());
                bundle.getValue().setHearing(hearingCourtBundle.getValue().getHearing());
                bundle.getValue().setCourtBundle(courtBundleListCTSC);
                hearingCourtBundleListCTSC.add(bundle);
            }
        }

        Map<String, Object> ret = new HashMap<>();
        ret.put("courtBundleListV2Backup", hearingCourtBundles);
        ret.put("courtBundleListV2", newHearingCourtBundleList);
        ret.put("courtBundleListLA", hearingCourtBundleListLA);
        ret.put("courtBundleListCTSC", hearingCourtBundleListCTSC);
        return ret;
    }

    private List<List<Element<SupportingEvidenceBundle>>> getHearingFurtherEvidenceDocumentsToBeMigrated(
        CaseData caseData) {
        return Optional.ofNullable(caseData.getHearingFurtherEvidenceDocuments()).orElse(List.of()).stream()
            .map(hfed -> hfed.getValue().getSupportingEvidenceBundle()).toList();
    }

    public int calculateExpectedNumberOfHearingFurtherEvidenceDocuments(CaseData caseData) {
        return getHearingFurtherEvidenceDocumentsToBeMigrated(caseData).stream()
            .mapToInt(Collection::size)
            .sum();
    }

    public Map<String, Object> migrateHearingFurtherEvidenceDocumentsToArchivedDocuments(CaseData caseData) {
        List<Element<ManagedDocument>> ret = getHearingFurtherEvidenceDocumentsToBeMigrated(caseData).stream()
            .flatMap(c -> c.stream().map(toManagedDocumentElement()))
            .collect(toList());
        return Map.of("archivedDocumentsListCTSC", ret);
    }

    private List<List<Element<SupportingEvidenceBundle>>> getFurtherEvidenceDocumentsToBeMigrated(
        CaseData caseData) {
        return List.of(Optional.ofNullable(caseData.getFurtherEvidenceDocuments()).orElse(List.of()),
            Optional.ofNullable(caseData.getFurtherEvidenceDocumentsLA()).orElse(List.of()),
            Optional.ofNullable(caseData.getFurtherEvidenceDocumentsSolicitor()).orElse(List.of()));
    }

    public int calculateExpectedNumberOfFurtherEvidenceDocuments(CaseData caseData) {
        return getFurtherEvidenceDocumentsToBeMigrated(caseData).stream().mapToInt(Collection::size).sum();
    }

    public Map<String, Object> migrateFurtherEvidenceDocumentsToArchivedDocuments(CaseData caseData) {
        List<Element<ManagedDocument>> ret = getFurtherEvidenceDocumentsToBeMigrated(caseData).stream()
            .flatMap(c -> c.stream().map(toManagedDocumentElement()))
            .collect(toList());
        return Map.of("archivedDocumentsListCTSC", ret);
    }

    public void validateMigratedFurtherEvidenceDocument(String migrationId, CaseData caseData,
                                                        Map<String, Object> changes) {
        int expectedSize = calculateExpectedNumberOfHearingFurtherEvidenceDocuments(caseData)
            + calculateExpectedNumberOfFurtherEvidenceDocuments(caseData);
        int actualSize = List.of(
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
            ).stream()
            .map(key -> (Collection) changes.get(key))
            .filter(collection -> collection != null)
            .mapToInt(Collection::size).sum();
        if (expectedSize != actualSize) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "FurtherEvidenceDocument/HearingFurtherEvidenceDocument (%s/%s)",
                migrationId, caseData.getId(), expectedSize, actualSize));
        }
    }

    // SELECT *
    // FROM (SELECT reference,
    //             COALESCE(SUM(jsonb_array_length(ncElements -> 'value' -> 'supportingEvidenceBundle')), 0) +
    //             COALESCE(jsonb_array_length(data -> 'furtherEvidenceDocumentsLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'furtherEvidenceDocuments'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'furtherEvidenceDocumentsSolicitor'), 0)
    //                 AS oldCount,
    //             COALESCE(jsonb_array_length(data -> 'noticeOfActingOrIssueList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'noticeOfActingOrIssueListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'noticeOfActingOrIssueListCTSC'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'guardianEvidenceList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'guardianEvidenceListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'guardianEvidenceListCTSC'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'applicantWitnessStmtList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'applicantWitnessStmtListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'applicantWitnessStmtListCTSC'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'expertReportList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'expertReportListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'expertReportListCTSC'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'drugAndAlcoholReportList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'drugAndAlcoholReportListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'drugAndAlcoholReportListCTSC'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'archivedDocumentsList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'archivedDocumentsListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'archivedDocumentsListCTSC'), 0)
    //                 AS newCount
    //      FROM case_data cd
    //               LEFT JOIN LATERAL jsonb_array_elements(data -> 'hearingFurtherEvidenceDocuments')
    //               AS ncElements ON TRUE
    //      where jurisdiction = 'PUBLICLAW'
    //        and case_type_id = 'CARE_SUPERVISION_EPO'
    //        and (data -> 'hasBeenCFVMigrated')::text = '"YES"'
    //      GROUP BY reference, data) T
    // WHERE oldCount <> newCount

    private List<Element<CaseSummary>> getCaseSummariesToBeMigrated(CaseData caseData) {
        return Optional.ofNullable(caseData.getHearingDocuments())
            .orElse(HearingDocuments.builder().caseSummaryList(List.of()).build())
            .getCaseSummaryList();
    }

    public Map<String, Object> migrateCaseSummaryToArchivedDocuments(CaseData caseData) {
        List<Element<ManagedDocument>> ret = getCaseSummariesToBeMigrated(caseData).stream()
            .map(caseSummaryToManagedDocumentElement()).collect(toList());
        return Map.of("archivedDocumentsListCTSC", ret);
    }

    public void validateMigratedCaseSummary(String migrationId, CaseData caseData, Map<String, Object> changes) {
        int expectedSize = getCaseSummariesToBeMigrated(caseData).size();
        int actualSize = List.of("caseSummaryList", "caseSummaryListLA").stream()
            .map(key -> (Collection) changes.get(key))
            .filter(collection -> collection != null)
            .mapToInt(Collection::size).sum();
        if (expectedSize != actualSize) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "CaseSummary (%s/%s)",
                migrationId, caseData.getId(), expectedSize, actualSize));
        }
    }

    // SELECT *
    // FROM (SELECT reference,
    //             COALESCE(jsonb_array_length(data -> 'caseSummaryListBackup'), 0) AS oldCount,
    //             COALESCE(jsonb_array_length(data -> 'caseSummaryListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'caseSummaryList'), 0) AS newCount
    //      FROM case_data cd
    //      where jurisdiction = 'PUBLICLAW'
    //        and case_type_id = 'CARE_SUPERVISION_EPO'
    //        and (data -> 'hasBeenCFVMigrated')::text = '"YES"') T
    // WHERE oldCount <> newCount

    private List<Element<PositionStatementRespondent>> getPositionStatementRespondentToBeMigrated(CaseData caseData) {
        return Optional.ofNullable(caseData.getHearingDocuments())
            .orElse(HearingDocuments.builder().positionStatementRespondentListV2(List.of()).build())
            .getPositionStatementRespondentListV2();
    }

    private List<Element<PositionStatementChild>> getPositionStatementChildToBeMigrated(CaseData caseData) {
        return Optional.ofNullable(caseData.getHearingDocuments())
            .orElse(HearingDocuments.builder().positionStatementChildListV2(List.of()).build())
            .getPositionStatementChildListV2();
    }

    public Map<String, Object> migratePositionStatementToArchivedDocuments(CaseData caseData) {
        List<Element<ManagedDocument>> ret = getPositionStatementRespondentToBeMigrated(caseData).stream()
            .map(positionStatementRespondentToManagedDocumentElement()).collect(toList());
        ret.addAll(getPositionStatementChildToBeMigrated(caseData).stream()
            .map(positionStatementChildToManagedDocumentElement()).collect(toList()));
        return Map.of("archivedDocumentsListCTSC", ret);
    }

    public void validateMigratedPositionStatement(String migrationId, CaseData caseData, Map<String, Object> changes) {
        int expectedSize = getPositionStatementRespondentToBeMigrated(caseData).size()
            + getPositionStatementChildToBeMigrated(caseData).size();
        int actualSize = List.of("posStmtRespList", "posStmtRespListLA", "posStmtRespListCTSC",
                "posStmtChildList", "posStmtChildListLA", "posStmtChildListCTSC").stream()
            .map(key -> (Collection) changes.get(key))
            .filter(collection -> collection != null)
            .mapToInt(Collection::size).sum();
        if (expectedSize != actualSize) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Unexpected number of migrated "
                    + "PositionStatement(Child/Respondent) (%s/%s)",
                migrationId, caseData.getId(), expectedSize, actualSize));
        }
    }

    // SELECT *
    // FROM (SELECT reference,
    //             COALESCE(jsonb_array_length(data -> 'positionStatementRespondentListV2'), 0)   AS oldCount,
    //             COALESCE(jsonb_array_length(data -> 'posStmtRespList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'posStmtRespListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'posStmtRespListCTSC'), 0) AS newCount
    //      FROM case_data cd
    //      where jurisdiction = 'PUBLICLAW'
    //        and case_type_id = 'CARE_SUPERVISION_EPO'
    //        and (data -> 'hasBeenCFVMigrated')::text = '"YES"') T
    // WHERE oldCount <> newCount;
    //
    // SELECT *
    // FROM (SELECT reference,
    //             COALESCE(jsonb_array_length(data -> 'positionStatementChildListV2'), 0)   AS oldCount,
    //             COALESCE(jsonb_array_length(data -> 'posStmtChildList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'posStmtChildListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'posStmtChildListCTSC'), 0) AS newCount
    //      FROM case_data cd
    //      where jurisdiction = 'PUBLICLAW'
    //        and case_type_id = 'CARE_SUPERVISION_EPO'
    //        and (data -> 'hasBeenCFVMigrated')::text = '"YES"') T
    // WHERE oldCount <> newCount;

    private List<List<Element<SupportingEvidenceBundle>>> getRespondentStatementsToBeMigrated(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondentStatements()).orElse(List.of()).stream()
            .map(hfed -> hfed.getValue().getSupportingEvidenceBundle())
            .toList();
    }

    public Map<String, Object> migrateRespondentStatementToArchivedDocuments(CaseData caseData) {
        List<Element<ManagedDocument>> ret = getRespondentStatementsToBeMigrated(caseData).stream()
            .flatMap(c -> c.stream().map(toManagedDocumentElement()))
            .collect(toList());
        return Map.of("archivedDocumentsListCTSC", ret);
    }

    public void validateMigratedRespondentStatement(String migrationId, CaseData caseData,
                                                    Map<String, Object> changes) {
        int expectedSize = getRespondentStatementsToBeMigrated(caseData).stream()
            .mapToInt(Collection::size)
            .sum();
        int actualSize = List.of("respStmtList", "respStmtListLA", "respStmtListCTSC").stream()
            .map(key -> (Collection) changes.get(key))
            .filter(collection -> collection != null)
            .mapToInt(Collection::size).sum();
        if (expectedSize != actualSize) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Unexpected number of migrated respondent statements (%s/%s)",
                migrationId, caseData.getId(), expectedSize, actualSize));
        }
    }

    // SELECT *
    // FROM (SELECT reference,
    //            COALESCE(SUM(jsonb_array_length(ncElements -> 'value' -> 'supportingEvidenceBundle')), 0) as oldCount,
    //            COALESCE(jsonb_array_length(data -> 'respStmtList'), 0) +
    //            COALESCE(jsonb_array_length(data -> 'respStmtListLA'), 0) +
    //            COALESCE(jsonb_array_length(data -> 'respStmtListCTSC'), 0)                               AS newCount
    //      FROM case_data cd
    //               LEFT JOIN LATERAL jsonb_array_elements(data -> 'respondentStatements') AS ncElements ON TRUE
    //      where jurisdiction = 'PUBLICLAW'
    //        and case_type_id = 'CARE_SUPERVISION_EPO'
    //        and (data -> 'hasBeenCFVMigrated')::text = '"YES"'
    //      GROUP BY reference, data) T
    // WHERE oldCount <> newCount

    @SuppressWarnings("unchecked")
    private List<Element<SupportingEvidenceBundle>> getCorrespondenceDocumentsToBeMigrated(CaseData caseData) {
        List<Element<SupportingEvidenceBundle>> all = new ArrayList(Optional.ofNullable(caseData
            .getCorrespondenceDocuments()).orElse(List.of()));
        all.addAll(Optional.ofNullable(caseData.getCorrespondenceDocumentsLA()).orElse(List.of()));
        all.addAll(Optional.ofNullable(caseData.getCorrespondenceDocumentsSolicitor()).orElse(List.of()));
        return all;
    }

    public Map<String, Object> migrateCorrespondenceDocumentsToArchivedDocuments(CaseData caseData) {
        List<Element<ManagedDocument>> ret = getCorrespondenceDocumentsToBeMigrated(caseData).stream()
            .map(toManagedDocumentElement())
            .collect(toList());

        return Map.of("archivedDocumentsListCTSC", ret);
    }

    public void validateMigratedCorrespondenceDocuments(String migrationId, CaseData caseData,
                                                        Map<String, Object> changes) {
        int expectedSize = getCorrespondenceDocumentsToBeMigrated(caseData).size();
        int actualSize = List.of("correspondenceDocList", "correspondenceDocListLA", "correspondenceDocListCTSC")
            .stream()
            .map(key -> (Collection) changes.get(key))
            .filter(collection -> collection != null)
            .mapToInt(Collection::size).sum();
        if (expectedSize != actualSize) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Unexpected number of migrated correspondence documents"
                    + " (%s/%s)", migrationId, caseData.getId(), expectedSize, actualSize));
        }
    }

    // SELECT *
    // FROM (SELECT reference,
    //             COALESCE(jsonb_array_length(data -> 'correspondenceDocuments'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'correspondenceDocumentsSolicitor'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'correspondenceDocumentsLA'), 0) AS oldCount,
    //             COALESCE(jsonb_array_length(data -> 'correspondenceDocList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'correspondenceDocListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'correspondenceDocListCTSC'), 0) AS newCount
    //      FROM case_data cd
    //      where jurisdiction = 'PUBLICLAW'
    //        and case_type_id = 'CARE_SUPERVISION_EPO'
    //        and (data -> 'hasBeenCFVMigrated')::text = '"YES"'
    //     ) T
    // WHERE oldCount <> newCount

    @SuppressWarnings("unchecked")
    private List<Element<ApplicationDocument>> getApplicationDocumentsToBeMigrated(CaseData caseData) {
        return Optional.ofNullable(caseData.getApplicationDocuments()).orElse(List.of());
    }

    public Map<String, Object> migrateApplicationDocumentsToArchivedDocuments(CaseData caseData) {
        List<Element<ManagedDocument>> ret = getApplicationDocumentsToBeMigrated(caseData).stream()
            .map(applicationDocumentToManagedDocumentElement())
            .collect(toList());
        return Map.of("archivedDocumentsListCTSC", ret);
    }

    public void validateMigratedApplicationDocuments(String migrationId, CaseData caseData,
                                                        Map<String, Object> changes) {
        int expectedSize = getApplicationDocumentsToBeMigrated(caseData).size();
        int actualSize = List.of("documentsFiledOnIssueList", "documentsFiledOnIssueListLA",
                "documentsFiledOnIssueListCTSC",
                "carePlanList", "carePlanListLA", "carePlanListCTSC",
                "thresholdList", "thresholdListLA", "thresholdListCTSC")
            .stream()
            .map(key -> (Collection) changes.get(key))
            .filter(collection -> collection != null)
            .mapToInt(Collection::size).sum();
        if (expectedSize != actualSize) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Unexpected number of migrated application documents"
                    + " (%s/%s)", migrationId, caseData.getId(), expectedSize, actualSize));
        }
    }

    // SELECT *
    // FROM (SELECT reference,
    //             jsonb_array_length(data -> 'applicationDocuments')           AS oldCount,
    //             COALESCE(jsonb_array_length(data -> 'documentsFiledOnIssueList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'documentsFiledOnIssueListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'carePlanList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'carePlanListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'carePlanListCTSC'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'thresholdList'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'thresholdListLA'), 0) +
    //             COALESCE(jsonb_array_length(data -> 'thresholdListCTSC'), 0) AS newCount
    //      FROM case_data cd
    //      where jurisdiction = 'PUBLICLAW'
    //        and case_type_id = 'CARE_SUPERVISION_EPO'
    //        and (data -> 'hasBeenCFVMigrated')::text = '"YES"') T
    // WHERE oldCount <> newCount;

    @SuppressWarnings("unchecked")
    private static int sumCourtBundleSizes(Map<String, Object> map, String[] keys) {
        int total = 0;

        for (String key : keys) {
            List<Element<HearingCourtBundle>> hearingCourtBundles = (List<Element<HearingCourtBundle>>) map.get(key);

            if (hearingCourtBundles != null) {
                for (Element<HearingCourtBundle> hearingCourtBundle : hearingCourtBundles) {
                    total += hearingCourtBundle.getValue().getCourtBundle().size();
                }
            }
        }

        return total;
    }

    private List<List<Element<CourtBundle>>> getCourtBundlesToBeMigrated(CaseData caseData) {
        return Optional.ofNullable(caseData.getHearingDocuments())
            .orElse(HearingDocuments.builder().courtBundleListV2(List.of()).build())
            .getCourtBundleListV2()
            .stream().map(a -> a.getValue().getCourtBundle()).toList();
    }

    public Map<String, Object> migrateCourtBundlesToArchivedDocuments(CaseData caseData) {
        List<Element<ManagedDocument>> ret = getCourtBundlesToBeMigrated(caseData).stream()
            .flatMap(c -> c.stream().map(courtBundleToManagedDocumentElement()))
            .collect(toList());

        return Map.of("archivedDocumentsListCTSC", ret);
    }

    public void validateMigratedCourtBundle(String migrationId, CaseData caseData,
                                            Map<String, Object> changes) {
        int expectedSize = getCourtBundlesToBeMigrated(caseData).stream()
            .mapToInt(Collection::size).sum();

        String[] keysToSum = {"courtBundleListV2", "courtBundleListLA", "courtBundleListCTSC"};
        int actualSize = sumCourtBundleSizes(changes, keysToSum);
        if (expectedSize != actualSize) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Unexpected number of migrated court bundles"
                    + " (%s/%s)", migrationId, caseData.getId(), expectedSize, actualSize));
        }
    }

    // SELECT *
    // FROM (SELECT reference,
    //          COALESCE(SUM(jsonb_array_length(backupElements -> 'value' -> 'courtBundle')), 0) AS oldCourtBundleCount,
    //          COALESCE(SUM(jsonb_array_length(ncElements -> 'value' -> 'courtBundle')), 0) +
    //          COALESCE(SUM(jsonb_array_length(laElements -> 'value' -> 'courtBundle')), 0) +
    //          COALESCE(SUM(jsonb_array_length(ctscElements -> 'value' -> 'courtBundle')), 0)
    //                                                                              AS migratedCourtBundleCount
    //      FROM case_data cd
    //               LEFT JOIN LATERAL jsonb_array_elements(data -> 'courtBundleListV2') AS ncElements ON TRUE
    //               LEFT JOIN LATERAL jsonb_array_elements(data -> 'courtBundleListLA') AS laElements ON TRUE
    //               LEFT JOIN LATERAL jsonb_array_elements(data -> 'courtBundleListCTSC') AS ctscElements ON TRUE
    //               LEFT JOIN LATERAL jsonb_array_elements(data -> 'courtBundleListV2Backup') AS backupElements
    //                         ON TRUE
    //      where jurisdiction = 'PUBLICLAW'
    //        and case_type_id = 'CARE_SUPERVISION_EPO'
    //        and (data -> 'hasBeenCFVMigrated')::text = '"YES"'
    //      GROUP BY reference
    //     ) T
    // WHERE oldCourtBundleCount <> migratedCourtBundleCount

    public void validateMigratedNumberOfDocuments(String migrationId, CaseData caseData, Map<String, Object> changes) {
        validateMigratedFurtherEvidenceDocument(migrationId, caseData, changes);
        validateMigratedCaseSummary(migrationId, caseData, changes);
        validateMigratedPositionStatement(migrationId, caseData, changes);
        validateMigratedRespondentStatement(migrationId, caseData, changes);
        validateMigratedCorrespondenceDocuments(migrationId, caseData, changes);
        validateMigratedApplicationDocuments(migrationId, caseData, changes);
        validateMigratedCourtBundle(migrationId, caseData, changes);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> migrateMissingApplicationDocuments(CaseData caseData,
                                                                  DocumentUploaderType defaultUploaderType,
                                                                  List<CaseRole> defaultUploaderCaseRoles) {
        List<Element<ApplicationDocument>> missingApplicationDocuments = new ArrayList<>();

        caseData.getApplicationDocuments().stream().forEach(ea -> {
            final UUID targetId = ea.getId();

            List<List<Element<ManagedDocument>>> listOfLists = new ArrayList<>();

            listOfLists.add(Optional.ofNullable(caseData.getCarePlanList()).orElse(List.of()));
            listOfLists.add(Optional.ofNullable(caseData.getCarePlanListLA()).orElse(List.of()));
            listOfLists.add(Optional.ofNullable(caseData.getCarePlanListCTSC()).orElse(List.of()));
            listOfLists.add(Optional.ofNullable(caseData.getCarePlanListRemoved()).orElse(List.of()));

            listOfLists.add(Optional.ofNullable(caseData.getThresholdList()).orElse(List.of()));
            listOfLists.add(Optional.ofNullable(caseData.getThresholdListLA()).orElse(List.of()));
            listOfLists.add(Optional.ofNullable(caseData.getThresholdListCTSC()).orElse(List.of()));
            listOfLists.add(Optional.ofNullable(caseData.getThresholdListRemoved()).orElse(List.of()));

            listOfLists.add(Optional.ofNullable(caseData.getDocumentsFiledOnIssueList()).orElse(List.of()));
            listOfLists.add(Optional.ofNullable(caseData.getDocumentsFiledOnIssueListLA()).orElse(List.of()));
            listOfLists.add(Optional.ofNullable(caseData.getDocumentsFiledOnIssueListCTSC()).orElse(List.of()));
            listOfLists.add(Optional.ofNullable(caseData.getDocumentsFiledOnIssueListRemoved()).orElse(List.of()));

            List<Element<ManagedDocument>> concatenatedList = listOfLists.stream()
                .flatMap(List::stream)
                .collect(toList());

            if (ElementUtils.findElement(targetId, concatenatedList).isEmpty()) {
                String filename = ea.getValue().getDocument().getFilename();
                // check filename as documents may be manually uploaded by Sara
                if (!concatenatedList.stream().map(e -> e.getValue().getDocument().getFilename()).collect(toList())
                    .contains(filename)) {
                    if (ea.getValue().getUploaderType() == null) {
                        ea.getValue().setUploaderType(defaultUploaderType);
                    }
                    if (ea.getValue().getUploaderCaseRoles() == null || ea.getValue()
                        .getUploaderCaseRoles().isEmpty()) {
                        ea.getValue().setUploaderCaseRoles(defaultUploaderCaseRoles);
                    }
                    missingApplicationDocuments.add(ea);
                }
            }
        });

        Map<String, Object> ret = new HashMap<>();
        Map<String, Object> result = applicationDocumentsService.synchroniseToNewFields(missingApplicationDocuments);
        result.keySet().stream().forEach(key -> {
            try {
                List<Element<ManagedDocument>> list = (List<Element<ManagedDocument>>) BeanUtils
                    .getPropertyDescriptor(CaseData.class, key).getReadMethod().invoke(caseData);
                if (list == null || list.isEmpty()) {
                    ret.put(key, result.get(key));
                } else {
                    list.addAll((List<Element<ManagedDocument>>) result.get(key));
                    ret.put(key, list);
                }
            } catch (Exception ex) {
                throw new AssertionError("Fail to retrieve property value from caseData: " + key);
            }
        });
        return ret;
    }

}
