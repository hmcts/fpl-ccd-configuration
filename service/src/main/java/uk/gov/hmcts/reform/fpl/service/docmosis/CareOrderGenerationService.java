package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@Service
public class CareOrderGenerationService extends GeneratedOrderTemplateDataGeneration {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    public CareOrderGenerationService(CaseDataExtractionService caseDataExtractionService,
        LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration) {
        super(caseDataExtractionService);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
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

        List<DocmosisChild> children = getChildrenDetails(caseData);

        return orderBuilder
            .localAuthorityName(getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .orderDetails(getFormattedCareOrderDetails(children.size(), caseData.getCaseLocalAuthority(),
                orderTypeAndDocument.hasInterimSubtype(), interimEndDate));
    }

    private String getLocalAuthorityName(String caseLocalAuthority) {
        return localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);
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

    private String getInterimEndDateString(InterimEndDate interimEndDate) {
        return interimEndDate.toLocalDateTime()
            .map(dateTime -> {
                final String dayOrdinalSuffix = getDayOfMonthSuffix(dateTime.getDayOfMonth());
                return formatLocalDateTimeBaseUsingFormat(
                    dateTime, String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix));
            })
            .orElse("the end of the proceedings");
    }
}
