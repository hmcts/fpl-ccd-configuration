package uk.gov.hmcts.reform.fpl.model.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SealType {
    ENGLISH("static_data/FL-PLW-familycourtsealV2.png"),
    WELSH("static_data/FL-PLW-familycourtseal-welshV2.png"),
    HIGHCOURT_ENGLISH("static_data/highcourtseal.png");

    private final String image;
}
