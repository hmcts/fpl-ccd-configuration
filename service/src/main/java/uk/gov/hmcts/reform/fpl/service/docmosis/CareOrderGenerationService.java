package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;

import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;

@Service
public class CareOrderGenerationService extends GeneratedOrderTemplateDataGeneration {

    public CareOrderGenerationService(CaseDataExtractionService caseDataExtractionService,
        LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration) {
        super(caseDataExtractionService, localAuthorityNameLookupConfiguration);
    }

    @Override
    DocmosisGeneratedOrderBuilder getGeneratedOrderBuilder(CaseData caseData) {
        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();
        GeneratedOrderSubtype subtype = orderTypeAndDocument.getSubtype();
        InterimEndDate interimEndDate = caseData.getInterimEndDate();

        DocmosisGeneratedOrderBuilder<?, ?> orderBuilder = DocmosisGeneratedOrder.builder();
        if (subtype == INTERIM) {
            orderBuilder
                .orderTitle(orderTypeAndDocument.getFullType(INTERIM))
                .childrenAct("Section 38 Children Act 1989");
        } else if (subtype == FINAL) {
            orderBuilder
                .orderTitle(orderTypeAndDocument.getFullType())
                .childrenAct("Section 31 Children Act 1989");
        }

        int childrenCount = getChildrenCount(caseData);

        return orderBuilder
            .localAuthorityName(getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .orderDetails(getFormattedCareOrderDetails(childrenCount, caseData.getCaseLocalAuthority(),
                orderTypeAndDocument.hasInterimSubtype(), interimEndDate));
    }

    private String getFormattedCareOrderDetails(int numOfChildren,
        String caseLocalAuthority,
        boolean isInterim,
        InterimEndDate interimEndDate) {
        String childOrChildren = (numOfChildren == 1 ? "child is" : "children are");
        return String.format("It is ordered that the %s placed in the care of %s%s.",
            childOrChildren, getLocalAuthorityName(caseLocalAuthority),
            isInterim ? " until " + getInterimEndDateString(interimEndDate) : "");
    }

}
