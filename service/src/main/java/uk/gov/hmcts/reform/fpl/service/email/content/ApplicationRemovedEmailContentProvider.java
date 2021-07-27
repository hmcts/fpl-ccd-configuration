package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicationRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCalloutWithNextHearing;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApplicationRemovedEmailContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public ApplicationRemovedNotifyData getNotifyData(final CaseData caseData, final AdditionalApplicationsBundle removedApplication) {
        return ApplicationRemovedNotifyData.builder()
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .caseId(caseData.getId().toString())
            .c2Filename(getFilename(removedApplication))
            .removalDate(formatLocalDateToString(LocalDateTime.now().toLocalDate(), DATE_TIME_AT))
            .reason(removedApplication.getRemovalReason().toLowerCase())
            .applicantName(getApplicantName(removedApplication))
            .applicationFee("1")
            .build();
    }

    private String getApplicantName(AdditionalApplicationsBundle removedApplication) {
        if(!isEmpty(removedApplication.getC2DocumentBundle())) {
           return removedApplication.getC2DocumentBundle().getApplicantName();
        }

        if (!isEmpty(removedApplication.getOtherApplicationsBundle())) {
           return removedApplication.getOtherApplicationsBundle().getApplicantName();
        }

        return "";
    }

    private String getFilename(AdditionalApplicationsBundle removedApplication) {
        String c2DocumentName = "";
        String otherDocumentName = "";
        if(!isEmpty(removedApplication.getC2DocumentBundle())) {
            c2DocumentName = removedApplication.getC2DocumentBundle().getDocument().getFilename();
        }

        if (!isEmpty(removedApplication.getOtherApplicationsBundle())) {
            otherDocumentName = removedApplication.getOtherApplicationsBundle().getDocument().getFilename();
        }

        return Stream.of(c2DocumentName, otherDocumentName)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(", "));
    }
}
