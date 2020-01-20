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
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
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

    private String getName(Other other) {
        return defaultIfNull(other.getName(), "BLANK - Please complete");
    }

    private boolean otherExists(Others others) {
        return others != null && (others.getFirstOther() != null || others.getAdditionalOthers() != null);
    }

    public Others modifyHiddenValues(List<Element<Other>> others) {
        List<Element<Other>> othersList = new ArrayList<>();

        others.forEach(other -> {
            if (other.getValue().containsConfidentialDetails()) {
                othersList.add(
                    element(other.getId(), other.getValue().toBuilder()
                        .address(null)
                        .telephone(null)
                        .build()));
            } else {
                othersList.add(other);
            }
        });

        return new Others(othersList.get(0).getValue(), othersList.subList(1, othersList.size()));
    }

    public List<Element<Other>> retainConfidentialDetails(List<Element<Other>> confidentialOthers) {
        final List<Element<Other>> confidentialOthersModified = new ArrayList<>();

        confidentialOthers.forEach(element -> confidentialOthersModified.add(
            element(element.getId(), Other.builder()
                .address(element.getValue().getAddress())
                .telephone(element.getValue().getTelephone())
                .build())));

        return confidentialOthersModified;
    }

    public Others prepareOthers(CaseData caseData) {
        List<Element<Other>> others = new ArrayList<>();

        caseData.getAllOthers().forEach(element -> {
            if (element.getValue().containsConfidentialDetails()) {
                Element<Other> confidentialOther = findConfidentialOther(caseData.getConfidentialOthers(), element);

                others.add(element(element.getId(), addConfidentialDetails(confidentialOther, element)));
            } else {
                others.add(element);
            }
        });

        Other firstOther = getFirstOther(caseData.getConfidentialOthers(), others);

        if (isNotEmpty(others)) {
            others.remove(0);
        }

        return Others.builder().firstOther(firstOther).additionalOthers(others).build();
    }

    // This finds the element id in confidential others that doesn't match which is therefore the first other id
    // Hacky but only way we can find the first other id as it is not an element
    private Other getFirstOther(List<Element<Other>> confidentialOthers, List<Element<Other>> others) {
        List<UUID> ids = others.stream().map(Element::getId).collect(toList());
        Other firstOther = null;

        if (!others.isEmpty()) {
            firstOther = confidentialOthers.stream()
                .filter(other -> !ids.contains(other.getId()))
                .map(other -> addConfidentialDetails(other, others.get(0)))
                .findFirst()
                .orElse(others.get(0).getValue());
        }

        return firstOther;
    }

    private Other addConfidentialDetails(Element<Other> confidentialOther, Element<Other> other) {
        return other.getValue().toBuilder()
            .telephone(confidentialOther.getValue().getTelephone())
            .address(confidentialOther.getValue().getAddress())
            .build();
    }

    private Element<Other> findConfidentialOther(List<Element<Other>> confidentialOthers, Element<Other> element) {
        return confidentialOthers.stream()
            .filter(confidentialOther -> confidentialOther.getId().equals(element.getId()))
            .findFirst()
            .orElse(element);
    }
}
