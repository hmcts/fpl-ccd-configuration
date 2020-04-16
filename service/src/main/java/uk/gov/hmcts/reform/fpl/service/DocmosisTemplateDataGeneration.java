package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.io.IOException;
import java.util.Map;

public abstract class DocmosisTemplateDataGeneration {
    static final String IMAGE_REF_DRAFT_WATERMARK = "[userImage:draft-watermark.png]";
    static final String IMAGE_REF_COURT_SEAL = "[userImage:family-court-seal.png]";
    static final String IMAGE_REF_CREST = "[userImage:crest.png]";

    public static Map<String, Object> getDraftWaterMarkData() {
        return ImmutableMap.of(
            "draftbackground", IMAGE_REF_DRAFT_WATERMARK
        );
    }

    protected Map<String, Object> getCourtSealData() {
        return ImmutableMap.of(
            "courtseal", IMAGE_REF_COURT_SEAL
        );
    }

    protected Map<String, Object> getCrestData() {
        return ImmutableMap.of(
            "crest", IMAGE_REF_CREST
        );
    }

    public abstract Map<String, Object> getTemplateData(CaseData caseData, boolean draft) throws IOException;
}
