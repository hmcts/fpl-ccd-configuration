package uk.gov.hmcts.reform.fpl.service.furtherevidence;

import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Component
public class FurtherEvidenceUploadDifferenceCalculator {

    public List<Element<SupportingEvidenceBundle>> calculate(CaseData caseData, CaseData caseDataBefore) {
        List<Element<SupportingEvidenceBundle>> documentsBefore = getElements(caseDataBefore);
        List<Element<SupportingEvidenceBundle>> documents = getElements(caseData);


        Set<SupportingEvidenceBundleView> evidenceBundleViewsBefore = documentsBefore.stream()
            .map(this::toSupportingEvidenceBundleView)
            .collect(Collectors.toSet());

        Set<SupportingEvidenceBundleView> evidenceBundleViews = documents.stream()
            .map(this::toSupportingEvidenceBundleView)
            .collect(Collectors.toSet());

        Set<UUID> change = Sets.difference(evidenceBundleViews, evidenceBundleViewsBefore)
            .stream()
            .map(it -> it.getUuid())
            .collect(Collectors.toSet());

        return getElements(caseData).stream().filter(el -> change.contains(el.getId())).collect(Collectors.toList());


    }

    private List<Element<SupportingEvidenceBundle>> getElements(CaseData caseData) {
        List<Element<SupportingEvidenceBundle>> list = new ArrayList<>();
        list.addAll(getHearingFurtherEvidenceDocuments(caseData));
        list.addAll(getRespondentStatements(caseData));
        list.addAll(getFurtherEvidenceDocuments(caseData));
        return list.stream().filter(p ->
            p.getValue().getNeedTranslation() == YesNo.YES && !p.getValue().hasBeenTranslated()
        ).collect(Collectors.toList());
    }

    private List<Element<SupportingEvidenceBundle>> getHearingFurtherEvidenceDocuments(CaseData caseData) {
        return caseData.getHearingFurtherEvidenceDocuments()
            .stream()
            .flatMap(x -> x.getValue().getSupportingEvidenceBundle().stream())
            .collect(Collectors.toList());
    }

    private List<Element<SupportingEvidenceBundle>> getRespondentStatements(CaseData caseData) {
        return caseData.getRespondentStatements().stream()
            .flatMap(a -> a.getValue().getSupportingEvidenceBundle().stream()).collect(Collectors.toList());
    }

    private List<Element<SupportingEvidenceBundle>> getFurtherEvidenceDocuments(CaseData caseData) {
        return defaultIfNull(caseData.getFurtherEvidenceDocuments(), new ArrayList<>());
    }

    private SupportingEvidenceBundleView toSupportingEvidenceBundleView(Element<SupportingEvidenceBundle> element) {

        SupportingEvidenceBundle bundle = element.getValue();

        return SupportingEvidenceBundleView.builder()
            .uuid(element.getId())
            .document(bundle.getDocument())
            .build();

    }

}
