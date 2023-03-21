package uk.gov.hmcts.reform.fpl.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
public class CafcassHelper {

    private CafcassHelper() {
    }

    public static boolean isNotifyingCafcassEngland(CaseData caseData,
                                                    CafcassLookupConfiguration cafcassLookupConfiguration) {
        final Optional<CafcassLookupConfiguration.Cafcass> recipientIsEngland
            = isEmpty(caseData.getCaseLocalAuthority())
            ? Optional.empty() : cafcassLookupConfiguration.getCafcassEngland(caseData.getCaseLocalAuthority());
        if (isEmpty(caseData.getCaseLocalAuthority())) {
            log.info(format("Not sending notification to England CAFCASS since caseLocalAuthority is null for case: %s",
                caseData.getId()));
        }
        return recipientIsEngland.isPresent();
    }

    public static boolean isNotifyingCafcassWelsh(CaseData caseData,
                                                  CafcassLookupConfiguration cafcassLookupConfiguration) {
        final Optional<CafcassLookupConfiguration.Cafcass> recipientIsWelsh
            = isEmpty(caseData.getCaseLocalAuthority())
            ? Optional.empty() : cafcassLookupConfiguration.getCafcassWelsh(caseData.getCaseLocalAuthority());
        if (isEmpty(caseData.getCaseLocalAuthority())) {
            log.info(format("Not sending notification to Welsh CAFCASS since caseLocalAuthority is null for case: %s",
                caseData.getId()));
        }
        return recipientIsWelsh.isPresent();
    }
}
