package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiChild;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiAddress;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiSolicitor;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getTelephoneNumber;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;

@Component
public class CafcassApiChildrenConverter implements CafcassApiCaseDataConverter {
    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.children(getCafcassApiChild(caseData));
    }

    private List<CafcassApiChild> getCafcassApiChild(CaseData caseData) {
        return Optional.ofNullable(caseData.getChildren1()).orElse(List.of()).stream()
            .map(Element::getValue)
            .map(child -> {
                ChildParty childParty = child.getParty();
                return CafcassApiChild.builder()
                    .firstName(childParty.getFirstName())
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
                    .solicitor(getCafcassApiSolicitor(child.getSolicitor()))
                    .fathersResponsibility(childParty.getFathersResponsibility())
                    .build();
            })
            .toList();
    }
}
