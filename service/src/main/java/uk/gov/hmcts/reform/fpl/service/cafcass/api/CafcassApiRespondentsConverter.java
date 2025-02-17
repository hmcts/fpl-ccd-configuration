package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiRespondent;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.convertAdrewssKnown;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiAddress;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiSolicitor;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getGenderForApiResponse;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getTelephoneNumber;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
public class CafcassApiRespondentsConverter implements CafcassApiCaseDataConverter {
    private static final List<String> SOURCE = List.of("data.respondents1");

    @Override
    public List<String> getEsSearchSources() {
        return SOURCE;
    }

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.respondents(getCafcassApiRespondents(caseData));
    }

    private List<CafcassApiRespondent> getCafcassApiRespondents(CaseData caseData) {
        return unwrapElements(caseData.getRespondents1()).stream()
            .map(respondent -> {
                CafcassApiRespondent.CafcassApiRespondentBuilder builder = CafcassApiRespondent.builder()
                    .solicitor(getCafcassApiSolicitor(respondent.getSolicitor()));
                RespondentParty respondentParty = respondent.getParty();
                if (isNotEmpty(respondentParty)) {
                    builder = builder.firstName(respondentParty.getFirstName())
                        .lastName(respondentParty.getLastName())
                        .gender(getGenderForApiResponse(respondentParty.getGender()))
                        .genderIdentification(respondentParty.getGenderIdentification())
                        .addressKnown(convertAdrewssKnown(respondentParty.getAddressKnow()))
                        .addressUnknownReason(respondentParty.getAddressNotKnowReason())
                        .address(getCafcassApiAddress(respondentParty.getAddress()))
                        .dateOfBirth(respondentParty.getDateOfBirth())
                        .telephoneNumber(getTelephoneNumber(respondentParty.getTelephoneNumber()))
                        .litigationIssues(respondentParty.getLitigationIssues())
                        .litigationIssuesDetails(respondentParty.getLitigationIssuesDetails())
                        .contactDetailsHidden(isYes(respondentParty.getContactDetailsHidden()))
                        .contactDetailsHiddenReason(respondentParty.getContactDetailsHiddenReason())
                        .relationshipToChild(respondentParty.getRelationshipToChild());
                }
                return builder.build();
            })
            .toList();
    }
}
