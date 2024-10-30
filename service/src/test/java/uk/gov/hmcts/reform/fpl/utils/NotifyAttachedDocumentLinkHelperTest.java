package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.apache.commons.lang3.RandomUtils.nextBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;

class NotifyAttachedDocumentLinkHelperTest {
    @Test
    void shouldGenerateAttachedDocumentLinkSuccessfully() {
        byte[] documentContentsAsByte = nextBytes(20);
        String documentContent = new String(Base64.encodeBase64(documentContentsAsByte), ISO_8859_1);

        JSONObject expectedDocumentLink = new JSONObject()
            .put("file", documentContent)
            .put("retention_period", JSONObject.NULL)
            .put("filename", JSONObject.NULL)
            .put("confirm_email_before_download", JSONObject.NULL)
            .put("file", documentContent);

        Optional<JSONObject> generatedDocumentLink = generateAttachedDocumentLink(documentContentsAsByte);
        assertThat(generatedDocumentLink).isPresent();
        assertEquals(generatedDocumentLink.get(), expectedDocumentLink, true);
    }

    @Test
    void shouldNotGenerateDocumentLinkWhenDocumentByteContentGreaterThanTwoMB() {
        final byte[] documentContentsAsByte = nextBytes(5 * 1024 * 1024);

        assertFalse(generateAttachedDocumentLink(documentContentsAsByte).isPresent());
    }

    @ParameterizedTest
    @ValueSource(ints = {0})
    @NullSource
    void shouldNotGenerateDocumentLinkWhenDocumentByteContentIsEmptyOrNull(final Integer value) {
        final byte[] documentContentsAsByte = value == null ? null : nextBytes(value);

        assertFalse(generateAttachedDocumentLink(documentContentsAsByte).isPresent());
    }
}
