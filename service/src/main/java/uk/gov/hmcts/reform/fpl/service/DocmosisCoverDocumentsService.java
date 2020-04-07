package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCoverDoc;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.COVER_DOCS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocmosisCoverDocumentsService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final ObjectMapper mapper;

    public DocmosisDocument createCoverDocuments(String familyManCaseNumber,
                                                 Long caseNumber,
                                                 Representative representative) {
        DocmosisCoverDoc coverDocData = buildCoverDocumentsData(familyManCaseNumber, caseNumber, representative);
        return docmosisDocumentGeneratorService.generateDocmosisDocument(coverDocData.toMap(mapper), COVER_DOCS);
    }

    DocmosisCoverDoc buildCoverDocumentsData(String familyManCaseNumber,
                                                Long caseNumber,
                                                Representative representative) {
        DocmosisCoverDoc.Builder coverDocBuilder = DocmosisCoverDoc.builder();
        return coverDocBuilder.familyManCaseNumber(defaultIfNull(familyManCaseNumber, ""))
                            .ccdCaseNumber(formatCCDCaseNumber(caseNumber))
                            .representativeName(representative.getFullName())
                            .representativeAddress(representative.getAddress().getAddressAsString("\n"))
                            .build();
    }
}
