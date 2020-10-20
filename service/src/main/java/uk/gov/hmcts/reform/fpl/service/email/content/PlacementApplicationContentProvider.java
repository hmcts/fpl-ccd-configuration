package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementApplicationContentProvider extends AbstractEmailContentProvider {

    public Map<String, Object> buildPlacementApplicationNotificationParameters(CaseData caseData) {

        return ImmutableMap.of(
            "respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()),
            "caseUrl", getCaseUrl(caseData.getId(), "PlacementTab")
        );
    }
}
