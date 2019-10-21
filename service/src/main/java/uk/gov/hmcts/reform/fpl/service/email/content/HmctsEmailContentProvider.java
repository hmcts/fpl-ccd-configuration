package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.time.LocalDate;
import java.time.format.FormatStyle;
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
    private final DateFormatterService dateFormatterService;

    @Autowired
    public HmctsEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                     MapperService mapper,
                                     DateFormatterService dateFormatterService,
                                     @Value("${ccd.ui.base.url}") String uiBaseUrl) {
        super(uiBaseUrl);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.mapper = mapper;
        this.dateFormatterService = dateFormatterService;
    }

    public Map<String, Object> buildHmctsSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        return super.getCasePersonalisationBuilder(caseDetails)
            .put("court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    public Map<String, Object> buildC2UploadNotification(final CaseDetails caseDetails,
                                                         final String localAuthorityCode) {
        // Validation within our frontend ensures that the following data is present
        CaseData caseData = mapper.getObjectMapper().convertValue(caseDetails.getData(), CaseData.class);

        List<Map<String, Object>> respondents1 = mapper.getObjectMapper().convertValue(
            caseDetails.getData().get("respondents1"), new TypeReference<>() {});

        List<Respondent> respondents = respondents1.stream()
            .map(respondent ->
                mapper.getObjectMapper().convertValue(respondent.get("value"), Respondent.class))
            .collect(toList());

        return Map.of(
            "court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName(),
            "lastNameOfRespondent", (getRespondent1(respondents).isPresent() ? getRespondent1(respondents).get() : ""),
            "familyManCaseNumber", caseData.getFamilyManCaseNumber(),
            "hearingDate",
            dateFormatterService.formatLocalDateToString((getHearingBooking(caseData).isPresent()
                    ? getHearingBooking(caseData).get().getValue().getDate()
                    : LocalDate.now()), FormatStyle.MEDIUM),
            "reference", String.valueOf(caseDetails.getId())
        );
    }

    private Optional<Element<HearingBooking>> getHearingBooking(final CaseData caseData) {
        return caseData.getHearingDetails()
        .stream()
        .filter(Objects::nonNull)
        .findFirst();
    }

    private Optional<String> getRespondent1(final List<Respondent> respondents) {
        Optional<Respondent> optionalRespondent = respondents
                .stream()
                .filter(Objects::nonNull)
                .findFirst();

        return optionalRespondent.map(respondent -> respondent.getParty().getLastName());
    }
}
