package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PartyAddedToCaseContentProvider extends AbstractEmailContentProvider {

    public Map<String, Object> getPartyAddedToCaseNotificationParameters(
        CaseData caseData,
        RepresentativeServingPreferences servingPreference) {

        ImmutableMap.Builder<String, Object> notificationParams =
            buildPartyAddedToCaseCommonNotificationParams(caseData);

        if (servingPreference == DIGITAL_SERVICE) {
            notificationParams.put("caseUrl", getCaseUrl(caseData.getId()));
        }
        return notificationParams.build();
    }

    private ImmutableMap.Builder<String, Object> buildPartyAddedToCaseCommonNotificationParams(
        final CaseData caseData) {

        return ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .put("familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), ""));
    }
}
