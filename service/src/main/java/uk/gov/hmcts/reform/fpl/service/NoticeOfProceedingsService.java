package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsService {

    public List<Element<DocumentBundle>> getRemovedDocumentBundles(CaseData caseData,
                                                                   List<DocmosisTemplates> templateTypes) {
        List<String> templateTypeTitles = templateTypes.stream().map(DocmosisTemplates::getDocumentTitle)
            .collect(Collectors.toList());

        ImmutableList.Builder<Element<DocumentBundle>> removedDocumentBundles = ImmutableList.builder();

        caseData.getNoticeOfProceedingsBundle().forEach(element -> {
            String filename = element.getValue().getDocument().getFilename();

            if (!templateTypeTitles.contains(filename)) {
                removedDocumentBundles.add(element);
            }
        });

        return removedDocumentBundles.build();
    }
}
