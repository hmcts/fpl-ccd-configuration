package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.PartyAddedNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PartyAddedToCaseContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public PartyAddedNotifyData getPartyAddedToCaseNotificationParameters(CaseData caseData,
                                                                          RepresentativeServingPreferences preference) {

        return PartyAddedNotifyData.builder()
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .firstRespondentLastName(getFirstRespondentLastName(caseData))
            .caseUrl(DIGITAL_SERVICE == preference ? getCaseUrl(caseData.getId()) : null)
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .build();
    }
}
