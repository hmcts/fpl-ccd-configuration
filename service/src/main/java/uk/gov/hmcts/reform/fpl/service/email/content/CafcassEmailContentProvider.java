package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CafcassEmailContentProvider extends SharedNotifyContentProvider {
    private static final String LIST = "•";
    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final CafcassLookupConfiguration cafcassLookup;


    public SubmitCaseCafcassTemplate buildCafcassSubmissionNotification(CaseData caseData) {

        SubmitCaseCafcassTemplate template = buildNotifyTemplate(SubmitCaseCafcassTemplate.builder().build(), caseData);

        template.setCafcass(cafcassLookup.getCafcass(caseData.getCaseLaOrRelatingLa()).getName());
        template.setLocalAuthority(nonNull(caseData.getCaseLocalAuthority())
            ? laNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority())
            : caseData.getApplicantName().orElse(null));
        template.setDocumentLink(linkToAttachedDocument(caseData.getC110A().getSubmittedForm()));

        return template;
    }

    public NewApplicationCafcassData buildCafcassSubmissionSendGridData(CaseData caseData) {

        String ordersAndDirections = buildOrdersAndDirections(caseData.getOrders()).stream()
            .map(order -> String.join(" ", LIST, order))
            .collect(Collectors.joining("\n"));

        Optional<String> timeFrame = Optional.ofNullable(caseData.getHearing())
            .map(hearing -> nonNull(hearing.getHearingUrgencyType())
                ? hearing.getHearingUrgencyType().getLabel() : hearing.getTimeFrame())
            .filter(StringUtils::isNotBlank);

        String eldestChildLastName = helper.getEldestChildLastName(caseData.getAllChildren());

        return NewApplicationCafcassData.builder()
            .localAuthourity(caseData.getApplicantName().orElse("An applicant"))
            .ordersAndDirections(ordersAndDirections)
            .timeFramePresent(timeFrame.isPresent())
            .timeFrameValue(timeFrame.map(StringUtils::uncapitalize).orElse(""))
            .eldestChildLastName(eldestChildLastName)
            .firstRespondentName(getFirstRespondentLastName(caseData.getRespondents1()))
            .build();
    }
}
