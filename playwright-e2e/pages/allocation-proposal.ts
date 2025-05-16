import {expect, type Locator} from "@playwright/test";
import {BasePage} from "./base-page.ts";

export class AllocationProposal extends BasePage {


    get allocationProposalHeading(): Locator {
        return this.page.getByRole('group', {name: 'Allocation proposal'}).getByRole('heading');
    }

    get radioButton(): Locator {
        return this.page.getByRole('group', {name: 'Which level of judge do you recommend for this case'});
    }

    get reasonsForRecommendation(): Locator {
        return this.page.getByLabel('Reasons for recommendation');
    }


    async allocationProposalSmokeTest() {
        await expect.soft(this.allocationProposalHeading).toBeVisible();
        await this.radioButton.getByLabel('Circuit Judge').click();
        await this.reasonsForRecommendation.fill('Test');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
