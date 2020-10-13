package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;

    public List<Element<DocumentBundle>> prepapreNoticeOfProceedingsBundle(
        List<Document> documentReferences, List<DocmosisTemplates> templateTypes,
        List<Element<DocumentBundle>> noticeOfProceedingBundleBefore) {
        List<Element<DocumentBundle>> updatedNoticeOfProceedings
            = createNoticeOfProceedingsCaseData(documentReferences);

        if (isNotEmpty(noticeOfProceedingBundleBefore)) {
            updatedNoticeOfProceedings.addAll(getRemovedDocumentBundles(noticeOfProceedingBundleBefore, templateTypes));
        }

        return updatedNoticeOfProceedings;
    }

    public NoticeOfProceedings prepareNoticeOfProceedings(CaseData caseData) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
            caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge());

        removeAllocatedJudgeProperties(judgeAndLegalAdvisor);

        NoticeOfProceedings noticeOfProceedings = caseData.getNoticeOfProceedings();
        noticeOfProceedings = noticeOfProceedings.toBuilder().judgeAndLegalAdvisor(judgeAndLegalAdvisor).build();

        return noticeOfProceedings;
    }

    public List<DocmosisDocument> buildNoticeOfProceedingDocuments(DocmosisNoticeOfProceeding templateData,
                                                                   List<DocmosisTemplates> templateTypes) {
        return templateTypes.stream()
            .map(template -> docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, template))
            .collect(Collectors.toList());
    }

    public List<Document> uploadDocuments(List<DocmosisDocument> documents) {
        return documents.stream()
            .map(document -> uploadDocumentService.uploadPDF(document.getBytes(), document.getDocumentTitle()))
            .collect(Collectors.toList());
    }

    private List<Element<DocumentBundle>> getRemovedDocumentBundles(List<Element<DocumentBundle>> noticeOfProceedingBundle,
                                                                    List<DocmosisTemplates> templateTypes) {
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

    private List<Element<DocumentBundle>> createNoticeOfProceedingsCaseData(List<Document> uploadedDocuments) {
        return uploadedDocuments.stream()
            .map(document -> Element.<DocumentBundle>builder()
                .id(UUID.randomUUID())
                .value(DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename(document.originalDocumentName)
                        .url(document.links.self.href)
                        .binaryUrl(document.links.binary.href)
                        .build())
                    .build())
                .build())
            .collect(Collectors.toList());
    }
}
