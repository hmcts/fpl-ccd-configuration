package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NoticeOfProceedingsService.class})
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    LookupTestConfig.class,
    HearingBookingService.class,
    HearingVenueLookUpService.class,
    CaseDataExtractionService.class,
    FixedTimeConfiguration.class,
    NoticeOfHearingGenerationService.class,
    HearingVenueLookUpService.class
})
class NoticeOfProceedingsServiceTest {

    @Autowired
    private NoticeOfProceedingsService noticeOfProceedingService;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Test
    void shouldRetrieveExistingC6AWhenC6ANotIncludedInTemplateList() {
        CaseData caseData = generateNoticeOfProceedingBundle(ImmutableList.of(C6A));
        List<DocmosisTemplates> templatesList = ImmutableList.of(C6);
        List<Element<DocumentBundle>> removedDocuments = noticeOfProceedingService
            .getRemovedDocumentBundles(caseData, templatesList);

        assertThat(removedDocuments).hasSize(1);

        DocumentReference documentReference = removedDocuments.get(0).getValue().getDocument();

        assertThat(documentReference.getFilename()).isEqualTo(C6A.getDocumentTitle());
    }

    @Test
    void shouldNotRetrieveExistingDocumentsAWhenTemplateListIncludeBothC6AndC6A() {
        List<DocmosisTemplates> templatesList = ImmutableList.of(C6, C6A);
        CaseData caseData = generateNoticeOfProceedingBundle(templatesList);
        List<Element<DocumentBundle>> removedDocuments = noticeOfProceedingService
            .getRemovedDocumentBundles(caseData, templatesList);

        assertThat(removedDocuments).isEmpty();
    }

    private CaseData generateNoticeOfProceedingBundle(List<DocmosisTemplates> templateTypes) {
        return CaseData.builder()
            .noticeOfProceedingsBundle(templateTypes.stream()
                .map(docmosisDocument -> Element.<DocumentBundle>builder()
                    .id(UUID.randomUUID())
                    .value(DocumentBundle.builder()
                        .document(DocumentReference.builder()
                            .filename(docmosisDocument.getDocumentTitle())
                            .build())
                        .build())
                    .build()).collect(Collectors.toList())).build();
    }
}
