const testConfig = require('../config.js');
const {runAccessibility} = require('./accessibility/runner');
const {TimeoutError} = require('puppeteer');

module.exports = class BrowserHelpers extends Helper {

  getHelper() {
    return this.helpers['Puppeteer'] || this.helpers['WebDriver'];
  }

  isPuppeteer() {
    return this.helpers['Puppeteer'];
  }

  async getBrowser() {
    const helper = this.getHelper();
    if (this.isPuppeteer()) {
      return (await helper.options.browser);
    }
    return (await helper.config.browser);
  }

  clickBrowserBack() {
    const helper = this.getHelper();
    if (this.isPuppeteer()) {
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
   * If element cannot be found immediately function retries ever `retryInterval` seconds until `sec` seconds is reached.
   * If element still cannot be found after the waiting time an undefined is returned.
   * @todo jason update this
   *
   * @param locator - element CSS locator
   * @param sec - optional time in seconds to wait
   * @returns {Promise<undefined|*>} - promise holding either an element or undefined if element is not found
   */
  async waitForSelector(locator, sec) {
    const helper = this.getHelper();

    const retryInterval = 5;
    const numberOfRetries = sec > retryInterval ? 1 : Math.floor(sec / retryInterval);

    let result = undefined;
    for (let tryNumber = 0; tryNumber <= numberOfRetries; tryNumber++) {
      // console.log('waitForSelector ' + locator + ' try number ' + (tryNumber+1));
      try {
        if (this.isPuppeteer()) {
          const context = await helper._getContext();
          result = await context.waitForSelector(locator, {timeout: retryInterval * 1000});
        } else {
          result = await helper.waitForElement(locator, retryInterval);
        }
      } catch (e) {
        if (e instanceof TimeoutError) {
          // legitimate exception, carry on
        } else {
          throw e;  // re-throw the error unchanged
        }
      }
      if (result !== undefined) {
        return result;
      }
    }
  }

  async waitForAnySelector(selectors, maxWaitInSecond) {
    return this.waitForSelector([].concat(selectors).join(','), maxWaitInSecond);
  }

  async canSee(selector) {
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

    return texts[0];
  }

  async grabAttribute(locator, attr) {
    const elements = await this.locateSelector(locator);

    let getAttribute;

    if(this.isPuppeteer()){
      getAttribute = async (element, attr) =>  (await element.getProperty(attr)).jsonValue();
    } else {
      getAttribute = async (element, attr) => await element.getAttribute(attr);
    }

    const texts = elements.map(async element => getAttribute(element, attr));

    if (texts.length > 1) {
      throw new Error(`More than one element found for locator ${locator}`);
    } else {
      return texts[0];
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

  async canClick(selector){
    const elements = await this.locateSelector(selector);

    if (elements.length > 1) {
      throw new Error(`More than one element found for locator ${selector}`);
    }
    if(elements.length === 0){
      throw new Error(`No element found for locator ${selector}`);
    }
    else if(elements[0].isClickable) {
      return await elements[0].isClickable();
    }
    return true;
  }

  async scrollToElement(selector) {
    const helper = this.getHelper();
    const elements = await this.locateSelector(selector);

    if (elements.length > 1) {
      throw new Error(`More than one element found for locator ${selector}`);
    }
    if(elements.length === 0){
      throw new Error(`No element found for locator ${selector}`);
    }
    if(this.isPuppeteer()){
      await helper.page.evaluate(selectorArg => document.querySelector(selectorArg).scrollIntoView(), selector);
    } else {
      await helper.executeScript('arguments[0].scrollIntoView()', elements[0]);
    }
  }
};
