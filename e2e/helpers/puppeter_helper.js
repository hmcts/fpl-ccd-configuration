module.exports = class PuppeteerHelpers extends Helper {
  clickBrowserBack() {
    const page = this.helpers['Puppeteer'].page;
    return page.goBack();
  }

  reloadPage() {
    const page = this.helpers['Puppeteer'].page;
    return page.reload();
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
    const elements = await this.helpers['Puppeteer']._locate(locator);

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

  /**
   * Finds element described by locator.
   * If element cannot be found immediately function waits globally configured `waitForTimeout`.
   * If element still cannot be found after the waiting time an undefined is returned.
   *
   * @param locator - element CSS locator
   * @returns {Promise<undefined|*>} - promise holding either an element or undefined if element is not found
   */
  async waitForSelector(locator) {
    const waitTimeout = this.helpers['Puppeteer'].options.waitForTimeout;
    const context = await this.helpers['Puppeteer']._getContext();
    try {
      return await context.waitForSelector(locator, { timeout: waitTimeout });
    } catch (error) {
      return undefined;
    }
  }
};
