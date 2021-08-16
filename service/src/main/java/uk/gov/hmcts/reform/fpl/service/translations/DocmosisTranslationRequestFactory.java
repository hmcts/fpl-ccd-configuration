package uk.gov.hmcts.reform.fpl.service.translations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisTranslateLanguages;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisTranslationRequest;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisWelshLayout;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisWelshProject;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_SHORT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DocmosisTranslationRequestFactory {

    private static final String NAME = "Courts and Tribunals Service Centre";
    private static final String DEPARTMENT = "Family Public Law";
    private static final String CONTACT_INFORMATION = "contactfpl@justice.gov.uk";

    private final Time time;
    private final DocumentWordCounter documentWordCounter;

    public DocmosisTranslationRequest create(CaseData caseData,
                                             LanguageTranslationRequirement language,
                                             String documentDescription, byte[] originalDocumentContent) {
        return DocmosisTranslationRequest.builder()
            .format(RenderFormat.WORD)
            .name(NAME)
            .department(DEPARTMENT)
            .contactInformation(CONTACT_INFORMATION)
            .ccdId(caseData.getId().toString())
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .project(DocmosisWelshProject.builder()
                .reform(true)
                .build())
            .description(String.format("Translation of Document %s", documentDescription))
            .layout(DocmosisWelshLayout.builder()
                .mirrorImage(true)
                .build())
            .translate(DocmosisTranslateLanguages.builder()
                .englishToWelsh(language == ENGLISH_TO_WELSH)
                .welshToEnglish(language == WELSH_TO_ENGLISH)
                .build())
            .wordCount(documentWordCounter.count(originalDocumentContent))
            .dateOfReturn(formatLocalDateToString(time.now().toLocalDate().plusDays(1), DATE_SHORT))
            .build();
    }

}
