package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCoverDocument;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.COVER_DOCS;
import static uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisTemplateDataGeneration.getHmctsLogoLarge;
import static uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisTemplateDataGeneration.getHmctsLogoSmall;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocmosisCoverDocumentsService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final CaseDetailsHelper caseDetailsHelper;

    public DocmosisDocument createCoverDocuments(String familyManCaseNumber, Long caseNumber, Recipient recipient) {
        DocmosisCoverDocument coverDocumentData = buildCoverDocumentsData(familyManCaseNumber, caseNumber, recipient);
        return docmosisDocumentGeneratorService.generateDocmosisDocument(coverDocumentData, COVER_DOCS);
    }

    public DocmosisCoverDocument buildCoverDocumentsData(String familyManCaseNumber, Long caseId, Recipient recipient) {
        return DocmosisCoverDocument.builder()
            .familyManCaseNumber(defaultIfNull(familyManCaseNumber, ""))
            .ccdCaseNumber(caseDetailsHelper.formatCCDCaseNumber(caseId))
            .representativeName(recipient.getFullName())
            .representativeAddress(recipient.getAddress().getAddressAsString("\n"))
            .hmctsLogoLarge(getHmctsLogoLarge())
            .hmctsLogoSmall(getHmctsLogoSmall())
            .build();
    }
}
