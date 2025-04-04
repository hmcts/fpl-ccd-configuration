import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";
import { join } from "path";
import { ApplicantDetails } from "./applicant-details";

export class SubmitCase extends BasePage{
 // readonly page: Page;

    get declarationHeading(): Locator {
        return this.page.getByText('Declaration');
    }

    get statementAgree(): Locator {
        return this.page.getByLabel('I agree with this statement');
    }

    get applicationSentHeading(): Locator {
        return this.page.getByRole('heading', { name: 'Application sent' });
    }

    get closeReturnToCase(): Locator {
        return this.page.getByRole('button', { name: 'Close and Return to case' });
    }

    get caseInfoHeading(): Locator {
        return this.page.getByRole('heading', { name: 'Case information' });
    }

    get teamManagerNameText(): Locator {
        return this.page.getByText(`believe that the facts stated in this application are true`);
    }

    get paymentAmountLocator(): Locator {
        return this.page.locator('dd').filter({ hasText: '£' });
    }

    get paymentAmountText(): Locator {
        return this.page.getByText('£');
    }



  async submitCaseSmokeTest(amount:string= '2,437.00') {
    //first page
    await expect(this.declarationHeading).toBeVisible();
    await expect(this.teamManagerNameText).toBeVisible();
    await this.statementAgree.check();
    await expect(this.paymentAmountLocator).toBeVisible();
    await this.clickSubmit();

    //second page
    // await expect(this.checkYourAnswersHeader).toBeVisible();
    await expect(this.declarationHeading).toBeVisible();
    await expect(this.teamManagerNameText).toBeVisible();
    await expect(this.paymentAmountText).toBeVisible();
    await expect(this.page.getByText(`${amount}`)).toBeVisible();
    await this.clickSubmit();
    await expect(this.applicationSentHeading).toBeVisible();
    await this.closeReturnToCase.click();
   // await expect(this.caseInfoHeading).toBeVisible();
  }
}
