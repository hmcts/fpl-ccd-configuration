import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";


export class ChildDetails extends BasePage{
    readonly firstName: Locator;
    readonly lastName: Locator;
    readonly dobDay: Locator;
    readonly dobMonth: Locator;
    readonly dobYear: Locator;
    readonly gender: Locator;
    readonly startLiving: Locator;
    readonly slDay: Locator; //SL = start living
    readonly slMonth: Locator;
    readonly slYear: Locator;
    readonly postcode: Locator;
    readonly findAddress: Locator;
    readonly selectAddress: Locator;
    readonly keyDates: Locator;
    readonly briefSummaryCare: Locator;
    readonly adoption: Locator;
    readonly placeOrderApp: Locator; //Are you submitting an application for a placement order? (Optional)
    readonly courtApp: Locator; //'Which court are you applying to? (Optional)'
    readonly motherName: Locator;
    readonly fatherName: Locator;
    readonly fatherParentalResponsibility: Locator;
    readonly socialWorkerName: Locator;
    readonly telephone: Locator;
    readonly personToContact: Locator;
    readonly additionalNeeds: Locator;
    readonly contactDetailsHidden: Locator;
    readonly litigationCapability: Locator;
    readonly childHaveRepresentative: Locator;
    readonly representativeFirstName: Locator;
    readonly  representativeLastName: Locator;
    readonly representativeTelephone: Locator;
    readonly representativeEmail: Locator;
    readonly representativeOrgSearch: Locator;
    readonly applyToAllChildren: Locator;
    private childgroup: any;
    private child: any;
    private selectFPLSolicitorOrganisation: Locator;
    private selectPrivateOrganisation: Locator;
    readonly unregisteredOrganisation: Locator;


    constructor(page:Page){
        super(page);
        this.firstName = page.getByLabel('*First name (Optional)');
        this.lastName = page.getByLabel('*Last name (Optional)');
        this.dobDay = page.getByRole('textbox', { name: 'Day' });
        this.dobMonth = page.getByRole('textbox', { name: 'Month' });
        this.dobYear = page.getByRole('textbox', { name: 'Year' });
        this.gender = page.getByLabel('*Gender (Optional)');
        this.startLiving = page.getByRole('group', { name: 'What date did they start' });
        this.slDay = this.startLiving.getByLabel('Day');
        this.slMonth = this.startLiving.getByLabel('Month');
        this.slYear = this.startLiving.getByLabel('Year');
        this.postcode = page.getByLabel('Enter a UK postcode');
        this.findAddress = page.getByRole('button', { name: 'Find address' });
        this.selectAddress = page.getByLabel('Select an address');
        this.keyDates = page.getByLabel('Key dates for this child (Optional)');
        this.briefSummaryCare = page.getByLabel('Brief summary of care and');
        this.adoption = page.getByRole('group', { name: 'Are you considering adoption at this stage? (Optional)' });
        this.placeOrderApp = page.getByRole('group', { name: 'Are you submitting an application for a placement order? (Optional)' });
        this.courtApp = page.getByLabel('Which court are you applying to? (Optional)');
        this.motherName = page.getByLabel('Mother\'s full name (Optional)');
        this.fatherName = page.getByLabel('Father\'s full name (Optional)');
        this.fatherParentalResponsibility = page.getByLabel('Does the father have parental responsibility? (Optional)');
        this.socialWorkerName = page.getByLabel('Name of social worker (Optional)')
        this.telephone = page.getByLabel('Telephone number (Optional)');
        this.personToContact = page.getByLabel('Name of person to contact (Optional)')
        this.additionalNeeds = page.getByRole('group', { name: 'Does the child have any additional needs? (Optional)' });
        this.contactDetailsHidden = page.getByRole('group', { name: 'Do you need contact details hidden from other parties? (Optional)' });
        this.litigationCapability = page.getByRole('group', { name: 'Do you believe this child' });
        this.childHaveRepresentative = page.locator('#childrenHaveRepresentation');
        this.representativeFirstName =page.getByLabel('Representative\'s first name');
        this.representativeLastName =page.getByLabel('Representative\'s last name');
        this.representativeEmail=page.getByLabel('Email address');
        this.representativeOrgSearch=page.getByLabel('You can only search for');
        this.selectPrivateOrganisation = page.getByTitle('Select the organisation Private solicitors', { exact: true });
        this.selectFPLSolicitorOrganisation = page.getByTitle('Select the organisation FPLSolicitorOrg', { exact: true });
        this.representativeTelephone = page.getByRole('group', { name: 'Telephone number' });
        this.applyToAllChildren = page.getByRole('group', { name: 'Do all the children have this' });
       // this.applyToAllChildren = page.getByRole('radio', { name: 'Yes' });
        this.childgroup =page.getByRole('group', { name: `${(this.child)}`});
        this.unregisteredOrganisation =page.getByLabel('Organisation name (Optional)');


    }

