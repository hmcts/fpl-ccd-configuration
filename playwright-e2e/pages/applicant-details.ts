import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ApplicantDetails extends BasePage {
    get mainContactDetails(): any {
        return this.page.getByRole('group').locator('#applicantContact_applicantContact');
    }



    get otherContactPerson(): any {
        return this.page.locator('#applicantContactOthers_0_0');
    }
    get applicantDetailsHeading(): Locator {
        return this.page.getByRole('heading', { name: 'Applicant details' });
    }

    get groupEmailAddress(): Locator {
        return this.page.getByLabel('Legal team manager\'s name and');
    }

    get pbaNumber(): Locator {
        return this.page.getByLabel('PBA number');
    }

    get customerReference(): Locator {
        return this.page.getByLabel('Customer reference');
    }

    get nameOfApplicantToSign(): Locator {
        return this.page.getByLabel('Name of the person who will');
    }

    get clientCode(): Locator {
        return this.page.getByLabel('Client code (Optional)');
    }

    get phoneNumber(): Locator {
        return this.page.getByLabel('Phone number', { exact: true });
    }

    get country(): Locator {
        return this.page.getByLabel('Country (Optional)');
    }

    get firstName(): Locator {
        return this.page.getByLabel('First name');
    }

    get lastName(): Locator {
        return this.page.getByLabel('Last name');
    }

    get alternativeNumber(): Locator {
        return this.page.getByLabel('Alternative phone number (');
    }

    get directEmailAddress(): Locator {
        return this.page.getByText('Direct email address (');
    }

    get addNew(): Locator {
        return this.page.getByRole('button', { name: 'Add new' });
    }

    get colleagueHeading(): Locator {
        return this.page.locator('h2').filter({ hasText: 'Colleague' });
    }

    get role(): Locator {
        return this.page.getByLabel('Other', { exact: true });
    }

    get enterRole(): Locator {
        return this.page.getByLabel('Enter their role (Optional)');
    }

  get representingPersonDetails(): Locator{
   return    this.page.getByRole('group', { name: 'Details of person you are representing' });

  }

  async applicantDetailsNeeded() {
    await expect.soft(this.applicantDetailsHeading).toBeVisible();
    await this.pbaNumber.fill('PBA0082848');
    await this.customerReference.fill('1234567');
    await this.nameOfApplicantToSign.fill('Tom Jones');
    await this.country.fill('United Kingdom');
    await this.clickContinue();
    await this.firstName.fill('Peters');
    await this.lastName.fill('John');
    await this.phoneNumber.fill('0123456789');
    await this.alternativeNumber.fill('123456780');
    await this.directEmailAddress.fill('Me2@mail.com');
    await this.addNew.click();
    await this.page.locator('#applicantContactOthers_0_firstName').fill('Me');
    await this.page.locator('#applicantContactOthers_0_lastName').fill('Two');
    await this.page.getByLabel('Email address', { exact: true }).fill('zee@mail.com');
    await this.role.check();
    await this.enterRole.fill('QA');
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }

  async solicitorC110AApplicationApplicantDetails(){

      await expect.soft(this.representingPersonDetails).toBeVisible();
      await this.representingPersonDetails.getByLabel('First name').fill('John');
      await this.representingPersonDetails.getByLabel('Last name').fill('Somuy');
      await this.page.getByLabel('Group email address (Optional)').fill('privatesol@gmail.com');
      await this.pbaNumber.fill('PBA1234567');
      await this.customerReference.fill('Customer reference 1000');
      await this.clickContinue();

      await expect.soft(this.page.getByText('People within your organization who need notifications')).toBeVisible();
      await expect.soft(this.page.getByText('HMCTS will contact this person if they have any questions')).toBeVisible();

      await this.mainContactDetails.getByLabel('First name').fill('Maie');
      await this.mainContactDetails.getByLabel('Last name').fill('Nouth');
      await this.mainContactDetails.getByLabel('Phone number', { exact: true }).fill('35346878679876');
      await this.mainContactDetails.getByLabel('Direct email address (').fill('email@email.com');

      await expect.soft(this.page.getByRole('heading', { name: 'Others within your' })).toBeVisible();
      await expect.soft(this.page.getByText('Only people with myHMCTS')).toBeVisible();
      await this.addNew.click();
      await this.otherContactPerson.getByLabel('First name').fill('Johnson');
      await this.otherContactPerson.getByLabel('Last name').fill('Johnson');
      await this.otherContactPerson.getByLabel('Email address').fill('Johnson@hmcts.com');
      await this.otherContactPerson.getByRole('radio', { name: 'Other' }).check();
      await this.otherContactPerson.getByLabel('Enter their role (Optional)').fill('assistant');
      await this.clickContinue();
      await this.checkYourAnsAndSubmit();
  }
}
