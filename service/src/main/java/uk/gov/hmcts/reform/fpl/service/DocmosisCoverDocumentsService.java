package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.COVER_SHEET;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocmosisCoverDocumentsService {

    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    public DocmosisDocument createCoverSheet(Long caseId, Representative representative) {
        Map<String, Object> templateData = buildCoverSheetData(caseId, representative);
        return docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, COVER_SHEET);
    }

    Map<String, Object> buildCoverSheetData(Long caseNumber, Representative representative) {
        return ImmutableMap.<String, Object>builder()
            .put("ccdCaseNumber", formatCCDCaseNumber(caseNumber))
            .putAll(getRepresentativeData(representative))
            .build();
    }

    private Map<String, Object> getRepresentativeData(Representative representative) {
        return ImmutableMap.of(
            "representativeName", representative.getFullName(),
            "representativeAddress", representative.getAddress().getAddressAsString("\n")
        );
    }
}
