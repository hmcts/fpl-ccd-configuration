package uk.gov.hmcts.reform.fpl.service.docmosis;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.io.IOException;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.DRAFT_WATERMARK;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.HMCTS_LOGO_LARGE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.HMCTS_LOGO_SMALL;

public abstract class DocmosisTemplateDataGeneration<T> {

    protected String getDraftWaterMarkData() {
        return DRAFT_WATERMARK.getValue();
    }

    protected String getCourtSealData() {
        return COURT_SEAL.getValue();
    }

    protected String getCourtSealData(Language language) {
        return COURT_SEAL.getValue(language);
    }

    protected String getCrestData() {
        return CREST.getValue();
    }

    protected static String getHmctsLogoSmall() {
        return HMCTS_LOGO_SMALL.getValue();
    }

    protected static String getHmctsLogoLarge() {
        return HMCTS_LOGO_LARGE.getValue();
    }

    public abstract T getTemplateData(CaseData caseData) throws IOException;
}
