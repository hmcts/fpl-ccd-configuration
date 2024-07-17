package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiRespondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiAddress;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiSolicitor;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getTelephoneNumber;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;

@Component
public class CafcassApiRespondentsConverter implements CafcassApiCaseDataConverter {
    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.respondents(getCafcassApiRespondents(caseData));
    }

    private List<CafcassApiRespondent> getCafcassApiRespondents(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondents1()).orElse(List.of()).stream()
            .map(Element::getValue)
            .map(respondent -> {
                RespondentParty respondentParty = respondent.getParty();
                return CafcassApiRespondent.builder()
                    .firstName(respondentParty.getFirstName())
                    .lastName(respondentParty.getLastName())
                    .gender(respondentParty.getGender())
                    .addressKnown(isYes(respondentParty.getAddressKnow()))
                    .addressUnknownReason(respondentParty.getAddressNotKnowReason())
                    .address(getCafcassApiAddress(respondentParty.getAddress()))
                    .dateOfBirth(respondentParty.getDateOfBirth())
                    .telephoneNumber(getTelephoneNumber(respondentParty.getTelephoneNumber()))
                    .litigationIssues(respondentParty.getLitigationIssues())
                    .litigationIssuesDetails(respondentParty.getLitigationIssuesDetails())
                    .contactDetailsHidden(respondentParty.getContactDetailsHidden())
                    .contactDetailsHiddenReason(respondentParty.getContactDetailsHiddenReason())
                    .relationshipToChild(respondentParty.getRelationshipToChild())
                    .solicitor(getCafcassApiSolicitor(respondent.getSolicitor()))
                    .build();
            })
            .toList();
    }
}
