package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class OthersService {

    public Others expandOthersCollection(CaseData caseData) {
        List<Element<Other>> others = new ArrayList<>();

        if (caseData.getOthers() == null) {
            others.add(Element.<Other>builder()
                .value(Other.builder()
                    .build())
                .build());
        } else {
            //Maintain existing cases where a firstOther exists
            if (isNotEmpty(caseData.getOthers().getFirstOther())
                && !caseData.getOthers().getFirstOther().equals(Other.builder().build())) {
                others.add(Element.<Other>builder().value(caseData.getOthers().getFirstOther()).build());
            }

            if (isNotEmpty(caseData.getOthers().getAdditionalOthers())) {
                others.addAll(caseData.getOthers().getAdditionalOthers());
            }
        }

        return Others.builder()
            .additionalOthers(others)
            .build();
    }

    public Others handleFirstOther(CaseData caseData) {
        List<Element<Other>> others = caseData.getOthers().getAdditionalOthers();

        if (isNotEmpty(caseData.getOthers().getFirstOther())) {
            others.add(0, Element.<Other>builder().value(caseData.getOthers().getFirstOther()).build());
        }

        return Others.builder()
            .additionalOthers(others)
            .build();
    }

    public String buildOthersLabel(Others others) {
        StringBuilder sb = new StringBuilder();
        //Handles old cases where firstOther exists (others event not re-submitted) and new cases with no firstOther
        if (otherExists(others)) {
            int othersListStartIndex = 1;
            if (isNotEmpty(others.getFirstOther())) {
                sb.append(String.format("Person 1 - %s", getName(others.getFirstOther()))).append("\n");
                if (isNotEmpty(others.getAdditionalOthers())) {
                    othersListStartIndex = 0;
                }
            } else if (isNotEmpty(others.getAdditionalOthers())) {
                sb.append(String.format("Person 1 - %s", getName(others.getAdditionalOthers().get(0).getValue())))
                    .append("\n");
            }

            for (int i = othersListStartIndex; i < others.getAdditionalOthers().size(); i++) {
                Other other = others.getAdditionalOthers().get(i).getValue();

                sb.append(String.format("Other person %d - %s", i + 1, getName(other))).append("\n");
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

    public boolean expandedCollectionNotEmpty(List<Element<Other>> additionalOthers) {
        return (isNotEmpty(additionalOthers)
            && !additionalOthers.get(0).getValue().equals(
            Other.builder().address(Address.builder().build()).build()));
    }
}
