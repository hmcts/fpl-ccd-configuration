import AxeBuilder from "@axe-core/playwright";
import { TestInfo, Page } from "@playwright/test";

export async function runA11yCheck(page: Page, testInfo: TestInfo, label: string) {
    const results = await new AxeBuilder({ page }).analyze();

    await testInfo.attach(`a11y-${label}`, {
        body: JSON.stringify(results, null, 2),
        contentType: 'application/json'
    });
}
