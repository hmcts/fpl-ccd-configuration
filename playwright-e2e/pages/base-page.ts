import { type Page, type Locator } from "@playwright/test";

export abstract class BasePage {

  readonly nextStep: Locator;
  readonly go:Locator;
  readonly page: Page ;
  readonly saveAndContinue : Locator;


  constructor(page : Page){
    this.page=page;
    this.nextStep = page.getByLabel('Next step');
    this.go=  page.getByRole('button', { name: 'Go' });
    this.saveAndContinue =  page.getByRole('button', { name: 'Save and continue' });
  }
async gotoNextStep(eventName:string)
{
  await this.nextStep.selectOption(eventName);
  await this.go.click();
}
async checkYourAnsAndSubmit(){

  await this.saveAndContinue.click();
}

async tabNavigation(tabName :string){

  await this.page.getByText(tabName).click();

}


}


