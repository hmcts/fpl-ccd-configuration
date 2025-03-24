import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class OtherProceedings extends BasePage {

    readonly otherProceedingsHeading: Locator;
    addProceeding: Locator;
    proceedingTwo: Locator;
    proceedingOne: Locator;

    public constructor(page: Page) {
        super(page);
        this.otherProceedingsHeading = page.locator('#proceedings').getByRole('heading', {name: 'Other proceedings'})
        this.addProceeding = page.getByRole('button', {name: 'Add new'});
        this.proceedingOne = page.locator('#proceedings_0_0');
        this.proceedingTwo = page.locator('#proceedings_1_1');
    }


    async otherProceedings() {
        await expect(this.otherProceedingsHeading).toBeVisible();
        await this.addProceeding.nth(0).click();
        await this.proceedingOne.getByLabel('Ongoing').check();
        await this.proceedingOne.getByRole('textbox', {name: 'Day'}).fill('12');
        await this.proceedingOne.getByRole('textbox', {name: 'Month'}).fill('4');
        await this.proceedingOne.getByRole('textbox', {name: 'Year'}).fill('2024');
        await this.proceedingOne.getByLabel('Case number').fill('125756805769');
        await this.proceedingOne.getByLabel('Orders made').fill('EPO');
        await this.proceedingOne.getByLabel('Judge').fill('District judge Susan');
        await this.proceedingOne.getByLabel('Names of children involved').fill('Baby srah\nMilont ');
        await this.proceedingOne.getByLabel('Name of guardian').fill('Joby marian');
        await this.proceedingOne.getByRole('radio', {name: 'Yes'}).check();
        await this.addProceeding.nth(1).click();

        await this.proceedingTwo.getByLabel('Previous').click();
        await this.proceedingTwo.getByLabel('Previous').click();
        await this.proceedingTwo.filter({hasText: 'Application submission date'}).locator('#started-day').fill('3');
        await this.proceedingTwo.filter({hasText: 'Application submission date'}).locator('#started-month').fill('4');
        await this.proceedingTwo.filter({hasText: 'Application submission date'}).locator('#started-year').fill('2010');
        await this.proceedingTwo.filter({hasText: 'Final order date'}).locator('#ended-day').fill('4');
        await this.proceedingTwo.filter({hasText: 'Final order date'}).locator('#ended-month').fill('6');
        await this.proceedingTwo.filter({hasText: 'Final order date'}).locator('#ended-year').fill('2019');
        await this.proceedingTwo.getByLabel('Case number').fill('45756867989');
        await this.proceedingTwo.getByLabel('Orders made').fill('Care order c12');
        await this.proceedingTwo.getByLabel('Judge').fill('District judge Joe Bloggs');
        await this.proceedingTwo.getByLabel('Names of children involved').fill('Baby Julie\nMilont ');
        await this.proceedingTwo.getByLabel('Name of guardian').fill('Joby Susam');
        await this.proceedingTwo.getByRole('radio', {name: 'No'}).check();
        await this.proceedingTwo.getByRole('textbox', {name: 'Give reason'}).fill('The guardian was moved to aboard');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();

    }
}
