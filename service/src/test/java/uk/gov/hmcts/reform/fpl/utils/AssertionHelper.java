package uk.gov.hmcts.reform.fpl.utils;

import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Map;

public class AssertionHelper {

    private AssertionHelper() {
    }

    //Used for asserting maps that contain JSONObjects (i.e document attachments for GOV.Notify notifications)
    public static void assertEquals(Map<String, Object> actual, Map<String, Object> expected) {
        JSONAssert.assertEquals(new JSONObject(actual), new JSONObject(expected), true);
    }
}
