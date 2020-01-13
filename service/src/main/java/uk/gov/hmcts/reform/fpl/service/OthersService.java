package uk.gov.hmcts.reform.fpl.service;

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

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.PartyType.INDIVIDUAL;

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
        //Gets all others from case data and returns list of confidential others first other and additional other
        final List<Element<Other>> confidentialOthers = new ArrayList<>();

        caseData.getAllOthers().forEach(element -> {

            if (element.containsConfidentialDetails()) {
                confidentialOthers.add(Element.<Other>builder()
                    .id(UUID.randomUUID())
                    .value(element)
                    .build());
            }
        });

        return confidentialOthers;
    }

    public List<Element<Others>> prepareConfidentialOthersForCaseData(List<Element<Other>> confidentialOther) {
        final List<Element<Others>> confidentialOthersForCaseData = new ArrayList<>();
        Other firstOther;

        if (!confidentialOther.isEmpty()) {
            //add the first element to first other and the rest to additional others
            firstOther = confidentialOther.get(0).getValue();
            confidentialOther.remove(0);

            Others other = new Others(firstOther, confidentialOther);

            confidentialOthersForCaseData.add(Element.<Others>builder().value(other).build());
        }

        return confidentialOthersForCaseData;
    }

    public Others modifyHiddenValues(Others others) {
        final List<Element<Other>> othersForPeopleTab = new ArrayList<>();
        Element<Other> firstOther;

        if(others.getFirstOther().containsConfidentialDetails()) {
            firstOther = Element.<Other>builder().value(others.getFirstOther().toBuilder().address(null).build()).build();
        }
        else {
            firstOther = Element.<Other>builder().value(others.getFirstOther()).build();
        }

        others.getAdditionalOthers().stream().forEach(additionalOther -> {
            if (additionalOther.getValue().containsConfidentialDetails()) {
                othersForPeopleTab.add(Element.<Other>builder()
                    .id(UUID.randomUUID())
                    .value(additionalOther.getValue().toBuilder().address(null).build())
                    .build());
            } else{
                othersForPeopleTab.add(Element.<Other>builder()
                    .id(UUID.randomUUID())
                    .value(additionalOther.getValue())
                    .build());

            }

        });

        return others.toBuilder().additionalOthers(othersForPeopleTab).firstOther(firstOther.getValue()).build();
    }
}

