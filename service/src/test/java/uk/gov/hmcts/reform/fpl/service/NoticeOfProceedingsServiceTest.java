package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;

@ExtendWith(SpringExtension.class)
class NoticeOfProceedingsServiceTest {

    private final NoticeOfProceedingsService service = new NoticeOfProceedingsService();

    @Test
    void shouldRetrieveExistingC6AWhenC6ANotIncludedInTemplateList() {
        CaseData caseData = generateNoticeOfProceedingBundle(ImmutableList.of(C6A));
        List<DocmosisTemplates> templatesList = ImmutableList.of(C6);
        List<Element<DocumentBundle>> removedDocuments = service.getRemovedDocumentBundles(caseData, templatesList);

        assertThat(removedDocuments).hasSize(1);

        DocumentReference documentReference = removedDocuments.get(0).getValue().getDocument();

        assertThat(documentReference.getFilename()).isEqualTo(C6A.getDocumentTitle() + ".pdf");
    }

    @Test
    void shouldNotRetrieveExistingDocumentsAWhenTemplateListIncludeBothC6AndC6A() {
        List<DocmosisTemplates> templatesList = ImmutableList.of(C6, C6A);
        CaseData caseData = generateNoticeOfProceedingBundle(templatesList);
        List<Element<DocumentBundle>> removedDocuments = service.getRemovedDocumentBundles(caseData, templatesList);

        assertThat(removedDocuments).hasSize(0);
    }

    private CaseData generateNoticeOfProceedingBundle(List<DocmosisTemplates> templateTypes) {
        return CaseData.builder()
            .noticeOfProceedingsBundle(templateTypes.stream()
                .map(docmosisDocument -> Element.<DocumentBundle>builder()
                    .id(UUID.randomUUID())
                    .value(DocumentBundle.builder()
                        .document(DocumentReference.builder()
                            .filename(docmosisDocument.getDocumentTitle() + ".pdf")
                            .build())
                        .build())
                    .build()).collect(Collectors.toList())).build();
    }
}