    async childDetailsNeeded(){
        await this.firstName.click();
        await this.firstName.fill('Susan');
        await this.lastName.click();
        await this.lastName.fill('Brown');
        await this.dobDay.click();
        await this.dobDay.fill('10');
        await this.dobMonth.click();
        await this.dobMonth.fill('1');
        await this.dobYear.click();
        await this.dobYear.fill('2019');
        await this.gender.selectOption('2: Girl');
        await this.page.getByLabel('Living with respondents').click();
        await this.page.getByLabel('Living with respondents').click(); //duplicated line is NOT an error - solves issue with checkbox not being able to be checked.
        await expect(this.page.getByLabel('Living with respondents')).toBeChecked(); //needed due to flakiness of checking the box.
        await this.slDay.click();
        await this.slDay.fill('1');
        await this.slMonth.click();
        await this.slMonth.fill('2');
        await this.slYear.click();
        await this.slYear.fill('2022');
        await this.postcode.fill('BN26 6AL');
        await this.findAddress.click();
        await this.selectAddress.selectOption('1: Object');
        await this.keyDates.click();
        await this.keyDates.fill('these are the key dates');
        await this.briefSummaryCare.click();
        await this.briefSummaryCare.fill('this is the brief summary of care and contact plan');
        await this.adoption.getByLabel('No').check();
        await this.motherName.click();
        await this.motherName.fill('Claire Brown');
        await this.fatherName.click();
        await this.fatherName.fill('Charles Brown');
        await this.fatherParentalResponsibility.selectOption('1: Yes');
        await this.socialWorkerName.click();
        await this.socialWorkerName.fill('Robert Taylor');
        await this.telephone.click();
        await this.telephone.fill('0123456789');
        await this.personToContact.click();
        await this.personToContact.fill('Jane Smith');
        await this.additionalNeeds.getByLabel('No').check();
        await this.contactDetailsHidden.getByLabel('No').check();
        await this.litigationCapability.getByLabel('No', { exact: true }).click();
        await this.clickContinue();
        await this.checkYourAnswersHeader.isVisible;
        await this.checkYourAnsAndSubmit();
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
        await this.childHaveRepresentative.getByText('Yes').click();
        await this.representativeFirstName.fill('Child Solicitor');
        await this.representativeLastName.fill('One');
        await this.representativeEmail.fill('solicitor@email.com');
        await this.unregisteredOrganisation.fill('NewOrganisation');
        await this.postcode.fill('TW7');
        await this.findAddress.click();
        await this.selectAddress.selectOption('1: Object');
        await this.representativeTelephone.locator('#childrenMainRepresentative_telephoneNumber_telephoneNumber').fill('012345678');

    }

    async  assignSolicitorToAllChildren(){
        await this.applyToAllChildren.getByRole('radio', { name: 'Yes' }).check();
    }

    async  assignDifferrentChildSolicitor(){
        await this.applyToAllChildren.getByRole('radio', { name: 'No' }).check();
    }

    async addDifferentSolicitorForChild(child:string){
        await this.page.getByRole('group', { name: `${(child)}`}).getByLabel('No').check();
        await this.page.getByRole('group', { name: `${(child)}`}).getByLabel('Representative\'s first name (').fill('child1');
        await this.page.getByRole('group', { name: `${(child)}`}).getByLabel('Representative\'s last name (').fill('private solicitor');
        await this.page.getByRole('group', { name: `${(child)}`}).getByLabel('Email address (Optional)').fill('FPLSolOrg@email.com');
        await this.representativeOrgSearch.fill('FPLSolicitorOrg');
        await this.selectFPLSolicitorOrganisation.click();

    }

    async addCafcassSolicitorForChild(child:string){
        await this.page.getByRole('group', { name: `${(child)}`}).getByLabel('Yes').check();
    }

}
