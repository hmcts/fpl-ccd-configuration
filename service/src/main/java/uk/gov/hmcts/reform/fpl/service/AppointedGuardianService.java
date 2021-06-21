package uk.gov.hmcts.reform.fpl.service;

import com.mchange.v2.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AppointedGuardianService {

    public static String getAppointedGuardiansLabel(List<Element<Respondent>> respondents, List<Element<Other>> others) {
        if (isEmpty(respondents) && isEmpty(others)) {
            return "No respondents or others on the case";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < CollectionUtils.size(respondents); i++) {
            Respondent respondent = respondents.get(i).getValue();

            builder.append(String.format("Respondent %d: %s", i + 1, respondent.getParty().getFullName()));
            builder.append("\n");
        }

        for (int i = 0; i < CollectionUtils.size(others); i++) {
            Other other = others.get(i).getValue();

            builder.append(String.format("Other %d: %s", i + 1, other.getName()));
            builder.append("\n");
        }

        return builder.toString();
    }

    public static String getAppointedGuardiansNames(List<Element<Respondent>> respondents, List<Element<Other>> others) {

        StringBuilder builder = new StringBuilder();
        boolean semaphore = true;
        boolean hasMultipleGuardiansGrammer = false;

        for (int i = 0; i < CollectionUtils.size(respondents); i++) {
            Respondent respondent = respondents.get(i).getValue();

            if (i >= 1) {
                hasMultipleGuardiansGrammer = true;
                builder.append(String.format(", %s", respondent.getParty().getFullName()));
            } else {
                builder.append(String.format("%s", respondent.getParty().getFullName()));
            }
        }

        for (int i = 0; i < CollectionUtils.size(others); i++) {
            Other other = others.get(i).getValue();

            boolean respondentAlreadySelected = builder.length() > 1 && semaphore;

            if (i >= 1) {
                hasMultipleGuardiansGrammer = true;
                builder.append(String.format(", %s", other.getName()));
            } else {
                if (respondentAlreadySelected){
                    addComma(builder, semaphore);
                }
                builder.append(String.format("%s", other.getName()));
            }
        }

        builder.append(" ");

        appendChildGrammerVerb(builder, hasMultipleGuardiansGrammer);

        return builder.toString();
    }

    private static void addComma(StringBuilder builder, boolean semaphore) {
        semaphore = false;
        builder.append(", ");
    }

    private static void appendChildGrammerVerb(StringBuilder builder, Boolean hasMultipleGuardiansGrammer) {
        if (hasMultipleGuardiansGrammer) {
            builder.append("are");
        } else {
            builder.append("is");
        }
    }

}
