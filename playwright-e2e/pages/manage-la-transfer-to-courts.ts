import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ManageLaTransferToCourts extends BasePage {
    readonly manageLaTransferToCourts: Locator;
    readonly caseType: Locator;
    readonly transferAnotherCourt: Locator;
    readonly selectNewCourt: Locator;
    readonly giveAccessToAnotherLa: Locator;
    readonly selectLocalAuthority: Locator;
    readonly localAuthorityToTransfer: Locator;
    readonly removeAccess: Locator;
    readonly transferToAnotherLa: Locator;
    readonly fullName: Locator;
    readonly email: Locator;
    readonly courtTransfer: Locator;
    private localAuthorityAction: Locator;

    constructor(page: Page) {
        super(page);
        this.manageLaTransferToCourts = page.getByRole('heading', { name: 'Manage LAs / Transfer to court', exact: true });
        this.localAuthorityAction = page.getByRole('group', { name: 'What do you want to do?' });
        this.caseType = page.getByLabel('Case type');
        this.transferAnotherCourt = page.getByLabel('Transfer to another Court');
        this.selectNewCourt = page.getByLabel('Select new court');
        this.giveAccessToAnotherLa = page.getByRole('radio', { name: 'Give case access to another' });
        this.selectLocalAuthority = page.getByLabel('Select local authority');
        this.localAuthorityToTransfer = page.getByLabel('Select local authority to');
        this.removeAccess = page.getByRole('radio', { name: 'Remove case access from local' });
        this.transferToAnotherLa = page.getByLabel('Transfer the case to another');
        this.fullName = page.getByRole('textbox', { name: 'Full name' });
        this.email = page.getByLabel('Email', { exact: true });
        this.courtTransfer = page.getByRole('group', { name: 'Is the case transferring to a different court' });
    }

    public async selectLAAction(action:string){
  await this.localAuthorityAction.getByText(action).click();
    }
    // public async toTransferAnotherCourt() {
    //     await expect(this.manageLaTransferToCourts).toBeVisible();
    //     await this.transferAnotherCourt.click();
    //
    //
    //     await this.clickContinue();
    //     await this.checkYourAnsAndSubmit();
    // }
    public async selectCourt(courtName:string) {
        await this.selectNewCourt.selectOption(courtName);
    }
    public async updateCourtAccess() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.giveAccessToAnotherLa.click();
        await this.selectLocalAuthority.selectOption('London Borough Hillingdon');
    }
    public async updateRemoveAccess() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.removeAccess.click();
    }
    public async updateTranferToLa() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.transferToAnotherLa.click();

    }
    public async selectNewLocalAuthority(laName:string){
        await this.localAuthorityToTransfer.selectOption(laName);
    }
    public async enterLAContactDetails(){
         await this.fullName.fill('Sam Hill');
         await this.email.fill('sam@hillingdon.gov.uk');
    }
    public async enterNewCourtDetails(courtName:string){
        await this.courtTransfer.getByLabel('Yes').check();
        await this.selectNewCourt.selectOption(courtName);
    }
}
