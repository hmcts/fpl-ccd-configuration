import { type Page, type Locator, expect } from "@playwright/test";
import {BasePage} from "./base-page";

export class RiskAndHarmToChildren extends BasePage {
    readonly page: Page;
    readonly riskAndHarmToChildrenHeader: Locator;
    readonly harmToChildren: Locator;
    readonly factorAffectingRespondentAbilityToParenting: Locator;
    readonly whatElseAbility: Locator;

    public constructor(page: Page) {
        super(page);
        this.page = page;
        this.factorAffectingRespondentAbilityToParenting = page.getByRole('group', { name: 'Is there anything affecting any respondent\'s ability to parent?' });
        this.harmToChildren = page.getByRole('group', { name: 'What kind of harm is the child at risk of?' });
        this.whatElseAbility = page.getByLabel('Tell us what else is affecting their ability to parent');
        this.riskAndHarmToChildrenHeader = page.getByRole('heading', { name: 'Risk and harm to children', exact: true,level:1 });
    }

    async riskAndHarmToChildrenSmokeTest() {
        await expect (this.riskAndHarmToChildrenHeader).toBeVisible();
        await this.harmToChildren.getByLabel('Physical harm including non-').check();
        await this.harmToChildren.getByLabel('Emotional harm').check();
        await this.harmToChildren.getByLabel('Sexual abuse').check();
        await this.harmToChildren.getByLabel('Neglect').check();
        await this.factorAffectingRespondentAbilityToParenting.getByLabel('Alcohol or drug abuse').check();
        await this.factorAffectingRespondentAbilityToParenting.getByLabel('Domestic abuse').check();
        await this.factorAffectingRespondentAbilityToParenting.getByLabel('Anything else').check();
        await this.whatElseAbility.fill('parent is drug addiction cant have mental stability to rise child');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
