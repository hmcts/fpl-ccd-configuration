package uk.gov.hmcts.reform.fpl.service.orders.generator;

import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;

public interface DocmosisParameterGenerator {

    Order accept();

    DocmosisParameters generate(CaseData caseData);

    DocmosisTemplates template();

    default List<DocumentReference> additionalDocuments(CaseData caseData) {
        return List.of();
    }
}
