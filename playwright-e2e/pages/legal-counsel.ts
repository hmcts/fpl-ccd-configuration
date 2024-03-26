import { type Page, type Locator } from "@playwright/test";
import { BasePage } from "./base-page";

export class LegalCounsel extends BasePage
{
  readonly addCounsel: Locator;
readonly firstName: Locator;
readonly lastName: Locator;
readonly email: Locator;
readonly organisation: Locator;
readonly selectOrg: Locator;


    constructor(page:Page){
        super(page);
        this.addCounsel = page.getByRole('button', { name: 'Add new' });
        this.firstName =  page.getByLabel('First name');
        this.lastName = page.getByLabel('Last name');
        this.email = page.getByLabel('Email address');
        this.organisation = page.getByLabel('You can only search for');
        this.selectOrg = page.getByRole('link', { name: 'Select' });
        
    }

    async addLegalCounsel(){
        await this.addCounsel.click();

    }

    async enterLegalCounselDetails(){

        await this.firstName.fill('FPLOrg');
        await this.lastName.fill('Solicitor');
        await this.email.fill('solicitoroneorg2@mailinator.com');
        await this.organisation.fill('FPLSolicitorOrg');
        await this.selectOrg.click();
       
    }



}