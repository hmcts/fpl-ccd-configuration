import { type Page, type Locator, expect } from "@playwright/test";
import {BasePage} from "./base-page.ts";

export class RiskAndHarmToChildren extends BasePage {


    get physicalHarmRadio(): Locator {
        return this.page.getByRole('group', { name: 'Physical harm including non-' });;
    }

    get emotionalHarmRadio(): Locator {
        return this.page.getByRole('group', { name: 'Emotional harm (Optional)' });
    }

    get sexualAbuseRadio(): Locator {
        return this.page.getByRole('group', { name: 'Sexual abuse (Optional)' });
    }

    get neglectRadio(): Locator {
        return this.page.getByRole('group', { name: 'Neglect (Optional)' });
    }

    get futureRiskOfHarmCheckbox(): Locator {
        return this.page.getByRole('checkbox', { name: 'Future risk of harm' });
    }

    get pastHarmCheckbox(): Locator {
        return this.page.locator('[id="risks_neglectOccurrences-Past\\ harm"]');
    }


    get riskAndHarmToChildrenHeader(): Locator {
        return this.page.getByRole('heading', { name: 'Risk and harm to children' });
    }


    public constructor(page: Page) {
        super(page);
    }

    async riskAndHarmToChildrenSmokeTest() {
        await expect (this.riskAndHarmToChildrenHeader).toBeVisible();
        await this.physicalHarmRadio.getByLabel('Yes').check();
        await this.futureRiskOfHarmCheckbox.check();
        await this.emotionalHarmRadio.getByLabel('No').check();
        await this.sexualAbuseRadio.getByLabel('No').check();
        await this.neglectRadio.getByLabel('Yes').check();
        await this.pastHarmCheckbox.check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
          }
}
