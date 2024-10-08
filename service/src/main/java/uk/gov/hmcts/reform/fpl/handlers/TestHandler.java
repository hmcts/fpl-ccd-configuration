package uk.gov.hmcts.reform.fpl.handlers;

import org.springframework.util.ConcurrentReferenceHashMap;

public class TestHandler {

    static ConcurrentReferenceHashMap<Integer, Object> testMap = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    public static void main(String[] args) throws InterruptedException {
        for (int i = 127; i < 130; i++) {
            testMap.put(i, "test" + i);
        }
        System.out.println("before gc:");
        System.out.println("Map is empty: " + testMap.isEmpty());

        new Thread(() -> {
            System.gc();
            System.out.println("after gc:");
            testMap.purgeUnreferencedEntries();
            System.out.println("Map is empty: " + testMap.isEmpty());
        }).start();


        new Thread(() -> {
            try {
                Thread.sleep(1);
            synchronized (testMap.get(128)) {
                System.out.println("Callback thread:" + testMap.get(128));
            }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
