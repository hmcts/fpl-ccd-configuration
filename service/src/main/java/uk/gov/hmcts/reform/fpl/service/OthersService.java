package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;

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

    public List<Element<Other>> getAllConfidentialOther(CaseData caseData) {
        final List<Element<Other>> confidentialOthers = new ArrayList<>();

        caseData.getAllOthers().forEach(element -> {
            if (element.getValue().containsConfidentialDetails()) {
                // we will need to persist id of element so that we can place back into others.
                confidentialOthers.add(element);
            }
        });

        return confidentialOthers;
    }

    public Others modifyHiddenValues(Others others) {
        final List<Element<Other>> othersList = new ArrayList<>();
        Element<Other> firstOther;

        if (others.getFirstOther().containsConfidentialDetails()) {
            firstOther = Element.<Other>builder()
                .value(others
                    .getFirstOther()
                    .toBuilder()
                    .address(null)
                    .telephone(null)
                    .build())
                .build();
        } else {
            firstOther = Element.<Other>builder()
                .value(others.getFirstOther()).build();
        }

        others.getAdditionalOthers().stream().forEach(additionalOther -> {
            if (additionalOther.getValue().containsConfidentialDetails()) {
                othersList.add(Element.<Other>builder()
                    .id(additionalOther.getId())
                    .value(additionalOther.getValue().toBuilder().address(null).telephone(null).build())
                    .build());
            } else {
                othersList.add(Element.<Other>builder()
                    .id(additionalOther.getId())
                    .value(additionalOther.getValue())
                    .build());

            }

        });

        return others.toBuilder().additionalOthers(othersList).firstOther(firstOther.getValue()).build();
    }

    public List<Element<Other>> modifyHiddenValuesConfidentialOthers(List<Element<Other>> confidentialOthers) {
        final List<Element<Other>> confidentialOthersModified = new ArrayList<>();

        confidentialOthers.stream().forEach(confidentialOther -> {
            confidentialOthersModified.add(Element.<Other>builder()
                .id(confidentialOther.getId())
                .value(confidentialOther.getValue().toBuilder()
                    .DOB(null)
                    .gender(null)
                    .birthPlace(null)
                    .childInformation(null)
                    .genderIdentification(null)
                    .litigationIssues(null)
                    .litigationIssuesDetails(null)
                    .detailsHidden(null)
                    .detailsHiddenReason(null)
                    .build())
                .build());
        });

        return confidentialOthersModified;
    }

    public Others prepareOthers(CaseData caseData) {
        final List<Element<Other>> others = new ArrayList<>();

        caseData.getAllOthers().forEach(element -> {
            if (element.getValue().containsConfidentialDetails()) {

                Element<Other> confidentialOther = getElementToAdd(caseData.getConfidentialOthers(), element);

                Element<Other> other = Element.<Other>builder()
                     .id(element.getId())
                     .value(buildOtherElement(confidentialOther, element))
                     .build();

                others.add(other);

            } else {
                others.add(element);
            }
        });

        Other firstOther = getFirstOther(caseData, others);

        return Others.builder().firstOther(firstOther).additionalOthers(others).build();
    }

    private Other getFirstOther(CaseData caseData, List<Element<Other>> others) {
        // This finds the element id in confidential others that doesn't match which is therefore the first other id
        // Hacky but only way we can find the first other id as it is not an element
        List<Element<Other>> confidentialOthers = caseData.getConfidentialOthers();
        confidentialOthers.removeAll(others);
        Other firstOther = null;

        if (!others.isEmpty()) {
            if (others.get(0).getValue().containsConfidentialDetails()) {
                Element<Other> confidentialOther = confidentialOthers.get(0);
                Element<Other> other = others.get(0);

                firstOther = buildOtherElement(confidentialOther,other);
                others.remove(0);
            } else {
                firstOther = others.get(0).getValue();
                others.remove(0);
            }
        }

        return firstOther;
    }

    private Other buildOtherElement(Element<Other> confidentialOther, Element<Other> other) {
        return  Other.builder()
            .DOB(other.getValue().getDOB())
            .name(other.getValue().getName())
            .gender(other.getValue().getGender())
            .birthPlace(other.getValue().getBirthPlace())
            .childInformation(other.getValue().getChildInformation())
            .genderIdentification(other.getValue().getGenderIdentification())
            .litigationIssues(other.getValue().getLitigationIssues())
            .litigationIssuesDetails(other.getValue().getLitigationIssuesDetails())
            .detailsHidden(other.getValue().getDetailsHidden())
            .detailsHiddenReason(other.getValue().getDetailsHiddenReason())
            .telephone(confidentialOther.getValue().getTelephone())
            .address(confidentialOther.getValue().getAddress())
            .build();
    }

    private Element<Other> getElementToAdd(List<Element<Other>> confidentialOthers,
                                                Element<Other> element) {
        return confidentialOthers.stream()
            .filter(confidentialOther -> confidentialOther.getId().equals(element.getId()))
            .findFirst()
            .orElse(element);
    }
}

