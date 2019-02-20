package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.exceptions.MappingException;

import java.beans.ConstructorProperties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MapperServiceTest {

    private final MapperService mapper = new MapperService(new ObjectMapper());

    private final String name = "John";
    private final int age = 18;

    @Test
    void mappedInstanceShouldHaveAllValuesPopulated() {
        Person instance = mapper.mapObject(personDataBuilder().build(), Person.class);

        assertThat(instance.name).isEqualTo(name);
        assertThat(instance.age).isEqualTo(age);
    }

    @Test
    void mappingShouldThrowMappingExceptionWhenMappingFailed() {
        Assertions.assertThatThrownBy(() -> mapper.mapObject(personDataBuilder().put("sex", "M").build(), Person.class))
            .isInstanceOf(MappingException.class)
            .hasMessageStartingWith("com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException")
            .hasMessageContaining("Unrecognized field \"sex\"");
    }

    private ImmutableMap.Builder<String, Object> personDataBuilder() {
        return ImmutableMap.<String, Object>builder()
            .put("name", name)
            .put("age", age);
    }

    private static class Person {
        private final String name;
        private final int age;

        @ConstructorProperties({"name", "age"})
        private Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}
