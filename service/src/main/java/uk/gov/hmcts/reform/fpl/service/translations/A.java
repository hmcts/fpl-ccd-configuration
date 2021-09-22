package uk.gov.hmcts.reform.fpl.service.translations;

import java.text.SimpleDateFormat;
import java.util.Date;

public class A {



    public static void main(String[] args) {
        System.out.println(new SimpleDateFormat("Y").format(new Date(2010,1,1)));
        System.out.println(new SimpleDateFormat("y").format(new Date(2010,1,1)));
    }

}
