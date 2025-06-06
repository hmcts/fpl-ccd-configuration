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
    async addChildDetailsForC110AApplication(){

        expect(this.page.getByText('Include any middle names and check that the spelling is correct to avoid delays.')).toBeVisible();
        await this.page.getByRole('textbox', { name: 'First name' }).fill('First ');
        expect(this.page.getByText('This is the family name of the child. Again, check that spelling is accurate.')).toBeVisible();
        await this.page.getByRole('textbox', { name: 'Last name' }).fill('Child');

        await this.page.getByRole('textbox', { name: 'Day' }).fill('12');
        await this.page.getByRole('textbox', { name: 'Month' }).fill('4');
        await this.page.getByRole('textbox', { name: 'Year' }).fill('2020');
        await this.page.getByLabel('What was the child\'s sex at').selectOption('Indeterminate');
        await this.page.getByRole('textbox', { name: 'What gender do they identify' }).fill('Baby');
        await this.page.getByRole('radio', { name: 'Living with respondents' }).check();
        await this.enterPostCode('EN4');
        await this.page.getByRole('group', { name: 'Do you need to keep the address confidential?' }).getByLabel('Yes').check();
        expect(this.page.getByText('For example, place baby in local authority foster care until further assessments are completed. Supervised contact for parents will be arranged.')).toBeVisible();
        await this.page.getByRole('textbox', { name: 'Brief summary of care and' }).fill('Brief summary');
        expect(this.page.getByText('List any events HMCTS will need to take into account when scheduling hearings. For example, child starting primary school or taking GCSEs.')).toBeVisible();
        await this.page.getByRole('textbox', { name: 'Important dates we need to' }).fill('Dates for KS2 exams 20-05-2025');
        await this.page.getByRole('group', { name: 'Does the child have any additional needs? (Optional)' }).getByLabel('Yes').check();
        expect(this.page.getByText('For example, physical or learning disabilities, severe allergies or conditions that need to be taken into account.')).toBeVisible();
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

        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }


    async assertChildConfidentialInformation(){
        await expect(this.page.locator('ccd-read-collection-field')).toMatchAriaSnapshot(`
          - cell /Child 1 Party First name First Last name Child Child's current living situation Living with respondents Building and Street \\d+ Crescent Road Town or City Barnet Postcode\\/Zipcode EN4 9RS Country United Kingdom Name of social worker Social Samy Social worker's telephone number \\d+ Social worker's email socialWorker@email\\.com/:
            - term: Child 1
            - definition
            - table:
              - rowgroup:
                - row /Party First name First Last name Child Child's current living situation Living with respondents Building and Street \\d+ Crescent Road Town or City Barnet Postcode\\/Zipcode EN4 9RS Country United Kingdom Name of social worker Social Samy Social worker's telephone number \\d+ Social worker's email socialWorker@email\\.com/:
                  - cell /Party First name First Last name Child Child's current living situation Living with respondents Building and Street \\d+ Crescent Road Town or City Barnet Postcode\\/Zipcode EN4 9RS Country United Kingdom Name of social worker Social Samy Social worker's telephone number \\d+ Social worker's email socialWorker@email\\.com/:
                    - term: Party
                    - definition
                    - table:
                      - rowgroup:
                        - row "First name First":
                          - cell "First name"
                          - cell "First"
                        - row "Last name Child":
                          - cell "Last name"
                          - cell "Child"
                        - row "Child's current living situation Living with respondents":
                          - cell "Child's current living situation"
                          - cell "Living with respondents"
                        - row /Building and Street \\d+ Crescent Road Town or City Barnet Postcode\\/Zipcode EN4 9RS Country United Kingdom/:
                          - cell /Building and Street \\d+ Crescent Road Town or City Barnet Postcode\\/Zipcode EN4 9RS Country United Kingdom/:
                            - term
                            - definition
                            - table:
                              - rowgroup:
                                - row /Building and Street \\d+ Crescent Road/:
                                  - cell "Building and Street"
                                  - cell /\\d+ Crescent Road/
                                - row "Town or City Barnet":
                                  - cell "Town or City"
                                  - cell "Barnet"
                                - row "Postcode/Zipcode EN4 9RS":
                                  - cell "Postcode/Zipcode"
                                  - cell "EN4 9RS"
                                - row "Country United Kingdom":
                                  - cell "Country"
                                  - cell "United Kingdom"
                        - row "Name of social worker Social Samy":
                          - cell "Name of social worker"
                          - cell "Social Samy"
                        - row /Social worker's telephone number \\d+/:
                          - cell /Social worker's telephone number \\d+/:
                            - term
                            - definition
                            - table:
                              - rowgroup:
                                - row /Social worker's telephone number \\d+/:
                                  - cell "Social worker's telephone number"
                                  - cell /\\d+/
                        - row "Social worker's email socialWorker@email.com":
                          - cell "Social worker's email"
                          - cell "socialWorker@email.com"
          `);

    }
}
