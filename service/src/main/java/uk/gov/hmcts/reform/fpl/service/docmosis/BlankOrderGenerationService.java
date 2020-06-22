package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BlankOrderGenerationService extends GeneratedOrderTemplateDataGeneration {

    @SuppressWarnings("rawtypes")
    @Override
    DocmosisGeneratedOrderBuilder populateCustomOrderFields(CaseData caseData) {
        return DocmosisGeneratedOrder.builder()
            .orderTitle(defaultIfNull(caseData.getOrder().getTitle(), "Order"))
            .childrenAct("Children Act 1989")
            .orderDetails(caseData.getOrder().getDetails());
    }
}
