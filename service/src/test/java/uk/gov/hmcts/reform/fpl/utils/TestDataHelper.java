package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class TestDataHelper {

    private TestDataHelper() {
    }

    public static DocumentReference testDocument() {
        return DocumentReference.builder()
            .filename(randomAlphanumeric(10))
            .url(randomAlphanumeric(10))
            .binaryUrl(randomAlphanumeric(10))
            .build();
    }
}
