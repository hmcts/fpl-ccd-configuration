import { type Page, type Locator, expect } from "@playwright/test";
import {BasePage} from "./base-page.ts";

export class RiskAndHarmToChildren extends BasePage {
    get whatElseAbility(): Locator {
        return this.page.getByLabel('Tell us what else is affecting their ability to parent');
    }
    get factorAffectingRespondentAbilityToParenting(): Locator {
        return this.page.getByRole('group', { name: 'Is there anything affecting any respondent\'s ability to parent?' });
    }
    get harmToChildren(): Locator {
        return this.page.getByRole('group', { name: 'What kind of harm is the child at risk of?' });
    }

    get riskAndHarmToChildrenHeader(): Locator {
        return this.page.getByRole('heading', { name: 'Risk and harm to children', exact: true,level:1 });
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
