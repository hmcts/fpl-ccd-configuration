package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.exceptions.LocalAuthorityNotFound;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.SharedLocalAuthorityChangedNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityChangedContentProvider extends AbstractEmailContentProvider {

    private final EmailNotificationHelper helper;

    public SharedLocalAuthorityChangedNotifyData getNotifyDataForAddedLocalAuthority(CaseData caseData) {

        return SharedLocalAuthorityChangedNotifyData.builder()
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .secondaryLocalAuthority(getSharedLocalAuthorityName(caseData))
            .designatedLocalAuthority(caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationName())
            .build();
    }

    public SharedLocalAuthorityChangedNotifyData getNotifyDataForDesignatedLocalAuthority(CaseData caseData) {

        return SharedLocalAuthorityChangedNotifyData.builder()
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .childLastName(helper.getEldestChildLastName(caseData))
            .secondaryLocalAuthority(getSharedLocalAuthorityName(caseData))
            .designatedLocalAuthority(caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationName())
            .build();
    }

    public SharedLocalAuthorityChangedNotifyData getNotifyDataForRemovedLocalAuthority(
        CaseData caseData, CaseData caseDataBefore) {

        return SharedLocalAuthorityChangedNotifyData.builder()
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .secondaryLocalAuthority(getSharedLocalAuthorityName(caseDataBefore))
            .designatedLocalAuthority(caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationName())
            .build();
    }

    private String getSharedLocalAuthorityName(CaseData caseData) {
        return Optional.ofNullable(caseData.getSharedLocalAuthorityPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationName)
            .orElseThrow(() -> new LocalAuthorityNotFound("Secondary local authority for case " + caseData.getId()));
    }

}
