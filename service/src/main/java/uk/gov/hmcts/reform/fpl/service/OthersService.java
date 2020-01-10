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

    public List<Element<Other>> getAllConfidentialOthers(CaseData caseData) {
        //Gets all others from case data and returns list of confidential others first other and additional other
        final List <Element<Other>> confidentialOthers = new ArrayList<>();

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

}
