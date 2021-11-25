package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData;

import java.util.function.BiFunction;
import java.util.function.Function;

import static uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData.SAME_DAY;

@Getter
@RequiredArgsConstructor
public enum CafcassRequestEmailContentProvider {
    ORDER((caseData, cafcassData) -> String.format(Constants.SUBJECT,
                caseData.getFamilyManCaseNumber(),
                "new order"),
        (caseData, cafcassData) ->
            String.format("A new order for this case was uploaded to the Public Law Portal entitled %s",
                cafcassData.getDocumentName()),
        CafcassEmailConfiguration::getRecipientForOrder),

    COURT_BUNDLE((caseData, cafcassData) -> String.format(Constants.SUBJECT,
                caseData.getFamilyManCaseNumber(),
                "new court bundle"),
        (caseData, cafcassData) ->
            String.format("A new court bundle for this case was uploaded to the Public Law Portal entitled %s",
                cafcassData.getHearingDetails()),
        CafcassEmailConfiguration::getRecipientForCourtBundle),

    NEW_APPLICATION(CafcassRequestEmailContentProvider::getNewApplicationSubject,
        CafcassRequestEmailContentProvider::getNewApplicationMessage,
        CafcassEmailConfiguration::getRecipientForNewApplication),

    NEW_DOCUMENT((caseData, cafcassData) -> String.format(Constants.SUBJECT,
        caseData.getFamilyManCaseNumber(),
        "new document"),
        (caseData, cafcassData) ->
            String.join("\n\n",
                "Types of documents attached:",
                cafcassData.getDocumentTypes()),
        CafcassEmailConfiguration::getRecipientForNewDocument);


    private final BiFunction<CaseData, CafcassData, String> type;
    private final BiFunction<CaseData, CafcassData, String> content;
    private final Function<CafcassEmailConfiguration, String> recipient;

    private static String getNewApplicationSubject(CaseData caseData, CafcassData cafcassData) {
        String timeFrame = "Urgent application – same day hearing";

        if (!SAME_DAY.equals(cafcassData.getTimeFrameValue())) {
            timeFrame = String.join(" ",
                "Application received – hearing",
                cafcassData.getTimeFrameValue());
        }
        return String.join(", ", timeFrame, cafcassData.getEldestChildLastName());
    }

    private static String getNewApplicationMessage(CaseData caseData, CafcassData cafcassData) {
        String localAuthority = String.join(" ", cafcassData.getLocalAuthourity(), "has made a new application for:");

        String timeFrame = cafcassData.getTimeFrameValue();
        if (cafcassData.isTimeFramePresent()) {
            timeFrame = String.join(": ","Hearing date requested", cafcassData.getTimeFrameValue());
        }

        String respondent = String.join(": ","Respondent's surname", cafcassData.getFirstRespondentName());
        String caseNumber = String.join(": ","CCD case number", caseData.getId().toString());

        return String.join("\n\n",
            localAuthority,
            cafcassData.getOrdersAndDirections(),
            timeFrame,
            respondent,
            caseNumber);
    }

    private static class Constants {
        private static final String SUBJECT = "Court Ref. %s.- %s";
    }
}
