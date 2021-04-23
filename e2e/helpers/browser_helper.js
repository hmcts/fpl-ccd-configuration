const testConfig = require('../config.js');
const {runAccessibility} = require('./accessibility/runner');

module.exports = class BrowserHelpers extends Helper {

  getHelper() {
    return this.helpers['Puppeteer'] || this.helpers['WebDriver'];
  }

  clickBrowserBack() {
    const helper = this.getHelper();
    if (this.helpers['Puppeteer']) {
      return helper.page.goBack();
    } else {
      return helper.browser.back();
    }
  }

  reloadPage() {
    const helper = this.getHelper();
    return helper.refreshPage();
  }

  /**
   * Finds elements described by selector.
   * If element cannot be found an empty collection is returned.
   *
   * @param selector - element selector
   * @returns {Promise<Array>} - promise holding either collection of elements or empty collection if element is not found
   */
  async locateSelector(selector) {
    const helper = this.getHelper();
    return helper._locate(selector);
  }

  async hasSelector(selector) {
    return (await this.locateSelector(selector)).length;
  }

  /**
   * Finds element described by locator.
   * If element cannot be found immediately function waits specified amount of time or globally configured `waitForTimeout` period.
   * If element still cannot be found after the waiting time an undefined is returned.
   *
   * @param locator - element CSS locator
   * @param sec - optional time in seconds to wait
   * @returns {Promise<undefined|*>} - promise holding either an element or undefined if element is not found
   */
  async waitForSelector(locator, sec) {
    const helper = this.getHelper();
    const waitTimeout = sec ? sec * 1000 : helper.options.waitForTimeout;
    try {
      if (this.helpers['Puppeteer']) {
        const context = await helper._getContext();
        return await context.waitForSelector(locator, {timeout: waitTimeout});
      } else {
        return await helper.waitForElement(locator, waitTimeout);
      }
    } catch (error) {
      return undefined;
    }
  }

  async waitForAnySelector(selectors, maxWaitInSecond) {
    return this.waitForSelector([].concat(selectors).join(','), maxWaitInSecond);
  }

  async canSee(selector){
    const helper = this.getHelper();
    try {
      const numVisible = await helper.grabNumberOfVisibleElements(selector);
      return !!numVisible;
    } catch (err) {
      return false;
    }
  }

  /**
   * Grabs text from a element specified by locator.
   *
   * Note: When error is not found undefined is returned. That behaviour is bit different from a behaviour of
   * default `grabTextFrom` function which throws an error and fails test when element is not found.
   *
   * @param locator - element locator
   * @returns {Promise<string|undefined>}
   */
  async grabText(locator) {
    const elements = await this.locateSelector(locator);

    const texts = elements.map(async (element) => {
      return (await element.getProperty('innerText')).jsonValue();
    });

    if (texts.length > 1) {
      throw new Error(`More than one element found for locator ${locator}`);
    } else if (texts.length === 1) {
      return texts[0];
    } else {
      return undefined;
    }
  }

  async runAccessibilityTest() {
    const helper = this.getHelper();

    if (!testConfig.TestForAccessibility) {
      return;
    }
    const url = await helper.grabCurrentUrl();
    const {page} = await helper;

    runAccessibility(url, page);
  }
};
