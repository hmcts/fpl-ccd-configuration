package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.noticeofproceedings.NoticeOfProceedingsLanguageFactory;

import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final NoticeOfProceedingsTemplateDataGenerationService noticeOfProceedingsTemplateDataGenerationService;
    private final NoticeOfProceedingsLanguageFactory noticeOfProceedingsLanguageFactory;

    public Map<String, Object> initNoticeOfProceeding(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();

        caseData.getFirstHearing().ifPresent(hearingBooking -> listAndLabel.put("proceedingLabel",
            buildProceedingLabel(hearingBooking)));

        if (caseData.allocatedJudgeExists()) {
            String assignedJudgeLabel = buildAllocatedJudgeLabel(caseData.getAllocatedJudge());

            listAndLabel.put("noticeOfProceedings", ImmutableMap.of(
                "judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder()
                    .allocatedJudgeLabel(assignedJudgeLabel)
                    .build()
            ));
        }

        return listAndLabel;
    }

    public NoticeOfProceedings setNoticeOfProceedingJudge(CaseData caseData) {
        NoticeOfProceedings noticeOfProceedings = caseData.getNoticeOfProceedings();

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
            noticeOfProceedings.getJudgeAndLegalAdvisor(),
            caseData.getAllocatedJudge()
        );

        removeAllocatedJudgeProperties(judgeAndLegalAdvisor);
        noticeOfProceedings.setJudgeAndLegalAdvisor(judgeAndLegalAdvisor);

        return noticeOfProceedings;
    }

    public List<Element<DocumentBundle>> uploadNoticesOfProceedings(
        CaseData caseData, List<DocmosisTemplates> docmosisTemplatesTypes) {
        DocmosisNoticeOfProceeding templateData = noticeOfProceedingsTemplateDataGenerationService
            .getTemplateData(caseData);

        List<DocmosisDocument> noticeOfProceedingDocuments =
            buildNoticeOfProceedingDocuments(templateData, docmosisTemplatesTypes);

        List<Document> documentReferences = uploadDocuments(noticeOfProceedingDocuments);

        return createNoticeOfProceedingDocumentBundle(documentReferences, caseData);
    }

    public List<Element<DocumentBundle>> prepareNoticeOfProceedingBundle(
        List<Element<DocumentBundle>> updatedNoticeOfProceedings,
        List<Element<DocumentBundle>> noticeOfProceedingBundleBefore,
        List<DocmosisTemplates> templateTypes) {

        if (isNotEmpty(noticeOfProceedingBundleBefore)) {
            updatedNoticeOfProceedings.addAll(getRemovedDocumentBundles(noticeOfProceedingBundleBefore, templateTypes));
        }

        return updatedNoticeOfProceedings;
    }

    public List<Element<DocumentBundle>> getPreviousNoticeOfProceedings(CaseData caseDataBefore) {
        if (caseDataBefore == null || caseDataBefore.getNoticeOfProceedingsBundle() == null) {
            return List.of();
        }

        return caseDataBefore.getNoticeOfProceedingsBundle();
    }

    private List<DocmosisDocument> buildNoticeOfProceedingDocuments(DocmosisNoticeOfProceeding templateData,
                                                                    List<DocmosisTemplates> templateTypes) {
        return templateTypes.stream()
            .map(template -> docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, template))
            .collect(Collectors.toList());
    }

    private List<Document> uploadDocuments(List<DocmosisDocument> documents) {
        return documents.stream()
            .map(document -> uploadDocumentService.uploadPDF(document.getBytes(), document.getDocumentTitle()))
            .collect(Collectors.toList());
    }

    private List<Element<DocumentBundle>> createNoticeOfProceedingDocumentBundle(List<Document> uploadedDocuments,
                                                                                 CaseData caseData) {
        return uploadedDocuments.stream()
            .map(document -> Element.<DocumentBundle>builder()
                .id(UUID.randomUUID())
                .value(DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename(document.originalDocumentName)
                        .url(document.links.self.href)
                        .binaryUrl(document.links.binary.href)
                        .build())
                    .translationRequirements(noticeOfProceedingsLanguageFactory.calculate(caseData))
                    .build())
                .build())
            .collect(Collectors.toList());
    }

    private List<Element<DocumentBundle>> getRemovedDocumentBundles(
        List<Element<DocumentBundle>> noticeOfProceedingBundle, List<DocmosisTemplates> templateTypes) {
        List<String> templateTypeTitles = templateTypes.stream().map(DocmosisTemplates::getDocumentTitle)
            .collect(Collectors.toList());

        ImmutableList.Builder<Element<DocumentBundle>> removedDocumentBundles = ImmutableList.builder();

        noticeOfProceedingBundle.forEach(element -> {
            String filename = element.getValue().getDocument().getFilename();

            if (!templateTypeTitles.contains(filename)) {
                removedDocumentBundles.add(element);
            }
        });

        return removedDocumentBundles.build();
    }

    private String buildProceedingLabel(HearingBooking hearingBooking) {
        return String.format("The case management hearing will be on the %s.",
            formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG));
    }
}
