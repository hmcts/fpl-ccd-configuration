package uk.gov.hmcts.reform.fpl.e2e.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDetailsPage {

    public void startEvent(Page page, String eventName) {
        page.selectOption("#next-step", new SelectOption().setLabel(eventName));
        page.waitForTimeout(1000);
        page.locator("button:has-text(\"Go\")").click();
    }
}
