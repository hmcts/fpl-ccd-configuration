package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.FieldsGroup;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.TempNullify;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;

@Component
public class CaseDetailsHelper {

    private CaseDetailsHelper() {
    }

    public static boolean isCaseNumber(String caseNumber) {
        return StringUtils.isNumeric(caseNumber) && caseNumber.length() == 16;
    }

    public static String formatCCDCaseNumber(Long caseNumber) {
        String ccdCaseNumber = String.valueOf(caseNumber);

        if (!isCaseNumber(ccdCaseNumber)) {
            throw new IllegalArgumentException("CCD Case number must be 16 digits long");
        }

        return String.join("-", Splitter.fixedLength(4).splitToList(ccdCaseNumber));
    }

    public static void removeTemporaryFields(CaseDetails caseDetails, String... fields) {
        for (String field : fields) {
            caseDetails.getData().remove(field);
        }
    }

    public static void removeTemporaryFields(CaseDetailsMap caseDetails, String... fields) {
        for (String field : fields) {
            caseDetails.remove(field);
        }
    }

    public static void removeTemporaryFields(CaseDetailsMap caseDetails, List<String> fields) {
        for (String field : fields) {
            caseDetails.remove(field);
        }
    }

    public static CaseDetails removeTemporaryFields(CaseDetails caseDetails, Class clazz) {
        getFieldsListWithAnnotation(clazz, Temp.class).stream()
            .map(Field::getName)
            .forEach(caseDetails.getData()::remove);

        return caseDetails;
    }

    public static CaseDetailsMap addFields(CaseDetailsMap caseDetails, Object target, String... groups) {

        requireNonNull(target);

        final List<String> groupList = Arrays.asList(groups);

        getFieldsListWithAnnotation(target.getClass(), FieldsGroup.class).stream()
            .filter(field -> stream(field.getAnnotation(FieldsGroup.class).value()).anyMatch(groupList::contains))
            .forEach(field -> {
                ReflectionUtils.makeAccessible(field);
                caseDetails.putIfNotEmpty(field.getName(), ReflectionUtils.getField(field, target));
            });

        return caseDetails;
    }

    public static Map<String, Object> nullifyTemporaryFields(Map<String, Object> caseDetails, Class clazz) {
        Map<String, Object> map = new HashMap<>(caseDetails);

        getFieldsListWithAnnotation(clazz, TempNullify.class).stream()
            .map(Field::getName)
            .forEach(name -> map.put(name, null));

        return map;
    }

    public static boolean isInOpenState(CaseDetails caseDetails) {
        return isInState(caseDetails, OPEN);
    }

    public static boolean isInReturnedState(CaseDetails caseDetails) {
        return isInState(caseDetails, RETURNED);
    }

    public static boolean isInGatekeepingState(CaseDetails caseDetails) {
        return isInState(caseDetails, GATEKEEPING);
    }

    private static boolean isInState(CaseDetails caseDetails, State state) {
        return state.getValue().equals(caseDetails.getState());
    }
}
