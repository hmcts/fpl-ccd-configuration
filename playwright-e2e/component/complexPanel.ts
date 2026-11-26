import type { Locator } from "@playwright/test";

export interface ComplexTable {
    [key: string]: string | ComplexTable[]; // each field is a leaf string or nested list of complex blocks
}

export class ComplexPanel {
    constructor(private panel: Locator) {}

    async extractComplexTableData(): Promise<ComplexTable> {
        const result: ComplexTable = {};

        const rows = this.panel.locator(
            'table.complex-panel-table > tbody > tr.complex-panel-simple-field:not([hidden])'
        );
        const count = await rows.count();

        for (let i = 0; i < count; i++) {
            const row = rows.nth(i);
            const key = (await row.locator('th').first().innerText()).trim();
            const td = row.locator('td').first();

            // nested complex blocks in this value cell
            const nestedPanels = td.locator(':scope .complex-panel');
            const nestedCount = await nestedPanels.count();

            if (nestedCount > 0) {
                const nested: ComplexTable[] = [];
                for (let j = 0; j < nestedCount; j++) {
                    nested.push(await new ComplexPanel(nestedPanels.nth(j)).extractComplexTableData());
                }
                result[key] = nested;
            } else {
                result[key] = (await td.innerText()).replace(/\s+/g, ' ').trim();
            }
        }

        return result;
    }
}
