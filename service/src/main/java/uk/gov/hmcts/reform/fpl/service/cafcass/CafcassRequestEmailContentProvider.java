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
    ORDER("Order",
        (caseData, cafcassData) -> String.format(getSubject(),
                caseData.getFamilyManCaseNumber(),
                "new order"),
        (caseData, cafcassData) ->
            String.format("A new order for this case was uploaded to the Public Law Portal entitled %s",
                cafcassData.getDocumentName()),
        CafcassEmailConfiguration::getRecipientForOrder),

    CHANGE_OF_ADDRESS("Change of address",
        (caseData, cafcassData) -> String.format(getSubject(),
        caseData.getFamilyManCaseNumber(),
        "change of address" + (((ChangeOfAddressData) cafcassData).isRespondents()
                ? " - respondent solicitor" : (((ChangeOfAddressData) cafcassData).isChildren()
                ? " - child solicitor" : ""))),
        (caseData, cafcassData) ->
                String.format("A change of address has been added to this case "
                                + "which was uploaded to the Public Law Portal entitled [%s].",
                        caseData.getCaseName()),
        CafcassEmailConfiguration::getRecipientForChangeOfAddress),


    COURT_BUNDLE("Court bundle",
        (caseData, cafcassData) -> String.format(getSubject(),
                caseData.getFamilyManCaseNumber(),
                "new court bundle"),
        (caseData, cafcassData) ->
            String.format("A new court bundle for this case was uploaded to the Public Law Portal entitled %s",
                cafcassData.getHearingDetails()),
        CafcassEmailConfiguration::getRecipientForCourtBundle),

    NEW_APPLICATION("New application",
        CafcassRequestEmailContentProvider::getNewApplicationSubject,
        CafcassRequestEmailContentProvider::getNewApplicationMessage,
        CafcassEmailConfiguration::getRecipientForNewApplication),

    NEW_DOCUMENT("New document",
        (caseData, cafcassData) -> String.format(getSubject(),
        caseData.getFamilyManCaseNumber(),
        cafcassData.getEmailSubjectInfo()),
        (caseData, cafcassData) ->
            String.join("\n\n",
                "Types of documents attached:",
                cafcassData.getDocumentTypes()),
        CafcassEmailConfiguration::getRecipientForNewDocument),

    ADDITIONAL_DOCUMENT("Additional document",
        (caseData, cafcassData) -> String.format(getSubject(),
        caseData.getFamilyManCaseNumber(),
        cafcassData.getEmailSubjectInfo()),
        (caseData, cafcassData) ->
            String.join("\n\n",
                "Types of documents attached:",
                cafcassData.getDocumentTypes()),
        CafcassEmailConfiguration::getRecipientForAdditionlDocument),

    LARGE_ATTACHEMENTS("Large document",
        (caseData, cafcassData) -> String.format(getSubject(),
            caseData.getFamilyManCaseNumber(),
            String.join(" - ",
                    "new large document added",
                    cafcassData.getNotificationType())),
        CafcassRequestEmailContentProvider::getLargeApplicationMessage,
        CafcassEmailConfiguration::getRecipientForLargeAttachements),

    NOTICE_OF_HEARING("Notice of hearing",
        (caseData, cafcassData) ->  String.format(getSubject(),
        caseData.getFamilyManCaseNumber(),
        getHearingDetails(caseData, cafcassData)),
        CafcassRequestEmailContentProvider::getNoticeOfHearingMessage,
        CafcassEmailConfiguration::getRecipientForNoticeOfHearing);



    private final String label;
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

    private static String getHearingDetails(CaseData caseData, CafcassData cafcassData) {
        return String.join(" ",
                "New",
                cafcassData.getHearingType(),
                "hearing",
                cafcassData.getEldestChildLastName(),
                "- notice of hearing");
    }

    private static String getNoticeOfHearingMessage(CaseData caseData, CafcassData cafcassData) {
        return String.join(" ",
                "There’s a new", cafcassData.getHearingType(), "hearing for:",
                System.lineSeparator(),
                cafcassData.getFirstRespondentName(),  caseData.getFamilyManCaseNumber(),
                System.lineSeparator(), System.lineSeparator(),
                "Hearing details",
                System.lineSeparator(),
                "Date:", cafcassData.getHearingDate(),
                System.lineSeparator(),
                "Venue:", cafcassData.getHearingVenue(),
                System.lineSeparator(),
                "Pre-hearing time:", cafcassData.getPreHearingTime(),
                System.lineSeparator(),
                "Hearing time:", cafcassData.getHearingTime()
        );
    }

    private static String getSubject() {
        return "Court Ref. %s.- %s";
    }
}
