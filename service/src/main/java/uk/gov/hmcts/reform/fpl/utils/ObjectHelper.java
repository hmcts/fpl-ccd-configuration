package uk.gov.hmcts.reform.fpl.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ObjectHelper {

    private ObjectHelper() {
    }

    public static <T> T getFieldValue(Object object, String fieldName, Class<T> targetType)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String getterBaseName;
        int firstDotLocation = fieldName.indexOf('.');
        if (firstDotLocation >= 0) {
            getterBaseName = fieldName.substring(0, firstDotLocation);
        } else {
            getterBaseName = fieldName;
        }

        Method getter = object.getClass().getMethod("get" + getterBaseName.substring(0, 1).toUpperCase()
                                                    + getterBaseName.substring(1));

        if (firstDotLocation >= 0) {
            Object fieldValue = getter.invoke(object);
            if (fieldValue == null) {
                throw new NullPointerException();
            }
            return getFieldValue(fieldValue, fieldName.substring(firstDotLocation + 1), targetType);
        } else {
            return targetType.cast(getter.invoke(object));
        }
    }
}
