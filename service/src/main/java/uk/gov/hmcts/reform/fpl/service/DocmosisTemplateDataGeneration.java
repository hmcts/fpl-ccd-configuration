package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.io.IOException;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.DRAFT_WATERMARK;

public abstract class DocmosisTemplateDataGeneration {

    protected static Map<String, Object> getDraftWaterMarkData() {
        return ImmutableMap.of(
            "draftbackground", DRAFT_WATERMARK.getValue()
        );
    }

    protected Map<String, Object> getCourtSealData() {
        return ImmutableMap.of(
            "courtseal", COURT_SEAL.getValue()
        );
    }

    protected Map<String, Object> getCrestData() {
        return ImmutableMap.of(
            "crest", CREST.getValue()
        );
    }

    public abstract Map<String, Object> getTemplateData(CaseData caseData, boolean draft) throws IOException;
}
