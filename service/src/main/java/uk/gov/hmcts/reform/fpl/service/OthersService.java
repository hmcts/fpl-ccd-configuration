package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class OthersService {

    public Others expandOthersCollection(CaseData caseData) {
        List<Element<Other>> additionalOthers = new ArrayList<>();

        if (caseData.getOthers() == null) {
            additionalOthers.add(Element.<Other>builder()
                .value(Other.builder()
                    .build())
                .build());
        } else {
            //Maintain existing cases in which a first other exists
            if (isNotEmpty(caseData.getOthers().getFirstOther())
                && !caseData.getOthers().getFirstOther().equals(Other.builder().build())) {
                additionalOthers.add(Element.<Other>builder().value(caseData.getOthers().getFirstOther()).build());
            }
            //Iterate through list of others and re-populate confidential contact details
            for (Element<Other> other : caseData.getOthers().getAdditionalOthers()) {
                String contactDetails = other.getValue().getDetailsHidden();
                if (contactDetails != null && contactDetails.equals("Yes")) {
                    if (caseData.getConfidentialOthers() != null) {
                        for (Element<Other> confidentialOther : caseData.getConfidentialOthers()) {
                            if (isSameOtherById(other, confidentialOther)) {
                                additionalOthers.add(confidentialOther);
                                break;
                            }
                        }
                    }
                } else {
                    additionalOthers.add(other);
                }
            }
        }

        return Others.builder()
            .additionalOthers(additionalOthers)
            .build();
    }

    public List<Element<Other>> getConfidentialOthers(CaseData caseData) {
        List<Element<Other>> confidentialOthers = new ArrayList<>();

        for (Element<Other> other : caseData.getOthers().getAdditionalOthers()) {
            if (other.getValue().getDetailsHidden() != null && other.getValue().getDetailsHidden().equals("Yes")) {
                confidentialOthers.add(other);
            }
        }
        return confidentialOthers;
    }

    public Others handleFirstOtherAndHideConfidentialValues(CaseData caseData) {
        List<Element<Other>> additionalOthers = caseData.getOthers().getAdditionalOthers();

        if (isNotEmpty(caseData.getOthers().getFirstOther())) {
            additionalOthers.add(0, Element.<Other>builder().value(caseData.getOthers().getFirstOther()).build());
        }

        additionalOthers = additionalOthers.stream()
            .map(element -> {
                Other.OtherBuilder otherBuilder = element.getValue().toBuilder();

                String detailsHidden = element.getValue().getDetailsHidden();
                if (detailsHidden != null && detailsHidden.equals("Yes")) {
                    otherBuilder
                        .address(null)
                        .telephone(null)
                        .build();
                }

                return Element.<Other>builder()
                    .id(element.getId())
                    .value(otherBuilder.build())
                    .build();
            })
            .collect(toList());

        return Others.builder()
            .additionalOthers(additionalOthers)
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

    private boolean isSameOtherById(Element<Other> other, Element<Other> confidentialOther) {
        return confidentialOther.getId().equals(other.getId());
    }

    private String getName(Other other) {
        return defaultIfNull(other.getName(), "BLANK - Please complete");
    }

    private boolean otherExists(Others others) {
        return others != null && (others.getFirstOther() != null || others.getAdditionalOthers() != null);
    }

}
