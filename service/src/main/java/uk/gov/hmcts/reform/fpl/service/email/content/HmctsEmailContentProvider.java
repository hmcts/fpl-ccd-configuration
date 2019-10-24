package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Autowired
    public HmctsEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                     ObjectMapper objectMapper,
                                     @Value("${ccd.ui.base.url}") String uiBaseUrl) {
        super(uiBaseUrl);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buildHmctsSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        return super.getCasePersonalisationBuilder(caseDetails)
            .put("court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    public Map<String, Object> buildC2UploadNotification(final CaseDetails caseDetails,
                                                         final String localAuthorityCode) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Map<String, Object>> respondents1 =
            (ObjectUtils.isEmpty(caseDetails.getData().get("respondents1"))
                ? Collections.emptyList() : objectMapper.convertValue(
                    caseDetails.getData().get("respondents1"), new TypeReference<>() {}));

        List<Respondent> respondents = (CollectionUtils.isEmpty(respondents1)
            ? Collections.emptyList() : respondents1.stream().map(
                respondent -> objectMapper.convertValue(
                    respondent.get("value"), Respondent.class)).collect(toList()));

        final String subjectLine = buildSubjectLine(caseData, respondents);
        return super.getCasePersonalisationBuilder(caseDetails)
            .put("subjectLine", subjectLine)
            .put("hearingDetailsCallout", subjectLine)
            .build();
    }

    private String buildSubjectLine(CaseData caseData, List<Respondent> respondents) {
        return String.format("%1$s%2$s",
            (StringUtils.isNotBlank(getRespondent1Lastname(respondents))
                ? String.format("%1$s, ", getRespondent1Lastname(respondents)) : ""),
            StringUtils.defaultIfBlank(
                String.format("%1$s", caseData.getFamilyManCaseNumber()), ""));
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
