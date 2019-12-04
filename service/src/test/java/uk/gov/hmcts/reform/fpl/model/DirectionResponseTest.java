package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class DirectionResponseTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldSerialiseRespondingOnBehalfOfToCorrectStringValueWhenRespondentValue() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(DirectionResponse.builder()
            .respondingOnBehalfOf("RESPONDENT_1")
            .build());

        JSONAssert.assertEquals("{respondingOnBehalfOfRespondent:RESPONDENT_1}", serialised, false);
    }

    @Test
    void shouldSerialiseRespondingOnBehalfOfToCorrectStringValueWhenOtherValue() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(DirectionResponse.builder()
            .respondingOnBehalfOf("OTHER_1")
            .build());

        JSONAssert.assertEquals("{respondingOnBehalfOfOthers:OTHER_1}", serialised, false);
    }

    @Test
    void shouldSerialiseRespondingOnBehalfOfToCorrectStringValueWhenCafcassValue() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(DirectionResponse.builder()
            .respondingOnBehalfOf("CAFCASS")
            .build());

        JSONAssert.assertEquals("{respondingOnBehalfOfCafcass:CAFCASS}", serialised, false);
    }

    @Test
    void shouldSerialiseDirectionResponseWhenFullyPopulated() throws JsonProcessingException {
        UUID uuid = randomUUID();

        String serialised = mapper.writeValueAsString(getResponse(uuid));

        JSONAssert.assertEquals("{directionId:" + uuid + ",assignee:COURT,complied:Yes,"
            + "documentDetails:details,file:{document_url:url,document_filename:file name,"
            + "document_binary_url:binary url},cannotComplyReason:cannot comply reason,c2Uploaded:[Yes],"
            + "cannotComplyFile:{document_url:url,document_filename:file name,document_binary_url:binary url},"
            + "respondingOnBehalfOfOthers:OTHER_1}", serialised, false);
    }

    @Test
    void shouldSerialiseWhenNoValues() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(DirectionResponse.builder().build());

        assertThat(serialised).isEqualTo("{\"directionId\":null,\"assignee\":null,\"complied\":null,"
            + "\"documentDetails\":null,\"file\":null,\"cannotComplyReason\":null,\"c2Uploaded\":null,"
            + "\"cannotComplyFile\":null,\"respondingOnBehalfOfRespondent\":null,\"respondingOnBehalfOfOthers\":null,"
            + "\"respondingOnBehalfOfCafcass\":null}");
    }

    @Test
    void shouldDeserialiseRespondingOnBehalfOfWhenRespondingOnBehalfOfRespondent() throws JsonProcessingException {
        DirectionResponse deserialised =
            mapper.readValue("{\"respondingOnBehalfOfRespondent\":\"RESPONDENT_1\"}", DirectionResponse.class);

        assertThat(deserialised).isEqualTo(DirectionResponse.builder()
            .respondingOnBehalfOf("RESPONDENT_1")
            .build());
    }

    @Test
    void shouldDeserialiseRespondingOnBehalfOfWhenRespondingOnBehalfOfOther() throws JsonProcessingException {
        DirectionResponse deserialised =
            mapper.readValue("{\"respondingOnBehalfOfOthers\":\"OTHER_1\"}", DirectionResponse.class);

        assertThat(deserialised).isEqualTo(DirectionResponse.builder()
            .respondingOnBehalfOf("OTHER_1")
            .build());
    }

    @Test
    void shouldDeserialiseRespondingOnBehalfOfWhenRespondingOnBehalfOfCafcass() throws JsonProcessingException {
        DirectionResponse deserialised =
            mapper.readValue("{\"respondingOnBehalfOfCafcass\":\"CAFCASS\"}", DirectionResponse.class);

        assertThat(deserialised).isEqualTo(DirectionResponse.builder()
            .respondingOnBehalfOf("CAFCASS")
            .build());
    }

    @Test
    void shouldDeserialiseDirectionResponseWhenOnlyOneValidRespondingValue() throws JsonProcessingException {
        String content = "{\"respondingOnBehalfOfCafcass\":null,\"respondingOnBehalfOfOthers\":null,"
            + "\"respondingOnBehalfOfRespondent\":\"RESPONDENT_1\"}";

        DirectionResponse deserialised = mapper.readValue(content, DirectionResponse.class);

        assertThat(deserialised).isEqualTo(DirectionResponse.builder()
            .respondingOnBehalfOf("RESPONDENT_1")
            .build());
    }

    @Test
    void shouldDeserialiseDirectionResponseObjectWhenFullyPopulated() throws JsonProcessingException {
        UUID uuid = randomUUID();

        DirectionResponse deserialised = mapper.readValue("{\"directionId\":\"" + uuid + "\","
            + "\"assignee\":\"COURT\",\"respondingOnBehalfOfOthers\":\"OTHER_1\",\"complied\":\"Yes\","
            + "\"documentDetails\":\"details\",\"file\":{\"document_url\":\"url\",\"document_filename\":\"file name\","
            + "\"document_binary_url\":\"binary url\"},\"cannotComplyReason\":\"cannot comply reason\","
            + "\"c2Uploaded\":[\"Yes\"],\"cannotComplyFile\":{\"document_url\":\"url\","
            + "\"document_filename\":\"file name\",\"document_binary_url\":\"binary url\"}}", DirectionResponse.class);

        assertThat(deserialised).isEqualTo(getResponse(uuid));
    }

    private DirectionResponse getResponse(UUID uuid) {
        return DirectionResponse.builder()
            .assignee(COURT)
            .directionId(uuid)
            .complied("Yes")
            .c2Uploaded(singletonList("Yes"))
            .cannotComplyFile(DocumentReference.builder()
                .filename("file name")
                .url("url")
                .binaryUrl("binary url")
                .build())
            .documentDetails("details")
            .cannotComplyReason("cannot comply reason")
            .file(DocumentReference.builder()
                .filename("file name")
                .url("url")
                .binaryUrl("binary url")
                .build())
            .respondingOnBehalfOf("OTHER_1")
            .build();
    }
}
