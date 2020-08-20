package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ConfidentialDetailsHelper.getConfidentialItemToAdd;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class OthersService {

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

    private Other addConfidentialDetails(Other confidentialOther, Element<Other> other) {
        return other.getValue().toBuilder()
            .telephone(confidentialOther.getTelephone())
            .address(confidentialOther.getAddress())
            .build();
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
}
