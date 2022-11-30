package uk.gov.hmcts.reform.fpl.utils;

public class GrammarHelper {

    private GrammarHelper() {
    }

    public static String getChildGrammar(int numOfChildren) {
        return (numOfChildren > 1) ? "children" : "child";
    }

    public static String getIsOrAreGrammar(int num) {
        return (num > 1) ? "are" : "is";
    }

    public static String getWasOrWereGrammar(int num) {
        return (num > 1) ? "were" : "was";
    }

    public static String geHasOrHaveGrammar(int num, boolean isThirdPerson) {
        return (!isThirdPerson || num > 1) ? "have" : "has";
    }
}
