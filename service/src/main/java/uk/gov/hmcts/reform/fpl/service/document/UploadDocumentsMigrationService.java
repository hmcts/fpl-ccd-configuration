package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CHECKLIST_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_CHRONOLOGY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.THRESHOLD;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.TO_FOLLOW;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadDocumentsMigrationService {

    private final UploadDocumentTransformer transformer;

    public Map<String, Object> transformFromOldCaseData(CaseData caseData) {

        List<Element<ApplicationDocument>> applicationDocuments = Lists.newArrayList();
        List<String> toFollowComments = Lists.newArrayList();
        Map<String, Object> data = new HashMap<>();

        ofNullable(caseData.getSocialWorkChronologyDocument())
            .ifPresent(addTransformedDocument(applicationDocuments, toFollowComments, SOCIAL_WORK_CHRONOLOGY));

        ofNullable(caseData.getSocialWorkStatementDocument())
            .ifPresent(addTransformedDocument(applicationDocuments, toFollowComments, SOCIAL_WORK_STATEMENT));

        ofNullable(caseData.getSocialWorkCarePlanDocument())
            .ifPresent(addTransformedDocument(applicationDocuments, toFollowComments, CARE_PLAN));

        ofNullable(caseData.getSocialWorkEvidenceTemplateDocument())
            .ifPresent(addTransformedDocument(applicationDocuments, toFollowComments, SWET));

        ofNullable(caseData.getSocialWorkAssessmentDocument())
            .ifPresent(addTransformedDocument(applicationDocuments, toFollowComments, SOCIAL_WORK_STATEMENT));

        ofNullable(caseData.getThresholdDocument())
            .ifPresent(addTransformedDocument(applicationDocuments, toFollowComments, THRESHOLD));

        ofNullable(caseData.getChecklistDocument())
            .ifPresent(addTransformedDocument(applicationDocuments, toFollowComments, CHECKLIST_DOCUMENT));

        ofNullable(caseData.getOtherSocialWorkDocuments())
            .ifPresent(
                otherDocumentElements -> applicationDocuments.addAll(transformer.convert(otherDocumentElements)));

        if (ofNullable(caseData.getCourtBundle()).isPresent()) {
            data.put("courtBundleList", List.of(element(caseData.getCourtBundle())));
        }

        data.put("applicationDocuments", applicationDocuments);
        data.put("applicationDocumentsToFollowReason", String.join(", ", toFollowComments));
        return data;
    }

    private Consumer<Document> addTransformedDocument(List<Element<ApplicationDocument>> applicationDocuments,
                                                      List<String> toFollowComments,
                                                      ApplicationDocumentType documentType) {
        return doc -> {
            if (!isNull(doc.getTypeOfDocument())) {
                applicationDocuments.add(transformer.convert(doc, documentType));
            }
            addToFollowComment(doc, documentType, toFollowComments);
        };
    }

    private void addToFollowComment(Document document,
                                    ApplicationDocumentType documentType,
                                    List<String> toFollowComments) {
        String documentStatus = document.getDocumentStatus();
        if (TO_FOLLOW.getLabel().equals(documentStatus)) {
            toFollowComments.add(String.format("%s to follow", documentType.getLabel()));
        }

    }

}
