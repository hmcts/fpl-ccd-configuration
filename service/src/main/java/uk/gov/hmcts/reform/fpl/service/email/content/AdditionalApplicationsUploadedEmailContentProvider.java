package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.SupplementType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildUnformattedCalloutWithNextHearing;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdditionalApplicationsUploadedEmailContentProvider extends AbstractEmailContentProvider {
    private final Time time;
    private final EmailNotificationHelper helper;

    public AdditionalApplicationsUploadedTemplate getNotifyData(final CaseData caseData) {
        return AdditionalApplicationsUploadedTemplate.builder()
            .callout(buildUnformattedCalloutWithNextHearing(caseData, time.now()))
            .lastName(helper.getSubjectLineLastName(caseData))
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .caseUrl(getCaseUrl(caseData.getId(), OTHER_APPLICATIONS))
            .applicationTypes(getApplicationTypes(caseData.getAdditionalApplicationsBundle().get(0).getValue()))
            .build();
    }

    public BaseCaseNotifyData getPbaPaymentNotTakenNotifyData(final CaseData caseData) {
        return BaseCaseNotifyData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), OTHER_APPLICATIONS))
            .build();
    }

    private List<String> getApplicationTypes(AdditionalApplicationsBundle additionalApplicationsBundle) {
        List<String> applicationTypes = new ArrayList<>();
        C2DocumentBundle c2DocumentBundle = additionalApplicationsBundle.getC2DocumentBundle();
        OtherApplicationsBundle otherDocumentBundle = additionalApplicationsBundle.getOtherApplicationsBundle();

        addC2DocumentBundleApplicationTypes(applicationTypes, c2DocumentBundle);
        addOtherApplicationsBundleApplicationTypes(applicationTypes, otherDocumentBundle);

        return applicationTypes;
    }

    private void addC2DocumentBundleApplicationTypes(List<String> applicationTypes, C2DocumentBundle c2DocumentBundle) {
        if (c2DocumentBundle == null) {
            return;
        }

        String c2Type = String.format("C2 (%s)", c2DocumentBundle.getType().getLabel());
        List<C2AdditionalOrdersRequested> c2AdditionalOrdersRequested =
            c2DocumentBundle.getC2AdditionalOrdersRequested();

        if (isNotEmpty(c2AdditionalOrdersRequested)) {
            List<String> c2AdditionalOrdersRequestedLabels = new ArrayList<>();

            for (C2AdditionalOrdersRequested orderRequested : c2AdditionalOrdersRequested) {
                if (C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY.equals(orderRequested)) {
                    c2AdditionalOrdersRequestedLabels.add(c2DocumentBundle.getParentalResponsibilityType().getLabel());
                } else {
                    c2AdditionalOrdersRequestedLabels.add(orderRequested.getLabel());
                }
            }

            c2Type += " - " + String.join(", ", c2AdditionalOrdersRequestedLabels);
        }

        applicationTypes.add(c2Type);

        addSupplementTypes(applicationTypes, c2DocumentBundle.getSupplementsBundle());
    }

    private void addOtherApplicationsBundleApplicationTypes(List<String> applicationTypes,
                                                            OtherApplicationsBundle otherDocumentBundle) {
        if (otherDocumentBundle == null) {
            return;
        }

        OtherApplicationType otherApplicationType = otherDocumentBundle.getApplicationType();

        if (OtherApplicationType.C1_PARENTAL_RESPONSIBILITY.equals(otherApplicationType)) {
            applicationTypes.add(String.format("C1 - %s",
                otherDocumentBundle.getParentalResponsibilityType().getLabel()));
        } else {
            applicationTypes.add(otherDocumentBundle.getApplicationType().getLabel());
        }

        addSupplementTypes(applicationTypes, otherDocumentBundle.getSupplementsBundle());
    }

    private void addSupplementTypes(List<String> applicationTypes, List<Element<Supplement>> supplements) {
        supplements.stream()
            .map(Element::getValue)
            .forEach(supplement -> {
                String supplementName = supplement.getName().getLabel();
                if (SupplementType.C20_SECURE_ACCOMMODATION.equals(supplement.getName())) {
                    supplementName += String.format(" (%s)", supplement.getSecureAccommodationType().getLabel());
                }
                applicationTypes.add(supplementName);
            });
    }
}
