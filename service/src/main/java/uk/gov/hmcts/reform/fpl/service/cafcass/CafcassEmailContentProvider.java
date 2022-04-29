package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.LargeFilesNotificationData;
import uk.gov.hmcts.reform.fpl.model.cafcass.UrgentHearingOrderAndNopData;

import java.util.function.BiFunction;

@Getter
@RequiredArgsConstructor
public enum CafcassEmailContentProvider {

    URGENT_HEARING_ORDER_AND_NOP((caseData, cafcassData) -> String.format(
        "Urgent hearing order and notice of proceedings issued, %s",
        ((UrgentHearingOrderAndNopData) cafcassData).getLeadRespondentsName()),
        CafcassEmailContentProvider::getUrgentHearingOrderAndNopMessage,
        CafcassEmailContentProvider::getUrgentHearingOrderAndNopMessageForLargeFile,
        (configuration, caseData) -> {
            return configuration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();
        }
    );

    private final BiFunction<CaseData, CafcassData, String> type;
    private final BiFunction<CaseData, CafcassData, String> content;
    private final BiFunction<CaseData, CafcassData, String> largeFileContent;
    private final BiFunction<CafcassLookupConfiguration, CaseData, String> recipient;

    private static String getUrgentHearingOrderAndNopMessage(CaseData caseData, CafcassData cafcassData) {
        UrgentHearingOrderAndNopData urgentHearingOrderAndNopData = (UrgentHearingOrderAndNopData) cafcassData;

        String callout = urgentHearingOrderAndNopData.getCallout();

        return String.join("\n\n",
            String.format("An urgent hearing order and notice of proceedings have been issued for:\n%s",
                callout),
            "Next steps",
            "You should now check the order to see your directions and compliance dates.",
            "HM Courts & Tribunals Service",
            "Do not reply to this email. If you need to contact us, call 0330 808 4424 or "
                + "email contactfpl@justice.gov.uk");
    }

    private static String getUrgentHearingOrderAndNopMessageForLargeFile(CaseData caseData, CafcassData cafcassData) {
        LargeFilesNotificationData largeFilesNotificationData  = (LargeFilesNotificationData) cafcassData;
        UrgentHearingOrderAndNopData urgentHearingOrderAndNopData = (UrgentHearingOrderAndNopData)
            largeFilesNotificationData.getOriginalCafcassData();

        String callout = urgentHearingOrderAndNopData.getCallout();

        return String.join("\n\n",
            String.format("An urgent hearing order and notice of proceedings have been issued for:\n%s",
                callout),
            "Next steps",
            "You should now check the order to see your directions and compliance dates.",
            "As the file exceeds the size limit that could not be sent by email you will need to download it from "
                + "the Portal using this link.",
            largeFilesNotificationData.getCaseUrl() + "\n",
            "HM Courts & Tribunals Service",
            "Do not reply to this email. If you need to contact us, call 0330 808 4424 or "
                + "email contactfpl@justice.gov.uk");
    }

}
