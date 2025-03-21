package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiChild;
import uk.gov.hmcts.reform.fpl.model.robotics.Gender;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiAddress;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiSolicitor;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getTelephoneNumber;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
public class CafcassApiChildrenConverter implements CafcassApiCaseDataConverter {
    private static final List<String> SOURCE = List.of("data.children1");

    @Override
    public List<String> getEsSearchSources() {
        return SOURCE;
    }

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
                        .gender(getChildGenderForResponse(childParty.getGender()))
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

    private String getChildGenderForResponse(ChildGender childGender) {
        return switch (childGender) {
            case BOY -> Gender.MALE.toString();
            case GIRL -> Gender.FEMALE.toString();
            case OTHER -> Gender.OTHER.toString();
        };
    }
}
