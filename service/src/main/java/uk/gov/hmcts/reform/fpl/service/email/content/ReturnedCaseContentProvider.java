package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentFullName;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnedCaseContentProvider extends AbstractEmailContentProvider {
    private final ObjectMapper mapper;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    public ReturnedCaseTemplate buildNotificationParameters(CaseDetails caseDetails, String localAuthority) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        ReturnApplication returnApplication = caseData.getReturnApplication();
        ReturnedCaseTemplate returnedCaseTemplate = new ReturnedCaseTemplate();

        returnedCaseTemplate.setLocalAuthority(
            localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthority));

        returnedCaseTemplate.setRespondentFullName(getFirstRespondentFullName(caseData.getRespondents1()));
        returnedCaseTemplate.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        returnedCaseTemplate.setFamilyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""));
        returnedCaseTemplate.setReturnedReasons(returnApplication.getFormattedReturnReasons());
        returnedCaseTemplate.setReturnedNote(returnApplication.getNote());
        returnedCaseTemplate.setCaseUrl(getCaseUrl(caseDetails.getId()));

        return returnedCaseTemplate;
    }
}
