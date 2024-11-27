package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ConfidentialDetailsHelper.getConfidentialItemToAdd;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeCollection;

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

    public String buildOthersLabel(Others others) {
        StringBuilder sb = new StringBuilder();

        if (otherExists(others)) {
            if (others.getFirstOther() != null) {
                sb.append(String.format("Person 1 - %s", getName(others.getFirstOther()))).append("\n");
            }

            if (others.getAdditionalOthers() != null) {
                for (int i = 0; i < others.getAdditionalOthers().size(); i++) {
                    Other other = others.getAdditionalOthers().get(i).getValue();

                    sb.append(String.format("Other person %d - %s", i + 1, getName(other))).append("\n");
                }
            }
        } else {
            sb.append("No others on the case");
        }

        return sb.toString();
    }

    public Others prepareOthers(CaseData caseData) {
        List<Element<Other>> others = new ArrayList<>();

        caseData.getAllOthers().forEach(element -> {
            if (element.getValue().containsConfidentialDetails()) {
                Other confidentialOther = getConfidentialItemToAdd(caseData.getConfidentialOthers(), element);
                others.add(element(element.getId(), addConfidentialDetails(confidentialOther, element)));
            } else {
                others.add(element);
            }
        });

        return Others.builder()
            .firstOther(getFirstOther(caseData.getConfidentialOthers(), others))
            .additionalOthers(getAdditionalOthers(others))
            .build();
    }

    public String getOthersLabel(List<Element<Other>> others) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < others.size(); i++) {
            Other other = others.get(i).getValue();

            builder.append(String.format("Other %d: %s", i + 1, other.getName()));
            builder.append("\n");
        }

        return builder.toString();
    }

    public List<Element<Other>> getSelectedOthers(CaseData caseData) {
        return getSelectedOthers(caseData.getAllOthers(), caseData.getOthersSelector(),
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
        return getSelectedOther(prepareOthers(caseData), singleSelector, randomUUID());
    }

    private Element<Other> getSelectedOther(Others others, DynamicList singleSelector, UUID firstOtherUUID) {
        if (others == null) {
            return null;
        }
        return ofNullable(singleSelector).map(DynamicList::getValueCodeAsUUID)
            .flatMap(otherId -> nullSafeCollection(others.getAdditionalOthers()).stream()
                .filter(o -> otherId.equals(o.getId()))
                .findFirst())
            .orElseGet(() -> nonNull(others.getFirstOther())
                ? element(firstOtherUUID, others.getFirstOther()) : null);
    }

    public Element<Other> getSelectedOther(CaseData caseData, DynamicList singleSelector) {
        return getSelectedOther(caseData, singleSelector, randomUUID());
    }

    public Element<Other> getSelectedOther(CaseData caseData, DynamicList singleSelector, UUID firstOtherUUID) {
        return getSelectedOther(caseData.getOthers(), singleSelector, firstOtherUUID);
    }

    private boolean useAllOthers(String sendOrdersToAllOthers) {
        return "Yes".equals(sendOrdersToAllOthers);
    }

    public Other addConfidentialDetails(Other confidentialOther, Element<Other> other) {
        Other ret = other.getValue().toBuilder()
            .telephone(confidentialOther.getTelephone())
            .address(confidentialOther.getAddress())
            .build();
        return ret;
    }

    // This finds firstOther element id in confidential others that doesn't match.
    private Other getFirstOther(List<Element<Other>> confidentialOthers, List<Element<Other>> others) {
        List<UUID> ids = others.stream().map(Element::getId).collect(toList());

        if (!others.isEmpty()) {
            return confidentialOthers.stream()
                .filter(other -> !ids.contains(other.getId()))
                .map(other -> addConfidentialDetails(other.getValue(), others.get(0)))
                .findFirst()
                .orElse(others.get(0).getValue());
        }
        return null;
    }

    // This removes firstOther element in others.
    private List<Element<Other>> getAdditionalOthers(List<Element<Other>> others) {
        if (isNotEmpty(others)) {
            others.remove(0);
        }
        return others;
    }

    private String getName(Other other) {
        return defaultIfNull(other.getName(), "");
    }

    private boolean otherExists(Others others) {
        return others != null && (others.getFirstOther() != null || others.getAdditionalOthers() != null);
    }

    public Others consolidateAndRemoveHiddenFields(CaseData caseData) {
        return Others.from(caseData.getAllOthers().stream()
            .map(otherElement -> {
                Other other = otherElement.getValue();
                if (!isNull(other.getAddressKnow())) {
                    Other.OtherBuilder builder = other.toBuilder();
                    if (IsAddressKnowType.NO.equals(other.getAddressKnow())) {
                        builder = builder.address(null);
                    } else {
                        builder = builder.addressNotKnowReason(null);
                    }

                    if (IsAddressKnowType.LIVE_IN_REFUGE.equals(other.getAddressKnow())) {
                        builder = builder.detailsHidden(YesNo.YES.getValue());
                    }
                    return element(otherElement.getId(), builder.build());
                }
                return otherElement;
            }).toList());
    }
}
