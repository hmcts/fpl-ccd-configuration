// https://manage-case.aat.platform.hmcts.net/cases/case-details/1738332965233095/trigger/manageOrders/manageOrdersOrderDetails
import { Locator, Page } from '@playwright/test';
import { BasePage } from '../base-page';

export class ManageOrdersOrderDetails extends BasePage {
    readonly ordersToIssueGroup: Locator;
    readonly orderToIssueChildArrangementOrdersCheckbox: Locator;
    readonly ordersToIssueSpecificIssueOrderCheckbox: Locator;
    readonly ordersToIssueProhibitedStepsOrderCheckbox: Locator;

    readonly orderForGroup: Locator;
    readonly orderForChildToLiveWithCheckbox: Locator;
    readonly orderForChildToHaveContactWithCheckbox: Locator;
    readonly detailsForChildToLiveWithOrderLabel: Locator;

    readonly orderByConsentGroup: Locator;
    readonly orderByConsentYesRadioButton: Locator;
    readonly orderByConsentNoRadioButton: Locator;

    readonly recitalsOrPreambleLabel: Locator;

    readonly finalOrderGroup: Locator;
    readonly finalOrderYesRadioButton: Locator;
    readonly finalOrderNoRadioButton: Locator;

    constructor(page: Page) {
        super(page);
        this.ordersToIssueGroup = page.getByRole('group', { name: 'Select orders to issue' });
        this.orderToIssueChildArrangementOrdersCheckbox = this.ordersToIssueGroup.getByRole('checkbox', { name: 'Child arrangements order' });
        this.ordersToIssueSpecificIssueOrderCheckbox = this.ordersToIssueGroup.getByRole('checkbox', { name: 'Specific Issue order' });
        this.ordersToIssueProhibitedStepsOrderCheckbox = this.ordersToIssueGroup.getByRole('checkbox', { name: 'Prohibited steps order' });

        this.orderForGroup = page.getByRole('group', { name: 'What is the order for?' });
        this.orderForChildToLiveWithCheckbox = this.orderForGroup.getByRole('checkbox', { name: 'Child to live with' });
        this.orderForChildToHaveContactWithCheckbox = this.orderForGroup.getByRole('checkbox', { name: 'Child to have contact with' });
        this.detailsForChildToLiveWithOrderLabel = page.getByLabel('Add details for child to live with order');

        this.orderByConsentGroup = page.getByRole('group', { name: 'Is order by consent?' });
        this.orderByConsentYesRadioButton = this.orderByConsentGroup.getByRole('radio', { name: 'Yes' });
        this.orderByConsentNoRadioButton = this.orderByConsentGroup.getByRole('radio', { name: 'No' });

        this.recitalsOrPreambleLabel = page.getByRole('textbox', { name: 'Add recitals or preamble' });

        this.finalOrderGroup = page.getByRole('group', { name: 'Is this a final order' });
        this.finalOrderYesRadioButton = this.finalOrderGroup.getByRole('radio', { name: 'Yes' });
        this.finalOrderNoRadioButton = this.finalOrderGroup.getByRole('radio', { name: 'No' });
    }

    async checkChildArrangementsOrder(): Promise<void> {
        await this.orderToIssueChildArrangementOrdersCheckbox.check();
    }

    async checkSpecificIssueOrder(): Promise<void> {
        await this.ordersToIssueSpecificIssueOrderCheckbox.check();
    }

    async checkProhibitedStepsOrder(): Promise<void> {
        await this.ordersToIssueProhibitedStepsOrderCheckbox.check();
    }

    async checkChildToLiveWith(): Promise<void> {
        await this.orderForChildToLiveWithCheckbox.check();
    }

    async checkChildToHaveContactWith(): Promise<void> {
        await this.orderForChildToHaveContactWithCheckbox.check();
    }

    async checkOrderByConsentYes(): Promise<void> {
        await this.orderByConsentYesRadioButton.check();
    }

    async checkOrderByConsentNo(): Promise<void> {
        await this.orderByConsentNoRadioButton.check();
    }

    async fillDetailsForChildToLiveWithOrder(text: string): Promise<void> {
        await this.detailsForChildToLiveWithOrderLabel.fill(text);
    }

    async fillRecitalsOrPreamble(text: string): Promise<void> {
        await this.recitalsOrPreambleLabel.fill(text);
    }

    async checkFinalOrderYes(): Promise<void> {
        await this.finalOrderYesRadioButton.check();
    }

    async checkFinalOrderNo(): Promise<void> {
        await this.finalOrderNoRadioButton.check();
    }
}
