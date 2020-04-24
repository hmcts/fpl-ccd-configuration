package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCoverDocument;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.COVER_DOCS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocmosisCoverDocumentsService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    public DocmosisDocument createCoverDocuments(String familyManCaseNumber,
                                                 Long caseNumber,
                                                 Representative representative) {
        DocmosisCoverDocument coverDocumentData = buildCoverDocumentsData(familyManCaseNumber,
                                                                          caseNumber,
                                                                          representative);
        return docmosisDocumentGeneratorService.generateDocmosisDocument(coverDocumentData, COVER_DOCS);
    }

    DocmosisCoverDocument buildCoverDocumentsData(String familyManCaseNumber,
                                                Long caseNumber,
                                                Representative representative) {
        return DocmosisCoverDocument.builder()
                            .familyManCaseNumber(defaultIfNull(familyManCaseNumber, ""))
                            .ccdCaseNumber(formatCCDCaseNumber(caseNumber))
                            .representativeName(representative.getFullName())
                            .representativeAddress(representative.getAddress().getAddressAsString("\n"))
                            .build();
    }
}
