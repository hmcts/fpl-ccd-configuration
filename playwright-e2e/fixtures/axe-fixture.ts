import { test as base } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";

type AxeFixture = {
  makeAxeBuilder: () => AxeBuilder;
};

export const test = base.extend<AxeFixture>({
  makeAxeBuilder: async ({ page }, use) => {
    const makeAxeBuilder = () =>
      new AxeBuilder({ page })
        .withTags(["wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "wcag22a", "wcag22aa"])
        .exclude("#commonly-reused-element-with-known-issue");

    await use(makeAxeBuilder);
  },
});
export { expect } from "@playwright/test";
