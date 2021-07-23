package uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisTranslationRequest implements DocmosisData {
    private String name;
    private String department;
    private String contactInformation;
    private DocmosisWelshProject project;
    private String familyManCaseNumber;
    private String description;
    private DocmosisTranslateLanguages translate;
    private DocmosisWelshLayout layout;
    private int wordCount;
    private LocalDate dateOfReturn;
    private RenderFormat format;
}
