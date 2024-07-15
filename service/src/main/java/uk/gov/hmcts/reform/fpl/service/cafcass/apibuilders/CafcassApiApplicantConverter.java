package uk.gov.hmcts.reform.fpl.service.cafcass.apibuilders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiApplicant;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiColleague;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Component
public class CafcassApiApplicantConverter implements CafcassApiCaseDataConverter{
    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData, CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.applicants(getCafcassApiApplicant(caseData));
    }

    private List<CafcassApiApplicant> getCafcassApiApplicant(CaseData caseData) {
        return caseData.getLocalAuthorities().stream()
            .map(applicantElement -> {
                LocalAuthority applicant = applicantElement.getValue();

                return CafcassApiApplicant.builder()
                    .id(applicantElement.getId().toString())
                    .name(applicant.getName())
                    .email(applicant.getEmail())
                    .phone(applicant.getPhone())
                    .address(applicant.getAddress())
                    .designated(YES.equals(YesNo.valueOf(applicant.getDesignated())))
                    .colleagues(getColleagues(applicant))
                    .build();
            })
            .toList();
    }

    private List<CafcassApiColleague> getColleagues(LocalAuthority applicant) {
        return applicant.getColleagues().stream()
            .map(Element::getValue)
            .map(colleague -> CafcassApiColleague.builder()
                .role(colleague.getRole().toString())
                .title(colleague.getTitle())
                .email(colleague.getEmail())
                .phone(colleague.getPhone())
                .fullName(colleague.getFullName())
                .mainContact(YES.equals(YesNo.valueOf(colleague.getMainContact())))
                .notificationRecipient(YES.equals(YesNo.valueOf(colleague.getNotificationRecipient())))
                .build())
            .toList();
    }
}
