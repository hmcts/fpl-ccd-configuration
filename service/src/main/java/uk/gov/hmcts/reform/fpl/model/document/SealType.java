package uk.gov.hmcts.reform.fpl.model.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SealType {
    ENGLISH("static_data/familycourtseal.png"),
    WELSH("static_data/familycourtseal-welsh.png"),
    HIGHCOURT_ENGLISH("static_data/highcourtseal.png");

    private final String image;
}
