package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.COVER_DOCS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocmosisCoverDocumentsService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    public DocmosisDocument createCoverDocuments(String familyManCaseNumber,
                                                 Long caseNumber,
                                                 Representative representative) {
        Map<String, Object> templateData = buildCoverDocumentsData(familyManCaseNumber, caseNumber, representative);
        return docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, COVER_DOCS);
    }

    Map<String, Object> buildCoverDocumentsData(String familyManCaseNumber,
                                                Long caseNumber,
                                                Representative representative) {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", familyManCaseNumber)
            .put("ccdCaseNumber", formatCCDCaseNumber(caseNumber))
            .put("representativeName", representative.getFullName())
            .put("representativeAddress", representative.getAddress().getAddressAsString("\n"))
            .build();
    }
}
