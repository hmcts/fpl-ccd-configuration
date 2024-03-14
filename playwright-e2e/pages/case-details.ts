import {BasePage} from "./base-page";
import {expect} from "@playwright/test";

export class CaseDetails extends BasePage
{
  async validateRolesAndAccessTab(expectedRows: string[][], cellLookup: string) {
    await this.waitForRoleAndAccessTab(`${cellLookup}`);
    const tableLookupCell = this.page.locator(`text="${cellLookup}"`);
    const rolesAndAccessTable = tableLookupCell.locator('xpath=ancestor::table/tbody');

    const trCount = await rolesAndAccessTable.locator('tr').count();

    for (let i = 0; i < trCount; i++) {
      const tableRows = rolesAndAccessTable.locator(`tr >> nth=${i}`);
      // Avoid additional empty content cell in table
      const tdCount = await tableRows.locator('td').count() - 1;

      for (let j = 0; j < tdCount; j++) {
        const rowCells = tableRows.locator(`td >> nth=${j}`);
        const cellText = await rowCells.textContent();
        expect(cellText.trim()).toBe(expectedRows[i][j]);
      }
    }
  }
}
