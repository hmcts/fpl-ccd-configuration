import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class OtherPeopleInCase extends BasePage {
  readonly otherPeopleHeading: Locator;
  readonly fullName: Locator;
  readonly dobDay: Locator;
  readonly dobMonth: Locator;
  readonly dobYear: Locator;
  readonly gender: Locator;
  readonly placeOfBirth: Locator;
  readonly currentAddress: Locator;
  readonly reasonUnknownAddress: Locator;
  readonly telephoneNumber: Locator;
  readonly relationshipToChild: Locator;
  readonly contactDetailsHidden: Locator;
  readonly addNew: Locator;

  public constructor(page: Page) {
    super(page);
    this.otherPeopleHeading = page.getByRole("heading", {name: "Other people in the case", exact: true});
    this.fullName = page.getByLabel('Full name (Optional)');
    this.dobDay = page.getByLabel('Day');
    this.dobMonth = page.getByLabel('Month');
    this.dobYear = page.getByLabel('Year');
    this.gender = page.getByLabel('Gender (Optional)');
    this.placeOfBirth = page.getByLabel('Place of birth (Optional)');
    this.currentAddress = page.getByRole('group', { name: '*Current address known? (' });
    this.reasonUnknownAddress = page.getByLabel('*Reason the address is not');
    this.telephoneNumber = page.getByLabel('Telephone number (Optional)');
    this.relationshipToChild = page.getByText('What is this person\'s relationship to the child or children in this case? (Optional)');
    this.contactDetailsHidden = page.getByRole('group', { name: 'Do you need contact details' });
    this.addNew = page.getByRole('button', { name: 'Add new' });

  }

  async personOneToBeGivenNotice() {
    await this.fullName.fill("John Doe");
    await this.dobDay.fill("1");
    await this.dobMonth.fill("10");
    await this.dobYear.fill("1990")
    await this.gender.selectOption('1: Male');
    await this.placeOfBirth.fill("London");
    await this.currentAddress.getByLabel('No').check();
    await this.reasonUnknownAddress.selectOption('1: No fixed abode');
    await this.telephoneNumber.fill("0123456789");
    await this.relationshipToChild.fill("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam fermentum augue velit, eget bibendum est viverra vel. Sed id urna mollis")
    await this.contactDetailsHidden.getByLabel('No').check();
    await this.page.getByLabel('Don\'t know').check();
  }

  async personTwoToBeGivenNotice() {
    await this.addNew.click();
    await this.page.locator('#others_additionalOthers_0_name').fill('Jane Doe');
    await this.page.locator('#others_additionalOthers #DOB-day').fill("2");
    await this.page.locator('#others_additionalOthers #DOB-month').fill("11");
    await this.page.locator('#others_additionalOthers #DOB-year').fill("1999");
    await this.page.locator('#others_additionalOthers_0_gender').selectOption('2: Female');
    await this.page.locator('#others_additionalOthers_0_birthPlace').fill("Leeds");
    await this.page.locator('#others_additionalOthers_0_addressKnowV2-No').check();
    await this.page.locator('#others_additionalOthers_0_addressNotKnowReason').selectOption('1: No fixed abode');
    await this.page.locator('#others_additionalOthers_0_telephone').fill("0123456789");
    await this.page.locator('#others_additionalOthers_0_childInformation').fill("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam fermentum augue velit, eget bibendum est viverra vel. Sed id urna mollis");
    await this.page.locator('#others_additionalOthers_0_detailsHidden_No').check();
    await this.page.locator('#others_additionalOthers_0_litigationIssues-NO').check();
  }

  async continueAndCheck(){
    await this.clickContinue();
    await this.page.getByText("John Doe", { exact: true });
    await this.page.getByText("1990", { exact: true });
    await this.page.getByText("London", { exact: true });
    await this.page.getByText("0123456789", { exact: true });
    await this.checkYourAnsAndSubmit();
  }
}

