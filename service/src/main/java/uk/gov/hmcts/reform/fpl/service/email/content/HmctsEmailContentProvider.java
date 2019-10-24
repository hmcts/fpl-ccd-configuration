package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class HmctsEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final MapperService mapper;

    @Autowired
    public HmctsEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                     MapperService mapper,
                                     @Value("${ccd.ui.base.url}") String uiBaseUrl) {
        super(uiBaseUrl);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.mapper = mapper;
    }

    public Map<String, Object> buildHmctsSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        return super.getCasePersonalisationBuilder(caseDetails)
            .put("court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    public Map<String, Object> buildC2UploadNotification(final CaseDetails caseDetails,
                                                         final String localAuthorityCode) {
        CaseData caseData = mapper.getObjectMapper().convertValue(caseDetails.getData(), CaseData.class);

        List<Map<String, Object>> respondents1 =
            (ObjectUtils.isEmpty(caseDetails.getData().get("respondents1"))
                ? Collections.emptyList() : mapper.getObjectMapper().convertValue(
                    caseDetails.getData().get("respondents1"), new TypeReference<>() {}));

        List<Respondent> respondents = (CollectionUtils.isEmpty(respondents1)
            ? Collections.emptyList() : respondents1.stream().map(
                respondent -> mapper.getObjectMapper().convertValue(
                    respondent.get("value"), Respondent.class)).collect(toList()));

        final String subjectLine = String.format("%1$s%2$s",
            (StringUtils.isNotBlank(getRespondent1Lastname(respondents))
                ? String.format("%1$s, ", getRespondent1Lastname(respondents)) : ""),
            StringUtils.defaultIfBlank(
                String.format("%1$s", caseData.getFamilyManCaseNumber()), ""));

        return super.getCasePersonalisationBuilder(caseDetails)
            .put("subjectLine", subjectLine)
            .put("hearingDetailsCallout", subjectLine)
            .build();
    }

    private String getRespondent1Lastname(final List<Respondent> respondents) {
        Optional<Respondent> optionalRespondent =
            (CollectionUtils.isEmpty(respondents) ? Optional.empty() : respondents
                .stream()
                .filter(Objects::nonNull)
                .findFirst());

        if (optionalRespondent.isPresent()) {
            return StringUtils.defaultIfBlank(optionalRespondent.get().getParty().getLastName(), "");
        }

        return StringUtils.EMPTY;
    }
}
