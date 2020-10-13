package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.ProceedingType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES;

class NoticeOfProceedingsTest {
    @Test
    void shouldIncludeBothNoticeOfProceedingTemplatesWhenBothNoticeOfProceedingsSelected() {
        NoticeOfProceedings noticeOfProceedings = NoticeOfProceedings.builder()
            .proceedingTypes(buildProceedingTypes(
                NOTICE_OF_PROCEEDINGS_FOR_PARTIES, NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES
            ))
            .build();

        List<DocmosisTemplates> docmosisTemplates = noticeOfProceedings.mapProceedingTypesToDocmosisTemplate();

        assertThat(docmosisTemplates).isEqualTo(List.of(C6, C6A));
    }

    @Test
    void shouldIncludeC6DocmosisTemplateWhenOnlyC6NoticeTypeHasBeenSelected() {
        NoticeOfProceedings noticeOfProceedings = NoticeOfProceedings.builder()
            .proceedingTypes(buildProceedingTypes(NOTICE_OF_PROCEEDINGS_FOR_PARTIES))
            .build();

        List<DocmosisTemplates> docmosisTemplates = noticeOfProceedings.mapProceedingTypesToDocmosisTemplate();

        assertThat(docmosisTemplates).isEqualTo(List.of(C6));
    }

    @Test
    void shouldIncludeC6aDocmosisTemplateWhenOnlyC6ANoticeTypeHasBeenSelected() {
        NoticeOfProceedings noticeOfProceedings = NoticeOfProceedings.builder()
            .proceedingTypes(buildProceedingTypes(NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES))
            .build();

        List<DocmosisTemplates> docmosisTemplates = noticeOfProceedings.mapProceedingTypesToDocmosisTemplate();

        assertThat(docmosisTemplates).isEqualTo(List.of(C6A));
    }

    @Test
    void shouldReturnAnEmptyListWhenNoNoticeTypesHaveBeenSelected() {
        NoticeOfProceedings noticeOfProceedings = NoticeOfProceedings.builder()
            .build();

        List<DocmosisTemplates> docmosisTemplates = noticeOfProceedings.mapProceedingTypesToDocmosisTemplate();

        assertThat(docmosisTemplates).isEqualTo(List.of());
    }

    List<ProceedingType> buildProceedingTypes(ProceedingType... proceedingType) {
        return List.of(proceedingType);
    }
}
