package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiAddress;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiApplicant;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseDocument;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseManagementLocation;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiChild;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiColleague;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiFactorsParenting;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiHearing;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiInternationalElement;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiOther;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiProceeding;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiRespondent;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiRisk;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiCaseService {
    private final CaseConverter caseConverter;
    private final SearchService searchService;
    private final List<CafcassApiCaseDataConverter> cafcassApiCaseDataConverters;

    public List<CafcassApiCase> searchCaseByDateRange(LocalDateTime startDate, LocalDateTime endDate) {

        final RangeQuery searchRange = RangeQuery.builder()
            .field("last_modified")
            .greaterThanOrEqual(startDate)
            .lessThanOrEqual(endDate).build();

        List<CaseDetails> caseDetails = searchService.search(searchRange, 10000 , 0);

        return caseDetails.stream()
            .map(this::convertToCafcassApiCase)
            .toList();
    }

    private CafcassApiCase convertToCafcassApiCase(CaseDetails caseDetails) {
        return CafcassApiCase.builder()
            .caseId(caseDetails.getId())
            .jurisdiction(caseDetails.getJurisdiction())
            .state(caseDetails.getState())
            .caseTypeId(caseDetails.getCaseTypeId())
            .createdDate(caseDetails.getCreatedDate())
            .lastModified(caseDetails.getLastModified())
            .caseData(getCafcassApiCaseData(caseConverter.convert(caseDetails)))
            .build();
    }

    private CafcassApiCaseData getCafcassApiCaseData(CaseData caseData) {
        CafcassApiCaseData.CafcassApiCaseDataBuilder builder = CafcassApiCaseData.builder();

        for (CafcassApiCaseDataConverter converter : cafcassApiCaseDataConverters) {
            builder = converter.convert(caseData, builder);
        }

        return builder.build();
    }
}
