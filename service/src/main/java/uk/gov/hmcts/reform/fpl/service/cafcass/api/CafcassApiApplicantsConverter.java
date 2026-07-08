package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiApplicant;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiColleague;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiAddress;
import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;

@Service
public class CafcassApiApplicantsConverter implements CafcassApiCaseDataConverter {
    private static final List<String> SOURCE = List.of("data.localAuthorities");

    @Override
    public List<String> getEsSearchSources() {
        return SOURCE;
    }

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.applicants(getCafcassApiApplicant(caseData));
    }

    private List<CafcassApiApplicant> getCafcassApiApplicant(CaseData caseData) {
        return Optional.ofNullable(caseData.getLocalAuthorities()).orElse(List.of()).stream()
            .map(applicantElement -> {
                LocalAuthority applicant = applicantElement.getValue();

                return CafcassApiApplicant.builder()
                    .id(applicantElement.getId().toString())
                    .name(applicant.getName())
                    .email(applicant.getEmail())
                    .phone(applicant.getPhone())
                    .address(getCafcassApiAddress(applicant.getAddress()))
                    .designated(isYes(applicant.getDesignated()))
                    .colleagues(applicant.getColleagues().stream()
                        .map(Element::getValue)
                        .map(colleague -> CafcassApiColleague.builder()
                            .role(Optional.ofNullable(colleague.getRole()).map(ColleagueRole::toString).orElse(null))
                            .title(colleague.getTitle())
                            .email(colleague.getEmail())
                            .phone(colleague.getPhone())
                            .fullName(colleague.getFullName())
                            .mainContact(isYes(colleague.getMainContact()))
                            .notificationRecipient(isYes(colleague.getNotificationRecipient()))
                            .build())
                        .toList())
                    .build();
            })
            .toList();
    }
}
