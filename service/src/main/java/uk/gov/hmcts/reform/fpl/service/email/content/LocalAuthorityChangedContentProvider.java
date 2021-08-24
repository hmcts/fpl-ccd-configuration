package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.exceptions.LocalAuthorityNotFound;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.CaseTransferredNotifyData;
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
            .secondaryLocalAuthority(getSecondaryLocalAuthorityName(caseData))
            .designatedLocalAuthority(getDesignatedLocalAuthorityName(caseData))
            .build();
    }

    public SharedLocalAuthorityChangedNotifyData getNotifyDataForDesignatedLocalAuthority(CaseData caseData) {

        return SharedLocalAuthorityChangedNotifyData.builder()
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .childLastName(helper.getEldestChildLastName(caseData))
            .secondaryLocalAuthority(getSecondaryLocalAuthorityName(caseData))
            .designatedLocalAuthority(getDesignatedLocalAuthorityName(caseData))
            .build();
    }

    public SharedLocalAuthorityChangedNotifyData getNotifyDataForRemovedLocalAuthority(
        CaseData caseData, CaseData caseDataBefore) {

        return SharedLocalAuthorityChangedNotifyData.builder()
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .secondaryLocalAuthority(getSecondaryLocalAuthorityName(caseDataBefore))
            .designatedLocalAuthority(getDesignatedLocalAuthorityName(caseData))
            .build();
    }

    public CaseTransferredNotifyData getCaseTransferredNotifyData(CaseData caseData, CaseData caseDataBefore) {

        return CaseTransferredNotifyData.builder()
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .caseUrl(getCaseUrl(caseData.getId()))
            .childLastName(helper.getEldestChildLastName(caseData))
            .newDesignatedLocalAuthority(getDesignatedLocalAuthorityName(caseData))
            .prevDesignatedLocalAuthority(getDesignatedLocalAuthorityName(caseDataBefore))
            .build();
    }

    private String getDesignatedLocalAuthorityName(CaseData caseData) {
        return getOrganisationName(caseData.getLocalAuthorityPolicy()).orElseThrow(() ->
            new LocalAuthorityNotFound("Designated local authority name not found for case " + caseData.getId()));
    }

    private String getSecondaryLocalAuthorityName(CaseData caseData) {
        return getOrganisationName(caseData.getSharedLocalAuthorityPolicy()).orElseThrow(() ->
            new LocalAuthorityNotFound("Secondary local authority name not found for case " + caseData.getId()));
    }

    private Optional<String> getOrganisationName(OrganisationPolicy organisationPolicy) {
        return Optional.ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationName);
    }

}
