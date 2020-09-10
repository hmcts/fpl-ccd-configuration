package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadC2DocumentsService {

    private final ValidateSupportingEvidenceBundleService validateSupportingEvidenceBundleService;
    private final IdamClient idamClient;
    private final Time time;
    private final RequestData requestData;

    public List<Element<C2DocumentBundle>> buildC2DocumentBundle(CaseData caseData) {
        List<Element<C2DocumentBundle>> c2DocumentBundle = defaultIfNull(caseData.getC2DocumentBundle(),
            Lists.newArrayList());

        List<SupportingEvidenceBundle> updatedSupportingEvidenceBundle =
            unwrapElements(caseData.getTemporaryC2Document().getSupportingEvidenceBundle())
                .stream()
                .map(supportingEvidence -> supportingEvidence.toBuilder().dateTimeUploaded(time.now()).build())
                .collect(Collectors.toList());

        var c2DocumentBundleBuilder = caseData.getTemporaryC2Document().toBuilder()
            .author(idamClient.getUserInfo(requestData.authorisation()).getName())
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME))
            .supportingEvidenceBundle(
                //TODO: Below empty check can be removed when supporting documents is toggled on in prod
                !isEmpty(updatedSupportingEvidenceBundle) ? wrapElements(updatedSupportingEvidenceBundle) : null);

        c2DocumentBundleBuilder.type(caseData.getC2ApplicationType().get("type"));

        c2DocumentBundle.add(Element.<C2DocumentBundle>builder()
            .id(UUID.randomUUID())
            .value(c2DocumentBundleBuilder.build())
            .build());

        return c2DocumentBundle;
    }

    public List<String> validate(C2DocumentBundle c2DocumentBundle) {
        return Optional.ofNullable(c2DocumentBundle)
            .map(c2Bundle -> unwrapElements(c2Bundle.getSupportingEvidenceBundle()))
            .map(validateSupportingEvidenceBundleService::validateBundle)
            .orElse(emptyList());
    }
}
