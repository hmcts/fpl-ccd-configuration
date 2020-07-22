package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Risks;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class RiskAndHarmValidator implements Validator {

    @Override
    public List<String> validate(CaseData caseData) {
        final Risks risks = caseData.getRisks();

        if (isEmpty(risks) || (isEmpty(risks.getNeglect())
            && isEmpty(risks.getSexualAbuse())
            && isEmpty(risks.getPhysicalHarm())
            && isEmpty(risks.getEmotionalHarm()))) {

            return List.of("You need to add risks and harms for children");
        } else {
            return emptyList();
        }

    }
}
