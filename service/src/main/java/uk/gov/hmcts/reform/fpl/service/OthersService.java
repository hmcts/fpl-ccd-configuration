package uk.gov.hmcts.reform.fpl.service;

import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.nameUUIDFromBytes;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.PartyType.INDIVIDUAL;
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
        final List<Element<Other>> othersForPeopleTab = new ArrayList<>();
        Element<Other> firstOther;

        if(others.getFirstOther().containsConfidentialDetails()) {
            firstOther = Element.<Other>builder().value(others.getFirstOther().toBuilder().address(null).telephone(null).build()).build();
        }
        else {
            firstOther = Element.<Other>builder().value(others.getFirstOther()).build();
        }

        others.getAdditionalOthers().stream().forEach(additionalOther -> {
            if (additionalOther.getValue().containsConfidentialDetails()) {
                othersForPeopleTab.add(Element.<Other>builder()
                    .id(additionalOther.getId())
                    .value(additionalOther.getValue().toBuilder().address(null).telephone(null).build())
                    .build());
            } else{
                othersForPeopleTab.add(Element.<Other>builder()
                    .id(additionalOther.getId())
                    .value(additionalOther.getValue())
                    .build());

            }

        });

        return others.toBuilder().additionalOthers(othersForPeopleTab).firstOther(firstOther.getValue()).build();
    }

    public Others prepareOthers(CaseData caseData) {
        final List <Element<Other>> additionalOthers = new ArrayList<>();
        Other firstOther = null;
        Element<Other> firstOtherElement = null;

            caseData.getAllOthers().forEach(element -> {
                if (element.getValue().containsConfidentialDetails()) {
                    System.out.println("Element to add is" + getElementToAdd(caseData.getConfidentialOthers(),element));
                    additionalOthers.add(getElementToAdd(caseData.getConfidentialOthers(), element));
                } else {
                    additionalOthers.add(element);
                }
            });

            List<Element<Other>> confidentialOthers = caseData.getConfidentialOthers();

            confidentialOthers.removeAll(additionalOthers);

            System.out.println("Element to find is" + confidentialOthers);

            if(!additionalOthers.isEmpty())
            {
                if(additionalOthers.get(0).getValue().containsConfidentialDetails()) //this should be if additional others 0 is confidential
                {
                    firstOther = confidentialOthers.get(0).getValue();
                    additionalOthers.remove(0);
                }else {
                    firstOther = additionalOthers.get(0).getValue();
                    additionalOthers.remove(0);
                }
            }

        return Others.builder().firstOther(firstOther).additionalOthers(additionalOthers).build();
    }

    public Element<Other> getElementToAdd(List<Element<Other>> confidentialOthers,
                                                Element<Other> element) {
        return confidentialOthers.stream()
            .filter(confidentialOther -> confidentialOther.getId().equals(element.getId()))
            .findFirst()
            .orElse(element);
    }
}

