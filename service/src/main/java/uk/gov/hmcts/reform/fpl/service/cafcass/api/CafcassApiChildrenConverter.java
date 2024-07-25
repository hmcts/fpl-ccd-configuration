package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiChild;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiAddress;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiSolicitor;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getTelephoneNumber;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class CafcassApiChildrenConverter implements CafcassApiCaseDataConverter {
    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.children(getCafcassApiChild(caseData));
    }

    private List<CafcassApiChild> getCafcassApiChild(CaseData caseData) {
        return unwrapElements(caseData.getChildren1()).stream()
            .map(child -> {
                CafcassApiChild.CafcassApiChildBuilder builder =  CafcassApiChild.builder()
                    .solicitor(getCafcassApiSolicitor(child.getSolicitor()));
                ChildParty childParty = child.getParty();
                if (isNotEmpty(childParty)) {
                    builder = builder.firstName(childParty.getFirstName())
                        .lastName(childParty.getLastName())
                        .dateOfBirth(childParty.getDateOfBirth())
                        .gender(childParty.getGender().toString())
                        .genderIdentification(childParty.getGenderIdentification())
                        .livingSituation(childParty.getLivingSituation())
                        .livingSituationDetails(childParty.getLivingSituationDetails())
                        .address(getCafcassApiAddress(childParty.getAddress()))
                        .careAndContactPlan(childParty.getCareAndContactPlan())
                        .detailsHidden(isYes(childParty.getDetailsHidden()))
                        .socialWorkerName(childParty.getSocialWorkerName())
                        .socialWorkerTelephoneNumber(getTelephoneNumber(childParty.getSocialWorkerTelephoneNumber()))
                        .additionalNeeds(isYes(childParty.getAdditionalNeeds()))
                        .additionalNeedsDetails(childParty.getAdditionalNeedsDetails())
                        .litigationIssues(childParty.getLitigationIssues())
                        .litigationIssuesDetails(childParty.getLitigationIssuesDetails())
                        .fathersResponsibility(childParty.getFathersResponsibility());
                }
                return builder.build();
            })
            .toList();
    }
}
