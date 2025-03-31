import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";


export class ChildDetails extends BasePage{
    get firstName(): Locator {
        return this.page.getByLabel('*First name (Optional)');
    }

    get lastName(): Locator {
        return this.page.getByLabel('*Last name (Optional)');
    }

    get dobDay(): Locator {
        return this.page.getByRole('textbox', { name: 'Day' });
    }

    get dobMonth(): Locator {
        return this.page.getByRole('textbox', { name: 'Month' });
    }

    get dobYear(): Locator {
        return this.page.getByRole('textbox', { name: 'Year' });
    }

    get gender(): Locator {
        return this.page.getByLabel('*Gender (Optional)');
    }

    get startLiving(): Locator {
        return this.page.getByRole('group', { name: 'What date did they start' });
    }

    get slDay(): Locator {
        return this.startLiving.getByLabel('Day');
    }

    get slMonth(): Locator {
        return this.startLiving.getByLabel('Month');
    }

    get slYear(): Locator {
        return this.startLiving.getByLabel('Year');
    }

    get postcode(): Locator {
        return this.page.getByRole('textbox', { name: 'Enter a UK postcode' });
    }

    get findAddress(): Locator {
        return this.page.getByRole('button', { name: 'Find address' });
    }

    get selectAddress(): Locator {
        return this.page.getByLabel('Select an address');
    }

    get keyDates(): Locator {
        return this.page.getByLabel('Key dates for this child (Optional)');
    }

    get briefSummaryCare(): Locator {
        return this.page.getByLabel('Brief summary of care and');
    }

    get adoption(): Locator {
        return this.page.getByRole('group', { name: 'Are you considering adoption at this stage? (Optional)' });
    }

    get placeOrderApp(): Locator {
        return this.page.getByRole('group', { name: 'Are you submitting an application for a placement order? (Optional)' });;
    }

    get courtApp(): Locator {
        return this.page.getByLabel('Which court are you applying to? (Optional)');;
    }

    get motherName(): Locator {
        return this.page.getByLabel('Mother\'s full name (Optional)');
    }

    get fatherName(): Locator {
        return this.page.getByLabel('Father\'s full name (Optional)');
    }

    get fatherParentalResponsibility(): Locator {
        return this.page.getByLabel('Does the father have parental responsibility? (Optional)');
    }

    get socialWorkerName(): Locator {
        return this.page.getByLabel('Name of social worker (Optional)');
    }

    get telephone(): Locator {
        return this.page.getByLabel('Telephone number (Optional)');
    }

    get personToContact(): Locator {
        return this.page.getByLabel('Name of person to contact (Optional)');
    }

    get additionalNeeds(): Locator {
        return this.page.getByRole('group', { name: 'Does the child have any additional needs? (Optional)' });
    }

    get contactDetailsHidden(): Locator {
        return this.page.getByRole('group', { name: 'Do you need contact details hidden from other parties? (Optional)' });
    }

    get litigationCapability(): Locator {
        return this.page.getByRole('group', { name: 'Do you believe this child' });
    }

    get childHaveRepresentative(): Locator {
        return this.page.getByRole('group', { name: 'Do you know if any of the' });
    }

    get representativeFirstName(): Locator {
        return this.page.getByLabel('Representative\'s first name');
    }

    get representativeLastName(): Locator {
        return this.page.getByLabel('Representative\'s last name');
    }

    get representativeTelephone(): Locator {
        return this.page.getByRole('group', { name: 'Telephone number' });
    }

    get representativeEmail(): Locator {
        return this.page.getByLabel('Email address');
    }

    get representativeOrgSearch(): Locator {
        return this.page.getByLabel('You can only search for');
    }

    get applyToAllChildren(): Locator {
        return this.page.getByRole('group', { name: 'Do all the children have this' });
    }

    get unregisteredOrganisation(): Locator {
        return this.page.getByLabel('Organisation name (Optional)');
    }

    // get childgroup(): any {
    //     return this.page.getByRole('group', { name: `${(this._child)}` });
    // }

    // get child(): any {
    //     return this._child;
    // }

