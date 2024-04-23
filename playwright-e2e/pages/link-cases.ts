import { type Page, type Locator } from "@playwright/test";
import { BasePage } from "./base-page";

export class CaseLink extends BasePage
{

    readonly caseNumber: Locator;
    readonly libkreason: Locator;
    readonly propose: Locator;
    readonly proposeLink: Locator;
    readonly next: Locator;
    readonly submit: Locator;


    constructor(page:Page){
        super(page);
        this.caseNumber =page.locator('#width-20');
        this.proposeLink = page.getByRole('button', { name: 'Propose case link' });
        this.next =  page.getByRole('button', { name: 'Next' });
        this.submit =page. getByRole('button', { name: 'Submit' })


    }
    async clickNext(){
        await this.next.click();
    }

    async proposeCaseLink(caseNumber: string,linkreason :string[] ){
        await this.caseNumber.fill(caseNumber);
        for (var  linktype of linkreason){
            await this.page.getByLabel(linktype).check();
        }
        await this.proposeLink.click();
    }
  hypenateCaseNumber( caseNumber : string){

        let hypenatedCaseNumber: string;
        hypenatedCaseNumber = caseNumber.slice(0,4)+"-" + caseNumber.slice(4,8) + "-" +caseNumber.slice(8,12) + "-" +caseNumber.slice(12,16);
        return  hypenatedCaseNumber
}
async submitCaseLink(){
    this.submit.click();
}




}
