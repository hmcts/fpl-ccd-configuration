package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class UpdateHearingOrderBundlesDraftsTest {

    UpdateHearingOrderBundlesDrafts underTest = new UpdateHearingOrderBundlesDrafts();

    @Test
    void shouldRemoveFromHearingOrdersBundlesDraftsWhenOrdersInSelectedHearingBundleAreEmpty() {
        Element<HearingOrdersBundle> selectedBundle = element(
            HearingOrdersBundle.builder().orders(emptyList()).build());

        Element<HearingOrdersBundle> otherBundle = element(HearingOrdersBundle.builder()
            .orders(newArrayList(element(HearingOrder.builder().type(C21).build()))).build());

        List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = List.of(otherBundle, selectedBundle);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of("hearingOrdersBundlesDrafts", hearingOrdersBundlesDrafts)))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        underTest.update(caseDetailsMap, hearingOrdersBundlesDrafts, selectedBundle);

        assertThat(caseDetailsMap.get("hearingOrdersBundlesDrafts")).isEqualTo(List.of(otherBundle));
    }

    @Test
    void shouldReplaceHearingOrdersBundleWhenSelectedHearingBundleContainsOrders() {
        UUID selectedBundleId = UUID.randomUUID();

        Element<HearingOrdersBundle> updatedBundle = element(selectedBundleId,
            HearingOrdersBundle.builder()
                .orders(newArrayList(element(HearingOrder.builder().type(AGREED_CMO).build()))).build());

        Element<HearingOrdersBundle> selectedBundleBeforeUpdate = element(selectedBundleId,
            HearingOrdersBundle.builder()
                .orders(newArrayList(element(HearingOrder.builder().type(AGREED_CMO).build()),
                    element(HearingOrder.builder().type(C21).build()))).build());

        Element<HearingOrdersBundle> bundle = element(HearingOrdersBundle.builder()
            .orders(newArrayList(element(HearingOrder.builder().type(C21).build()))).build());

        List<Element<HearingOrdersBundle>> hearingOrdersBundles = List.of(bundle, selectedBundleBeforeUpdate);

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(newHashMap(Map.of("hearingOrdersBundlesDrafts", hearingOrdersBundles)))
            .build());

        underTest.update(caseDetailsMap, hearingOrdersBundles, updatedBundle);

        assertThat(caseDetailsMap.get("hearingOrdersBundlesDrafts")).isEqualTo(List.of(bundle, updatedBundle));
    }
}
