package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;

@Service

public class NoticeOfHearingGenerationService {

    public DocmosisNoticeOfHearing getTemplateData(CaseData caseData) {
        return DocmosisNoticeOfHearing.builder().build();
    }

}
