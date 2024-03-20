import { type Page, type Locator } from "@playwright/test";
import { BasePage } from "./base-page";


export class ChildDetails extends BasePage{   
    readonly FirstName: Locator;
    readonly LastName: Locator;
    readonly DOB_Day: Locator;
    readonly DOB_Month: Locator;
    readonly DOB_Year: Locator;
    readonly Gender: Locator;
    readonly ChildLivingSituation_LivingWithRespondents: Locator;
    readonly StartLiving: Locator;
    readonly SL_Day: Locator; //SL = start living
    readonly SL_Month: Locator;
    readonly SL_Year: Locator;
    readonly Postcode: Locator;
    readonly FindAddress: Locator;
    readonly SelectAddress: Locator;
    readonly KeyDates: Locator;
    readonly BriefSummaryCare: Locator;
    readonly Adoption: Locator;
    readonly PlaceOrderApp: Locator; //Are you submitting an application for a placement order? (Optional)
    readonly CourtApp: Locator; //'Which court are you applying to? (Optional)'
    readonly MotherName: Locator;
    readonly FatherName: Locator;
    readonly FatherParentalResponsibility: Locator;
    readonly SocialWorkerName: Locator;
    readonly Telephone: Locator;
    readonly PersonToContact: Locator;
    readonly AdditionalNeeds: Locator;
    readonly ContactDetailsHidden: Locator;
    readonly LitigationCapability: Locator;

    constructor(page:Page){
        super(page);
        this.FirstName = page.getByLabel('*First name (Optional)');
        this.LastName = page.getByLabel('*Last name (Optional)');
        this.DOB_Day = page.getByRole('textbox', { name: 'Day' });
        this.DOB_Month = page.getByRole('textbox', { name: 'Month' });
        this.DOB_Year = page.getByRole('textbox', { name: 'Year' });
        this.Gender = page.getByLabel('*Gender (Optional)');
        this.StartLiving = page.getByRole('group', { name: 'What date did they start' });
        this.SL_Day = this.StartLiving.getByLabel('Day');
        this.SL_Month = this.StartLiving.getByLabel('Month');
        this.SL_Year = this.StartLiving.getByLabel('Year');
        this.Postcode = page.getByLabel('Enter a UK postcode');
        this.FindAddress = page.getByRole('button', { name: 'Find address' });
        this.SelectAddress = page.getByLabel('Select an address');
        this.KeyDates = page.getByLabel('Key dates for this child (Optional)');
        this.BriefSummaryCare = page.getByLabel('Brief summary of care and');
        this.Adoption = page.getByRole('group', { name: 'Are you considering adoption at this stage? (Optional)' });
        this.PlaceOrderApp = page.getByRole('group', { name: 'Are you submitting an application for a placement order? (Optional)' });
        this.CourtApp = page.getByLabel('Which court are you applying to? (Optional)');
        this.MotherName = page.getByLabel('Mother\'s full name (Optional)');
        this.FatherName = page.getByLabel('Father\'s full name (Optional)');
        this.FatherParentalResponsibility = page.getByLabel('Does the father have parental responsibility? (Optional)');
        this.SocialWorkerName = page.getByLabel('Name of social worker (Optional)')
        this.Telephone = page.getByLabel('Telephone number (Optional)');
        this.PersonToContact = page.getByLabel('Name of person to contact (Optional)')
        this.AdditionalNeeds = page.getByRole('group', { name: 'Does the child have any additional needs? (Optional)' });
        this.ContactDetailsHidden = page.getByRole('group', { name: 'Do you need contact details hidden from other parties? (Optional)' });
        this.LitigationCapability = page.getByRole('group', { name: 'Do you believe this child will have problems with litigation capacity (understanding what\'s happening in the case)? (Optional)' });
    }

    async childDetailsNeeded(){
        await this.FirstName.click();
        await this.FirstName.fill('Susan');
        await this.LastName.click();
        await this.LastName.fill('Brown');
        await this.DOB_Day.click();
        await this.DOB_Day.fill('10');
        await this.DOB_Month.click();
        await this.DOB_Month.fill('1');
        await this.DOB_Year.click();
        await this.DOB_Year.fill('2019');
        await this.Gender.selectOption('2: Girl');
        await this.page.getByLabel('Living with other family or').click({ force: true });
        await this.SL_Day.click();
        await this.SL_Day.fill('1');
        await this.SL_Month.click();
        await this.SL_Month.fill('2');
        await this.SL_Year.click();
        await this.SL_Year.fill('2022');
        await this.Postcode.fill('BN26 6AL');
        await this.FindAddress.click();
        await this.KeyDates.click();
        await this.KeyDates.fill('these are the key dates');
        await this.BriefSummaryCare.click();
        await this.BriefSummaryCare.fill('this is the brief summary of care and contact plan');
        await this.Adoption.getByLabel('No').check();
        await this.MotherName.click();
        await this.MotherName.fill('Claire Brown');
        await this.FatherName.click();
        await this.FatherName.fill('Charles Brown');
        await this.FatherParentalResponsibility.selectOption('1: Yes');
        await this.SocialWorkerName.click();
        await this.SocialWorkerName.fill('Robert Taylor');
        await this.Telephone.click();
        await this.Telephone.fill('0123456789');
        await this.PersonToContact.click();
        await this.PersonToContact.fill('Jane Smith');
        await this.AdditionalNeeds.getByLabel('No').check();
        await this.ContactDetailsHidden.getByLabel('No').check();
        await this.LitigationCapability.getByLabel('No').check();
        await this.clickContinue();
        await this.checkYourAnswersHeader.isVisible;
        await this.checkYourAnsAndSubmit();
    }
}
