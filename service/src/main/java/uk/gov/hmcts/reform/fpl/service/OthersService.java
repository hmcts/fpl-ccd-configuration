package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ConfidentialDetailsHelper.getConfidentialItemToAdd;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getElement;

@Service
public class OthersService {

    public Selector buildOtherSelector(List<Other> allOthers, List<Other> selectedOthers) {
        List<Integer> selected = new ArrayList<>();

        if (selectedOthers != null) {
            for (int i = 0; i < allOthers.size(); i++) {
                if (selectedOthers.contains(allOthers.get(i))) {
                    selected.add(i);
                }
            }
        }

        return Selector.builder().selected(selected).build().setNumberOfOptions(allOthers.size());
    }

    public String buildOthersLabel(List<Element<Other>> others) {
        StringBuilder sb = new StringBuilder();

        if (isNotEmpty(others)) {
            sb.append(String.format("Person 1 - %s", defaultIfNull(others.get(0).getValue().getFullName(), "")))
                .append("\n");

            if (others.size() > 1) {
                for (int i = 1; i < others.size(); i++) {
                    Other other = others.get(i).getValue();

                    sb.append(String.format("Other person %d - %s", i, defaultIfNull(other.getFullName(), "")))
                        .append("\n");
                }
            }
        } else {
            sb.append("No others on the case");
        }

        return sb.toString();
    }

    public List<Element<Other>> prepareOthers(CaseData caseData) {
        return caseData.getOthersV2().stream()
            .map(element -> {
                Other other = element.getValue();
                if (other.containsConfidentialDetails()) {
                    Other confidentialOther = getConfidentialItemToAdd(caseData.getConfidentialOthers(), element);
                    other = addConfidentialDetails(confidentialOther, element);
                }

                return element(element.getId(), convertLegacyOther(other));
            }).toList();
    }

    public Other convertLegacyOther(Other other) {
        return other.toBuilder()
            .firstName(isEmpty(other.getFirstName()) ? other.getName() : other.getFirstName())
            .name(null)
            .build();
    }

    public String getOthersLabel(List<Element<Other>> others) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < others.size(); i++) {
            Other other = others.get(i).getValue();

            builder.append(String.format("Other %d: %s", i + 1, other.getFullName()));
            builder.append("\n");
        }

        return builder.toString();
    }

    public List<Element<Other>> getSelectedOthers(CaseData caseData) {
        return getSelectedOthers(caseData.getOthersV2(), caseData.getOthersSelector(),
            caseData.getSendOrderToAllOthers());
    }

    public List<Element<Other>> getSelectedOthers(List<Element<Other>> others, Selector selector,
                                                  String allOthersSelected) {

        if (useAllOthers(allOthersSelected)) {
            return others;
        } else {
            if (isNull(selector) || isEmpty(selector.getSelected())) {
                return Collections.emptyList();
            }
            return selector.getSelected().stream()
                .map(others::get)
                .collect(toList());
        }
    }

    public Element<Other> getSelectedPreparedOther(CaseData caseData, DynamicList singleSelector) {
        return getElement(singleSelector.getValueCodeAsUUID(), prepareOthers(caseData));
    }

    public Element<Other> getSelectedOther(CaseData caseData, DynamicList singleSelector) {
        return getElement(singleSelector.getValueCodeAsUUID(), caseData.getOthersV2());
    }

    private boolean useAllOthers(String sendOrdersToAllOthers) {
        return "Yes".equals(sendOrdersToAllOthers);
    }

    public Other addConfidentialDetails(Other confidentialOther, Element<Other> otherElement) {
        Other other = otherElement.getValue();
        Other.OtherBuilder retBuilder = other.toBuilder();

        if (YES.equalsString(other.getHideTelephone())) {
            retBuilder.telephone(confidentialOther.getTelephone());
        }
        if (YES.equalsString(other.getHideAddress())) {
            retBuilder.address(confidentialOther.getAddress());

            if (isEmpty(other.getAddressKnowV2())) {
                retBuilder.addressKnowV2(confidentialOther.getAddressKnowV2());
            }
        }

        return retBuilder.build();
    }

    public List<Element<Other>> consolidateAndRemoveHiddenFields(CaseData caseData) {
        return caseData.getOthersV2().stream()
            .map(otherElement -> {
                Other other = otherElement.getValue();
                if (!isNull(other.getAddressKnowV2())) {
                    Other.OtherBuilder builder = other.toBuilder();
                    if (IsAddressKnowType.NO.equals(other.getAddressKnowV2())) {
                        builder = builder.address(null);
                    } else {
                        builder = builder.addressNotKnowReason(null);
                    }

                    if (IsAddressKnowType.LIVE_IN_REFUGE.equals(other.getAddressKnowV2())) {
                        builder = builder.hideAddress(YES.getValue());
                    }
                    return element(otherElement.getId(), builder.build());
                }
                return otherElement;
            }).toList();
    }
}
