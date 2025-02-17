package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiOther;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.convertAdrewssKnown;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiAddress;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getGenderForApiResponse;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
public class CafcassApiOthersConverter implements CafcassApiCaseDataConverter {
    private static final List<String> SOURCE = List.of("data.others");

    @Override
    public List<String> getEsSearchSources() {
        return SOURCE;
    }

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.others(getCafcassApiOthers(caseData));
    }

    private List<CafcassApiOther> getCafcassApiOthers(CaseData caseData) {
        return Optional.ofNullable(caseData.getOthers())
            .map(others -> Stream.concat(Optional.ofNullable(others.getFirstOther()).stream(),
                    unwrapElements(others.getAdditionalOthers()).stream())
                .filter(Objects::nonNull)
                .map(other -> CafcassApiOther.builder()
                    .name(other.getName())
                    .dateOfBirth(other.getDateOfBirth())
                    .gender(getGenderForApiResponse(other.getGender()))
                    .genderIdentification(other.getGenderIdentification())
                    .birthPlace(other.getBirthPlace())
                    .addressKnown(convertAdrewssKnown(other.getAddressKnowV2()))
                    .addressUnknownReason(other.getAddressNotKnowReason())
                    .address(getCafcassApiAddress(other.getAddress()))
                    .telephone(other.getTelephone())
                    .litigationIssues(other.getLitigationIssues())
                    .litigationIssuesDetails(other.getLitigationIssuesDetails())
                    .detailsHidden(isYes(other.getDetailsHidden()))
                    .detailsHiddenReason(other.getDetailsHiddenReason())
                    .build())
                .toList()
            )
            .orElse(List.of());
    }
}
