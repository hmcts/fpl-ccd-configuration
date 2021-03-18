package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.SupplementType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdditionalApplicationsUploadedEmailContentProvider extends AbstractEmailContentProvider {

    private final Time time;

    public AdditionalApplicationsUploadedTemplate getNotifyData(final CaseData caseData,
                                                                final DocumentReference latestC2) {
        return AdditionalApplicationsUploadedTemplate.builder()
            .callout(buildCallout(caseData))
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .caseUrl(getCaseUrl(caseData.getId(), OTHER_APPLICATIONS))
            .applicationTypes(getApplicationTypes(caseData.getAdditionalApplicationsBundle().get(0).getValue()))
            .build();
    }

    private List<String> getApplicationTypes(AdditionalApplicationsBundle additionalApplicationsBundle) {
        List<String> applicationTypes = new ArrayList<>();
        C2DocumentBundle c2DocumentBundle = additionalApplicationsBundle.getC2DocumentBundle();
        OtherApplicationsBundle otherDocumentBundle = additionalApplicationsBundle.getOtherApplicationsBundle();

        getC2DocumentBundleApplicationTypes(applicationTypes, c2DocumentBundle);
        getOtherApplicationsBundleApplicationTypes(applicationTypes, otherDocumentBundle);

        return applicationTypes;
    }

    private void getC2DocumentBundleApplicationTypes(List<String> applicationTypes, C2DocumentBundle c2DocumentBundle) {
        if (c2DocumentBundle == null) {
            return;
        }

        String c2Type = String.format("C2 (%s)", c2DocumentBundle.getType().getLabel());
        List<C2AdditionalOrdersRequested> c2AdditionalOrdersRequested =
            c2DocumentBundle.getC2AdditionalOrdersRequested();

        if (c2AdditionalOrdersRequested != null) {
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

        c2DocumentBundle.getSupplementsBundle().stream()
            .map(Element::getValue)
            .forEach(Supplement -> {
                String supplementName = Supplement.getName().getLabel();
                if (SupplementType.C20_SECURE_ACCOMMODATION.equals(Supplement.getName())) {
                    supplementName += String.format(" (%s)", Supplement.getSecureAccommodationType().getLabel());
                }
                applicationTypes.add(supplementName);
            });
    }

    private void getOtherApplicationsBundleApplicationTypes(List<String> applicationTypes,
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

        otherDocumentBundle.getSupplementsBundle().stream()
            .map(Element::getValue)
            .forEach(Supplement -> {
                String supplementName = Supplement.getName().getLabel();
                if (SupplementType.C20_SECURE_ACCOMMODATION.equals(Supplement.getName())) {
                    supplementName += String.format(" (%s)", Supplement.getSecureAccommodationType().getLabel());
                }
                applicationTypes.add(supplementName);
            });
    }

    public BaseCaseNotifyData getPbaPaymentNotTakenNotifyData(final CaseData caseData) {
        return BaseCaseNotifyData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), OTHER_APPLICATIONS))
            .build();
    }
}
