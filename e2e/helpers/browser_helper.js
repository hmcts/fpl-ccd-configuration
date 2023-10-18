const testConfig = require('../config.js');
const { runAccessibility } = require('./accessibility/runner');
const { Helper } = require('codeceptjs');


module.exports = class BrowserHelpers extends Helper {
  getHelper() {
    return this.helpers['Playwright']; // Use Playwright instead of Puppeteer or WebDriver
  }

  async getBrowser() {
    const helper = this.getHelper();
    return helper.page; // Use helper.page for Playwright
  }

  async clickBrowserBack() {
    const helper = this.getHelper();
    await helper.page.goBack(); // Use page.goBack() for Playwright
  }

  async reloadPage() {
    const helper = this.getHelper();
    await helper.page.reload(); // Use page.reload() for Playwright
  }

  async locateSelector(selector) {
    const helper = this.getHelper();
    return helper.page.locator(selector).elements(); // Use page.locator for Playwright
  }

  async hasSelector(selector) {
    const elements = await this.locateSelector(selector);
    if (elements !== undefined) {
      return elements.length > 0;
    } else {
      return false;
    }
  }

  async waitForSelector(locator, timeout = 30) {
    const helper = this.getHelper();
    const retryInterval = 5;
    const numberOfRetries = timeout < retryInterval ? 1 : Math.floor(timeout / retryInterval);

    for (let tryNumber = 0; tryNumber <= numberOfRetries; tryNumber++) {
      console.log('waitForSelector ' + locator + ' try number ' + (tryNumber + 1));
      try {
        await helper.page.waitForSelector(locator, { timeout: retryInterval * 1000 });
      } catch (e) {
        if (e.name !== 'TimeoutError') {
          throw e;
        }
      }
      console.log('found it!');
      return;
    }
    console.log('not found it after ' + (numberOfRetries + 1) + ' tries (' + timeout + 's)');
  }

  async waitForAnySelector(selectors, maxWaitInSecond) {
    return this.waitForSelector(selectors.join(','), maxWaitInSecond);
  }

  async canSee(selector) {
    const helper = this.getHelper();
    try {
      const numVisible = await helper.page.locator(selector).count();
      return numVisible > 0;
    } catch (err) {
      return false;
    }
  }

  async grabText(locator) {
    const helper = this.getHelper();
    const element = await helper.page.locator(locator).first();
    return element ? element.innerText() : undefined;
  }

  async grabAttribute(locator, attr) {
    const helper = this.getHelper();
    const element = await helper.page.locator(locator).first();
    return element ? element.getAttribute(attr) : undefined;
  }

  async runAccessibilityTest() {
    const helper = this.getHelper();

    if (!testConfig.TestForAccessibility) {
      return;
    }
    const url = await helper.page.url();
    const page = helper.page;

    await runAccessibility(url, page);
  }

  async canClick(selector) {
    const elements = await this.locateSelector(selector);
    return elements.length > 0 && (await elements[0].isIntersectingViewport());
  }

  async scrollToElement(selector) {
    const helper = this.getHelper();
    const element = await helper.page.locator(selector).first();
    if (element) {
      await element.scrollIntoViewIfNeeded();
    } else {
      throw new Error(`No element found for locator ${selector}`);
    }
  }
};
