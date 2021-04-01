package uk.gov.hmcts.reform.fpl.utils.captor;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayDeque;
import java.util.Queue;

public class ResultsCaptor<R> implements Answer<R> {
    private final Queue<R> results = new ArrayDeque<>();

    @Override
    @SuppressWarnings("unchecked")
    public R answer(InvocationOnMock invocation) throws Throwable {
        R result = (R) invocation.callRealMethod();
        results.add(result);
        return result;
    }

    public R getResult() {
        return results.poll();
    }
}
