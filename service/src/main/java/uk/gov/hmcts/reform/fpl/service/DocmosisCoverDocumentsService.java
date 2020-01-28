package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.COVER;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.GENERAL;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocmosisCoverDocumentsService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    public DocmosisDocument createCoverSheet(Long caseId, Representative representative) {
        Map<String, Object> templateData = buildCoverSheetData(caseId, representative);
        return docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, COVER);
    }

    public DocmosisDocument createGeneralLetter(String familyManCaseNumber, Representative representative) {
        Map<String, Object> templateData = buildGeneralLetterData(familyManCaseNumber, representative);
        return docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, GENERAL);
    }

    Map<String, Object> buildGeneralLetterData(String familyManCaseNumber, Representative representative) {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", familyManCaseNumber)
            .putAll(getRepresentativeData(representative))
            .build();
    }

    Map<String, Object> buildCoverSheetData(Long caseId, Representative representative) {
        return ImmutableMap.<String, Object>builder()
            .put("ccdCaseNumber", caseId.toString())
            .putAll(getRepresentativeData(representative))
            .build();
    }

    private Map<String, Object> getRepresentativeData(Representative representative) {
        return ImmutableMap.<String, Object>builder()
            .put("representativeName", representative.getFullName())
            .put("representativeAddress", representative.getAddress().getAddressAsString("\n"))
            .build();
    }
}
