import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";


export class ChildDetails extends BasePage{
    readonly childHaveRepresentative: Locator;
    readonly representativeFirstName: Locator;
    readonly representativeLastName: Locator;
    readonly representativeTelephone: Locator;
    readonly representativeEmail: Locator;
    readonly representativeOrgSearch: Locator;
    readonly applyToAllChildren: Locator;
    readonly unregisteredOrganisation: Locator;
    private childgroup: any;
    private child: any;
    private selectFPLSolicitorOrganisation: Locator;
    private selectPrivateOrganisation: Locator;

    constructor(page:Page){
        super(page);

        this.childHaveRepresentative = page.getByRole('group', { name: 'Do you know if any of the' });
        this.representativeFirstName = page.getByLabel('Representative\'s first name');
        this.representativeLastName = page.getByLabel('Representative\'s last name');
        this.representativeEmail = page.getByLabel('Email address');
        this.representativeOrgSearch = page.getByLabel('You can only search for');
        this.selectPrivateOrganisation = page.getByTitle('Select the organisation Private solicitors', { exact: true });
        this.selectFPLSolicitorOrganisation = page.getByTitle('Select the organisation FPLSolicitorOrg', { exact: true });
        this.representativeTelephone = page.getByRole('group', { name: 'Telephone number' });
        this.applyToAllChildren = page.getByRole('group', { name: 'Do all the children have this' });
        this.childgroup = page.getByRole('group', { name: `${(this.child)}` });
        this.unregisteredOrganisation = page.getByLabel('Organisation name (Optional)');


    }


    async addRegisteredSOlOrg(){
        await this.childHaveRepresentative.getByText('Yes').click();
        await this.representativeFirstName.fill('Child Solicitor');
        await this.representativeLastName.fill('One');
        await this.representativeEmail.fill('solicitor@email.com');
        await this.representativeOrgSearch.fill('Private solicitors');
        await this.selectPrivateOrganisation.click();
        await this.representativeTelephone.locator('#childrenMainRepresentative_telephoneNumber_telephoneNumber').fill('012345678');
    }

    async addUnregisteredSolOrg(){
        await this.childHaveRepresentative.getByText('Yes', { exact: true }).click();
        await this.representativeFirstName.fill('Child Solicitor');
        await this.representativeLastName.fill('One');
        await this.representativeEmail.fill('solicitor@email.com');
        await this.unregisteredOrganisation.fill('NewOrganisation');
        await this.enterPostCode('TW7')
        await this.representativeTelephone.locator('#childrenMainRepresentative_telephoneNumber_telephoneNumber').fill('012345678');
    }

    async assignSolicitorToAllChildren(){
        await this.applyToAllChildren.getByRole('radio', { name: 'Yes' }).check();
    }

    async assignDifferrentChildSolicitor(){
        await this.applyToAllChildren.getByRole('radio', { name: 'No' }).check();
    }

    async addDifferentSolicitorForChild(child: string){
        await this.page.getByRole('group', { name: `${(child)}` }).getByLabel('No').check();
        await this.page.getByRole('group', { name: `${(child)}` }).getByLabel('Representative\'s first name (').fill('child1');
        await this.page.getByRole('group', { name: `${(child)}` }).getByLabel('Representative\'s last name (').fill('private solicitor');
        await this.page.getByRole('group', { name: `${(child)}` }).getByLabel('Email address (Optional)').fill('FPLSolOrg@email.com');
        await this.representativeOrgSearch.fill('FPLSolicitorOrg');
        await this.selectFPLSolicitorOrganisation.click();
    }

    async addCafcassSolicitorForChild(child: string){
        await this.page.getByRole('group', { name: `${(child)}` }).getByLabel('Yes').check();
    }

    async removeSolicitor(){
        await this.childHaveRepresentative.getByText('No', { exact: true }).click();
    }

    async addChildDetailsForC110AApplication(): Promise<void> {
        await this.page.getByRole('textbox', { name:'First name' }).fill('test');
        await this.page.getByRole('textbox', { name: 'Last name' }).fill('test');

        await this.page.getByRole('textbox', { name: 'Day' }).fill('01');
        await this.page.getByRole('textbox', { name: 'Month' }).fill('01');
        await this.page.getByRole('textbox', { name: 'Year' }).fill('2020');

        await this.page.getByLabel('What was the child\'s sex at birth').selectOption('Indeterminate');
        await this.page.getByRole('textbox', { name: 'What gender do they identify' }).fill('Non-binary');
        await this.page.getByRole('radio', { name: 'Living with respondents' }).check();
        await this.enterPostCode('SW1A 0AA');
        await this.page.getByRole('group', { name: 'Do you need to keep the address confidential?' }).getByLabel('Yes').check();
        await this.page.getByRole('textbox', { name: 'Brief summary of care and contact plan' }).fill('Brief summary');
        await this.page.getByRole('textbox', { name: 'Important dates we need to consider when scheduling hearings' }).fill('Dates for KS2 exams 20-05-2025');
        await this.page.getByRole('group', { name: 'Does the child have any additional needs? (Optional)' }).getByLabel('Yes').check();
        await this.page.getByRole('textbox', { name: 'Give details (Optional)' }).fill('Allegeries to nuts');
        await this.page.getByRole('textbox', { name: 'Birth mother\'s full name (' }).fill('Mother');
        await this.page.getByRole('textbox', { name: 'Birth father\'s full name (' }).fill('Father');
        await this.page.getByRole('group', { name: 'Is adoption being considered at this stage? (Optional)' }).getByLabel('Yes').check();
        await this.page.getByRole('group', { name: 'Are you submitting an application for a placement order? (Optional)' }).getByLabel('Yes').check();
        await this.page.getByRole('textbox', { name: 'Which court are you applying' }).fill('barnet court');
        await this.page.getByRole('textbox', { name: 'Name of social worker (' }).fill('Social Samy');
        await this.page.getByRole('textbox', { name: 'Social worker\'s telephone' }).fill('0564575685');
        await this.page.getByRole('textbox', { name: 'Social worker\'s email (' }).fill('socialWorker@email.com');
        await this.page.getByRole('group', { name: 'Do you need social worker' }).getByLabel('Yes').check();
        await this.page.getByRole('textbox', { name: 'Give a reason (Optional)' }).fill('Test');

        await Promise.all([
            this.page.waitForResponse(response =>
                response.url().includes('validate') &&
                response.request().method() === 'POST' &&
                response.status() === 200
            ),
            await this.clickContinue()
        ]);

        await Promise.all([
            this.page.waitForResponse(response =>
                response.url().includes('/get') &&
                response.request().method() === 'GET' &&
                response.status() === 200
            ),
            await this.clickSaveAndContinue()
        ]);

    }


    async assertChildConfidentialInformation() {
        await expect(this.page.getByRole('cell', {name: 'First name', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'First', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Last name', exact: true})).toBeVisible();
        await expect(this.page.getByRole('row', {name: 'Last name Child', exact: true}).locator('td')).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Child\'s current living situation', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Living with respondents', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Building and Street', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: '2 Sussex Way', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Address Line 3', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Cockfosters', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Town or City', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Barnet', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Postcode/Zipcode', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'EN4 0BJ', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Country', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'United Kingdom', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Name of social worker', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Social Samy', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Social worker\'s telephone number', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: '0564575685', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'Social worker\'s email', exact: true})).toBeVisible();
        await expect(this.page.getByRole('cell', {name: 'socialWorker@email.com', exact: true})).toBeVisible();

    }
}
