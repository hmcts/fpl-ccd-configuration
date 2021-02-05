package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.exceptions.HearingOrdersBundleNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftOrdersBundleHearingSelector {

    private final ObjectMapper mapper;

    public Element<HearingOrdersBundle> getSelectedHearingDraftOrdersBundle(CaseData caseData) {
        List<Element<HearingOrdersBundle>> ordersBundleReadyForApproval = caseData.getBundlesForApproval();

        if (ordersBundleReadyForApproval.isEmpty()) {
            throw new IllegalStateException("Bundle not found");
        }

        if (ordersBundleReadyForApproval.size() == 1) {
            return ordersBundleReadyForApproval.get(0);
        }

        UUID selectedHearingDraftOrdersBundleCode = getSelectedCMOId(caseData.getCmoToReviewList());
        return ordersBundleReadyForApproval.stream()
            .filter(element -> element.getId().equals(selectedHearingDraftOrdersBundleCode))
            .findFirst()
            .orElseThrow(() -> new HearingOrdersBundleNotFoundException(
                "Could not find hearing draft orders bundle with id " + selectedHearingDraftOrdersBundleCode));

    }

    private UUID getSelectedCMOId(Object dynamicList) {
        //see RDM-5696 and RDM-6651
        if (dynamicList instanceof String) {
            return UUID.fromString(dynamicList.toString());
        }
        return mapper.convertValue(dynamicList, DynamicList.class).getValueCode();
    }

}
