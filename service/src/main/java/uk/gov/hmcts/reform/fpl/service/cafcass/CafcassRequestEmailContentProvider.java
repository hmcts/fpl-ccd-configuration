package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.ChangeOfAddressData;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.HearingVacatedTemplate;

import java.time.format.FormatStyle;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData.SAME_DAY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

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
        CafcassEmailConfiguration::getRecipientForOrder,
            true),

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
        CafcassEmailConfiguration::getRecipientForChangeOfAddress,
            false),


    COURT_BUNDLE("Court bundle",
        (caseData, cafcassData) -> String.format(getSubject(),
                caseData.getFamilyManCaseNumber(),
                "new court bundle"),
        (caseData, cafcassData) ->
            (isEmpty(cafcassData.getHearingDetails()))
                ? "A new court bundle for this case was uploaded to the Public Law Portal"
                : String.format("A new court bundle for this case was uploaded to the Public Law Portal entitled %s",
                cafcassData.getHearingDetails()),
        CafcassEmailConfiguration::getRecipientForCourtBundle,
            true),

    CASE_SUMMARY("Case summary",
        (caseData, cafcassData) -> String.format(getSubject(),
            caseData.getFamilyManCaseNumber(),
            "new case summary"),
        (caseData, cafcassData) ->
            (isEmpty(cafcassData.getHearingDetails()))
                ? "A new case summary for this case was uploaded to the Public Law Portal"
                : String.format("A new case summary for this case was uploaded to the Public Law Portal entitled %s",
                cafcassData.getHearingDetails()),
        CafcassEmailConfiguration::getRecipientForCourtBundle,
        true),

    @Deprecated
    POSITION_STATEMENT_CHILD("Position statement child",
        (caseData, cafcassData) -> String.format(getSubject(),
            caseData.getFamilyManCaseNumber(),
            "new position statement child"),
        (caseData, cafcassData) ->
            String.format("A new position statement (child) for this case was uploaded to the "
                          + "Public Law Portal entitled %s",
                cafcassData.getHearingDetails()),
        CafcassEmailConfiguration::getRecipientForCourtBundle,
        true),

    @Deprecated
    POSITION_STATEMENT_RESPONDENT("Position statement respondent",
        (caseData, cafcassData) -> String.format(getSubject(),
            caseData.getFamilyManCaseNumber(),
            "new position statement respondent"),
        (caseData, cafcassData) ->
            String.format("A new position statement (respondent) for this case was uploaded to the "
                          + "Public Law Portal entitled %s",
                cafcassData.getHearingDetails()),
        CafcassEmailConfiguration::getRecipientForCourtBundle,
        true),

    POSITION_STATEMENT("Position statement",
        (caseData, cafcassData) -> String.format(getSubject(),
            caseData.getFamilyManCaseNumber(),
            "new position statement"),
        (caseData, cafcassData) ->
            "A new position statement for this case was uploaded to the Public Law Portal",
        CafcassEmailConfiguration::getRecipientForCourtBundle,
        true),

    SKELETON_ARGUMENT("Skeleton Argument",
        (caseData, cafcassData) -> String.format(getSubject(),
            caseData.getFamilyManCaseNumber(),
            "new skeleton argument"),
        (caseData, cafcassData) ->
            (isEmpty(cafcassData.getHearingDetails()))
                ? "A skeleton argument for this case was uploaded to the Public Law Portal"
                : String.format("A skeleton argument for this case was uploaded to the Public Law Portal entitled %s",
                cafcassData.getHearingDetails()),
        CafcassEmailConfiguration::getRecipientForCourtBundle,
        true),

    NEW_APPLICATION("New application",
        CafcassRequestEmailContentProvider::getNewApplicationSubject,
        CafcassRequestEmailContentProvider::getNewApplicationMessage,
        CafcassEmailConfiguration::getRecipientForNewApplication,
            true),

    NEW_DOCUMENT("New document",
        (caseData, cafcassData) -> String.format(getSubject(),
        caseData.getFamilyManCaseNumber(),
        cafcassData.getEmailSubjectInfo()),
        (caseData, cafcassData) ->
            String.join("\n\n",
                "Types of documents attached:",
                cafcassData.getDocumentTypes()),
        CafcassEmailConfiguration::getRecipientForNewDocument,
            true),

    ADDITIONAL_DOCUMENT("Additional document",
        (caseData, cafcassData) -> String.format(getSubject(),
        caseData.getFamilyManCaseNumber(),
        cafcassData.getEmailSubjectInfo()),
        (caseData, cafcassData) ->
            String.join("\n\n",
                "Types of documents attached:",
                cafcassData.getDocumentTypes()),
        CafcassEmailConfiguration::getRecipientForAdditionlDocument,
            true),

    LARGE_ATTACHEMENTS("Large document",
        (caseData, cafcassData) -> String.format(getSubject(),
            caseData.getFamilyManCaseNumber(),
            String.join(" - ",
                    "new large document added",
                    cafcassData.getNotificationType())),
        CafcassRequestEmailContentProvider::getLargeApplicationMessage,
        CafcassEmailConfiguration::getRecipientForLargeAttachements,
            false),

    NOTICE_OF_HEARING("Notice of hearing",
        (caseData, cafcassData) ->  String.format(getSubject(),
        caseData.getFamilyManCaseNumber(),
        getHearingDetails(caseData, cafcassData)),
        CafcassRequestEmailContentProvider::getNoticeOfHearingMessage,
        CafcassEmailConfiguration::getRecipientForNoticeOfHearing,
            true),

    PLACEMENT_APPLICATION("Placement Application",
        (caseData, cafcassData) ->  String.format(getSubject(),
            caseData.getFamilyManCaseNumber(),
            "Placement Application"),
        CafcassRequestEmailContentProvider::getPlacementApplicationMessage,
        CafcassEmailConfiguration::getRecipientForNewApplication,
            true),

    PLACEMENT_NOTICE("Notice of Placement",
        (caseData, cafcassData) ->  String.format(getSubject(),
            caseData.getFamilyManCaseNumber(),
            "Notice of Placement"),
        CafcassRequestEmailContentProvider::getPlacementNoticeMessage,
        CafcassEmailConfiguration::getRecipientForNoticeOfHearing,
            true);


    private final String label;
    private final BiFunction<CaseData, CafcassData, String> type;
    private final BiFunction<CaseData, CafcassData, String> content;
    private final Function<CafcassEmailConfiguration, String> recipient;
    private final boolean isGenericSubject;

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
                "Date:", formatLocalDateToString(cafcassData.getHearingDate().toLocalDate(), FormatStyle.LONG),
                System.lineSeparator(),
                "Venue:", cafcassData.getHearingVenue(),
                System.lineSeparator(),
                "Pre-hearing time:", cafcassData.getPreHearingTime(),
                System.lineSeparator(),
                "Hearing time:", cafcassData.getHearingTime()
        );
    }

    private static String getPlacementApplicationMessage(CaseData caseData, CafcassData cafcassData) {
        return String.join(" ",
            "There's a new Placement Application for:",
            System.lineSeparator(),
            cafcassData.getFirstRespondentName(), caseData.getFamilyManCaseNumber(),
            System.lineSeparator(), System.lineSeparator(),
            "Child name:", cafcassData.getPlacementChildName());
    }

    private static String getPlacementNoticeMessage(CaseData caseData, CafcassData cafcassData) {
        return String.join(" ",
            "There's a new notice of placement for:",
            System.lineSeparator(),
            cafcassData.getFirstRespondentName(), caseData.getFamilyManCaseNumber(),
            System.lineSeparator(), System.lineSeparator(),
            "Child name:", cafcassData.getPlacementChildName());
    }

    private static String getVacateOfHearingMessage(CaseData caseData, CafcassData cafcassData) {
        HearingVacatedTemplate hearingVacatedTemplate = (HearingVacatedTemplate) cafcassData;

        return String.join(" ",
            String.join(", ",
                hearingVacatedTemplate.getHearingDateFormatted(), hearingVacatedTemplate.getHearingVenue(),
                hearingVacatedTemplate.getHearingTime()),
            System.lineSeparator(),
            "This hearing has been vacated on", hearingVacatedTemplate.getVacatedDate(), "due to",
            hearingVacatedTemplate.getVacatedReason(), "and will", hearingVacatedTemplate.getRelistAction());
    }

    private static String getSubject() {
        return "Court Ref. %s.- %s";
    }
}
