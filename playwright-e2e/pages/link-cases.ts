import { type Page, type Locator } from "@playwright/test";
import { BasePage } from "./base-page";

export class CaseLink extends BasePage
{

    readonly caseNumber: Locator;
    readonly libkreason: Locator;
    readonly propose: Locator;
    readonly proposeLink: Locator;


    constructor(page:Page){
        super(page);
        this.caseNumber =page.getByLabel('#width-20');
        this.proposeLink = page.getByRole('button', { name: 'Propose case link' });


    }

    async proposeCaseLink(caseNumber: string,linkreason :string[] ){
        await this.caseNumber.fill(caseNumber);
        for (var  linktype of linkreason){
            await this.page.getByLabel(linktype).check();
        }
        await this.proposeLink.click();
    }





}
