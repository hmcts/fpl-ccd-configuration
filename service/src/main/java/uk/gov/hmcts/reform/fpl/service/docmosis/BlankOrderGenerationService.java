package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Service
public class BlankOrderGenerationService extends GeneratedOrderTemplateDataGeneration {

    @Autowired
    public BlankOrderGenerationService(CaseDataExtractionService caseDataExtractionService,
        LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration) {
        super(caseDataExtractionService, localAuthorityNameLookupConfiguration);
    }

    @SuppressWarnings("rawtypes")
    @Override
    DocmosisGeneratedOrderBuilder populateCustomOrderFields(CaseData caseData) {
        return DocmosisGeneratedOrder.builder()
            .orderTitle(defaultIfNull(caseData.getOrder().getTitle(), "Order"))
            .childrenAct("Children Act 1989")
            .orderDetails(caseData.getOrder().getDetails());
    }
}
