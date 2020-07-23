package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.FplEvent;
import uk.gov.hmcts.reform.fpl.Task;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TaskListRenderer.class})
public class TaskListRenderTest {


    @Autowired
    private TaskListRenderer taskListRenderer;

    @Test
    public void basisTest() {

    }
}
