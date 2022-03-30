package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.ChangeOfAddressData;

import java.util.function.BiFunction;
import java.util.function.Function;

import static uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData.SAME_DAY;

@Getter
@RequiredArgsConstructor
public enum CafcassRequestEmailContentProvider {
    ORDER((caseData, cafcassData) -> String.format(getSubject(),
                caseData.getFamilyManCaseNumber(),
                "new order"),
        (caseData, cafcassData) ->
            String.format("A new order for this case was uploaded to the Public Law Portal entitled %s",
                cafcassData.getDocumentName()),
        CafcassEmailConfiguration::getRecipientForOrder),

    CHANGE_OF_ADDRESS((caseData, cafcassData) -> String.format(getSubject(),
        caseData.getFamilyManCaseNumber(),
        "change of address" + (((ChangeOfAddressData) cafcassData).isRespondents()
            ? " - respondent solicitor" : (((ChangeOfAddressData) cafcassData).isChildren()
            ? " - child solicitor" : ""))),
        (caseData, cafcassData) ->
            String.format("A change of address has been added to this case "
                    + "which was uploaded to the Public Law Portal entitled [%s].",
                caseData.getCaseName()),
        CafcassEmailConfiguration::getRecipientForChangeOfAddress),

    COURT_BUNDLE((caseData, cafcassData) -> String.format(getSubject(),
                caseData.getFamilyManCaseNumber(),
                "new court bundle"),
        (caseData, cafcassData) ->
            String.format("A new court bundle for this case was uploaded to the Public Law Portal entitled %s",
                cafcassData.getHearingDetails()),
        CafcassEmailConfiguration::getRecipientForCourtBundle),

    NEW_APPLICATION(CafcassRequestEmailContentProvider::getNewApplicationSubject,
        CafcassRequestEmailContentProvider::getNewApplicationMessage,
        CafcassEmailConfiguration::getRecipientForNewApplication),

    NEW_DOCUMENT((caseData, cafcassData) -> String.format(getSubject(),
        caseData.getFamilyManCaseNumber(),
        cafcassData.getEmailSubjectInfo()),
        (caseData, cafcassData) ->
            String.join("\n\n",
                "Types of documents attached:",
                cafcassData.getDocumentTypes()),
        CafcassEmailConfiguration::getRecipientForNewDocument),

    ADDITIONAL_DOCUMENT((caseData, cafcassData) -> String.format(getSubject(),
        caseData.getFamilyManCaseNumber(),
        cafcassData.getEmailSubjectInfo()),
        (caseData, cafcassData) ->
            String.join("\n\n",
                "Types of documents attached:",
                cafcassData.getDocumentTypes()),
        CafcassEmailConfiguration::getRecipientForAdditionlDocument),

    LARGE_ATTACHEMENTS((caseData, cafcassData) -> String.format(getSubject(),
            caseData.getFamilyManCaseNumber(),
            "new large document added"),
        CafcassRequestEmailContentProvider::getLargeApplicationMessage,
        CafcassEmailConfiguration::getRecipientForLargeAttachements);

    private final BiFunction<CaseData, CafcassData, String> type;
    private final BiFunction<CaseData, CafcassData, String> content;
    private final Function<CafcassEmailConfiguration, String> recipient;

    private static String getNewApplicationSubject(CaseData caseData, CafcassData cafcassData) {
        String timeFrame = "Urgent application – same day hearing";

        if (!SAME_DAY.equals(cafcassData.getTimeFrameValue())) {
            timeFrame = "Application received";
            if (!"".equals(cafcassData.getTimeFrameValue())) {
                timeFrame = String.join(" ",
                        timeFrame,
                        "– hearing",
                        cafcassData.getTimeFrameValue());
            }
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

    private static String getLargeApplicationMessage(CaseData caseData, CafcassData cafcassData) {
        return String.join("",
                "Large document(s) for this case was uploaded to the Public Law Portal entitled ",
                cafcassData.getDocumentName(),
                ". As this could not be sent by email you will need to download it from ",
                "the Portal using this link.",
                System.lineSeparator(),
                cafcassData.getCaseUrl());
    }

    private static String getSubject() {
        return "Court Ref. %s.- %s";
    }
}
