package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class FactorsAffectingParentingValidator implements Validator {

    @Override
    public List<String> validate(CaseData caseData) {
        final FactorsParenting factors = caseData.getFactorsParenting();

        if (isEmpty(factors) || (isEmpty(factors.getAlcoholDrugAbuse())
            && isEmpty(factors.getDomesticViolence())
            && isEmpty(factors.getAnythingElse()))) {

            return List.of("You need to add factors affecting parenting");
        } else {
            return emptyList();
        }
    }
}
