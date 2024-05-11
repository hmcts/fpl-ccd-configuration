import { type Page, type Locator, expect } from "@playwright/test";
import {BasePage} from "./base-page";

export class RiskAndHarmToChildren extends BasePage{
    readonly page: Page;
    readonly physicalHarmRadio: Locator;
    readonly emotionalHarmRadio: Locator;
    readonly sexualAbuseRadio: Locator;
    readonly neglectRadio: Locator;
    readonly futureRiskOfHarmCheckbox: Locator;
    readonly pastHarmCheckbox: Locator;
    readonly continueButton: Locator;
    readonly checkYourAnswersHeader: Locator;
    readonly saveAndContinueButton: Locator;
    readonly riskAndHarmToChildrenHeader: Locator;

    public constructor(page: Page) {
        super(page);
        this.page = page;
        this.physicalHarmRadio = page.getByRole('group', { name: 'Physical harm including non-' });
        this.emotionalHarmRadio = page.getByRole('group', { name: 'Emotional harm (Optional)' });
        this.sexualAbuseRadio = page.getByRole('group', { name: 'Sexual abuse (Optional)' });
        this.neglectRadio = page.getByRole('group', { name: 'Neglect (Optional)' });
        this.futureRiskOfHarmCheckbox = page.getByRole('checkbox', { name: 'Future risk of harm' });
        this.pastHarmCheckbox = page.locator('[id="risks_neglectOccurrences-Past\\ harm"]');
        // this.continueButton = page.getByRole('button', { name: 'Continue' });
        // this.checkYourAnswersHeader = page.getByRole('heading', { name: 'Check your answers' });
        // this.saveAndContinueButton = page.getByRole('button', { name: 'Save and continue' });
        this.riskAndHarmToChildrenHeader = page.getByRole('heading', { name: 'Risk and harm to children' });
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
        // await this.continueButton.click();
        // await this.checkYourAnswersHeader.isVisible();
        // await this.saveAndContinueButton.click();
    }
}
