package uk.gov.hmcts.reform.fpl.model.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SealType {
    ENGLISH("static_data/familycourtseal.png"),
    BILINGUAL("static_data/familycourtseal-bilingual.png");

    private final String image;
}
