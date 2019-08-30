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
};
