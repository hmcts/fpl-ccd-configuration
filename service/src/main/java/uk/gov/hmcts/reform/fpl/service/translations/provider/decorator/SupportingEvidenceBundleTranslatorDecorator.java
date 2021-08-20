package uk.gov.hmcts.reform.fpl.service.translations.provider.decorator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SupportingEvidenceBundleTranslatorDecorator {

    private final Time time;

    public Function<Element<SupportingEvidenceBundle>, Element<SupportingEvidenceBundle>> translatedBundle(
        DocumentReference document, UUID selectedOrderId) {
        return bundle -> element(bundle.getId(), bundleMatch(selectedOrderId, bundle) ?
            addTranslationInfo(document, bundle) : bundle.getValue());
    }

    private boolean bundleMatch(UUID selectedOrderId, Element<SupportingEvidenceBundle> bundle) {
        return Objects.equals(bundle.getId(), selectedOrderId);
    }

    private SupportingEvidenceBundle addTranslationInfo(DocumentReference document,
                                                        Element<SupportingEvidenceBundle> bundle) {
        return bundle.getValue().toBuilder()
            .translatedDocument(document)
            .translationUploadDateTime(time.now())
            .build();
    }
}
