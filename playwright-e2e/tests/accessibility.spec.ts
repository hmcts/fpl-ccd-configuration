import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright'; // 1

test.describe('homepage', () => { // 2

  
    test('example with attachment', async ({ page }, testInfo) => {
        test.info().annotations.push(({
            type: 'Accessibility test 1 ',
            description: 'This test is an accessibility tes 1'
        }))
    await page.goto('https://aeolian-royal-chip.glitch.me/'); // 3

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze(); // 4

    expect(accessibilityScanResults.violations).toEqual([]); // 5
  });
});

test('example with attachment', async ({ page }, testInfo) => {
        test.info().annotations.push(({
        type: 'Accessibility test 2',
        description: 'This test is an accessibility test 2'
    }))
    await page.goto('https://aeolian-royal-chip.glitch.me/');
  
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze();
  
    await testInfo.attach('accessibility-scan-results', {
      body: JSON.stringify(accessibilityScanResults, null, 2),
      contentType: 'application/json'
    });
  
    expect(accessibilityScanResults.violations).toEqual([]);
  });