import { type Page, type Locator, expect } from "@playwright/test";
import { fill } from "lodash";
import {BasePage} from "./base-page.ts";

export class GroundsForTheApplication  extends BasePage {


    get groundsForTheApplicationHeading1(): Locator {
        return this.page.getByRole('heading', { name: 'Grounds for the application', level: 1 });
    }

    get groundsForTheApplicationHeading2(): Locator {
        return this.page.getByRole('heading', { name: 'Grounds for the application', level: 2 });
    }

    get chilBeyondParentalControl(): Locator {
        return this.page.getByRole('checkbox', { name: 'Child is beyond parental' });
    }

    get reasonBehindChildSuffering(): Locator {
        return this.page.getByText('What is the reason behind the child suffering or being likely to suffer significant harm?');
    }

    get thresholdDocument(): Locator {
        return this.page.getByText('Do you have the threshold document?');
    }

    get uploadDocumentSection(): Locator {
        return this.page.getByLabel('No, I will provide a summary');
    }

    get provideSummary(): Locator {
        return this.page.getByLabel('Provide a summary of how this');
    }


    async groundsForTheApplicationSmokeTest() {
        await expect(this.groundsForTheApplicationHeading1).toBeVisible();
        await expect(this.groundsForTheApplicationHeading2).toBeVisible();
        await expect(this.reasonBehindChildSuffering).toBeVisible();
        await this.chilBeyondParentalControl.check({ force: true });
        await expect(this.thresholdDocument).toBeVisible
        await this.uploadDocumentSection.check({ force: true });
        await this.provideSummary.fill('Test');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
     }
}