    get selectFPLSolicitorOrganisation(): Locator {
        return this.page.getByTitle('Select the organisation FPLSolicitorOrg', { exact: true });
    }

    get selectPrivateOrganisation(): Locator {
        return this.page.getByTitle('Select the organisation Private solicitors', { exact: true });
    }
    // private readonly _firstName: Locator;
    // private readonly _lastName: Locator;
    // private readonly _dobDay: Locator;
    // private readonly _dobMonth: Locator;
    // private readonly _dobYear: Locator;
    // private readonly _gender: Locator;
    // private readonly _startLiving: Locator;
    // private readonly _slDay: Locator; //SL = start living
    // private readonly _slMonth: Locator;
    // private readonly _slYear: Locator;
    // private readonly _postcode: Locator;
    // private readonly _findAddress: Locator;
    // private readonly _selectAddress: Locator;
    // private readonly _keyDates: Locator;
    // private readonly _briefSummaryCare: Locator;
    // private readonly _adoption: Locator;
    // private readonly _placeOrderApp: Locator; //Are you submitting an application for a placement order? (Optional)
    // private readonly _courtApp: Locator; //'Which court are you applying to? (Optional)'
    // private readonly _motherName: Locator;
    // private readonly _fatherName: Locator;
    // private readonly _fatherParentalResponsibility: Locator;
    // private readonly _socialWorkerName: Locator;
    // private readonly _telephone: Locator;
    // private readonly _personToContact: Locator;
    // private readonly _additionalNeeds: Locator;
    // private readonly _contactDetailsHidden: Locator;
    // private readonly _litigationCapability: Locator;
    // private readonly _childHaveRepresentative: Locator;
    // private readonly _representativeFirstName: Locator;
    // private readonly _representativeLastName: Locator;
    // private readonly _representativeTelephone: Locator;
    // private readonly _representativeEmail: Locator;
    // private readonly _representativeOrgSearch: Locator;
    // private readonly _applyToAllChildren: Locator;
    // private readonly _unregisteredOrganisation: Locator;
    // private _childgroup: any;
    // private _child: any;
    // private _selectFPLSolicitorOrganisation: Locator;
    // private _selectPrivateOrganisation: Locator;

    // constructor(page:Page){
    //     super(page);
    //     this._firstName =
    //     this._lastName =
    //     this._dobDay =
    //     this._dobMonth =
    //     this._dobYear =
    //     this._gender =
    //     this._startLiving =
    //     this._slDay =
    //     this._slMonth = ;
    //     this._slYear =
    //     this._postcode =
    //     this._findAddress = ;
    //     this._selectAddress = ;
    //     this._keyDates = ;
    //     this._briefSummaryCare = ;
    //     this._adoption =
    //     this._placeOrderApp =
    //     this._courtApp =
    //     this._motherName = ;
    //     this._fatherName =
    // this._fatherParentalResponsibility =
    //     this._socialWorkerName =
    //     this._telephone =
    //     this._personToContact =
    //     this._additionalNeeds =
    //     this._contactDetailsHidden =
    //     this._litigationCapability =
    //     this._childHaveRepresentative =
    //     this._representativeFirstName =
    //     this._representativeLastName =
    //     this._representativeEmail =
    //     this._representativeOrgSearch =
    //     this._selectPrivateOrganisation =
    //     this._selectFPLSolicitorOrganisation =
    //     this._representativeTelephone =
    //     this._applyToAllChildren =
    //     this._childgroup =
    //     this._unregisteredOrganisation =
    //
    //
    // }

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
        //await expect(this.checkYourAnswersHeader).toBeVisible();
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
        await this.childHaveRepresentative.getByText('Yes', { exact: true }).click();
        await this.representativeFirstName.fill('Child Solicitor');
        await this.representativeLastName.fill('One');
        await this.representativeEmail.fill('solicitor@email.com');
        await this.unregisteredOrganisation.fill('NewOrganisation');
        await this.postcode.fill('TW7');
        await this.findAddress.click();
        await this.selectAddress.selectOption({index: 1});
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
}
