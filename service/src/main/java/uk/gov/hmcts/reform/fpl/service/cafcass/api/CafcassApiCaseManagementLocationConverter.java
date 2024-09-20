package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseManagementLocation;

@Service
public class CafcassApiCaseManagementLocationConverter implements CafcassApiCaseDataConverter {
    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.caseManagementLocation(getCafcassApiCaseManagementLocation(caseData));
    }

    private CafcassApiCaseManagementLocation getCafcassApiCaseManagementLocation(CaseData caseData) {
        CaseLocation caseLocation = caseData.getCaseManagementLocation();
        if (caseLocation != null) {
            return CafcassApiCaseManagementLocation.builder()
                .region(caseLocation.getRegion()).baseLocation(caseLocation.getBaseLocation())
                .build();
        }

        return null;
    }
}
