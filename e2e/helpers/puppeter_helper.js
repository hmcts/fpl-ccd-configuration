/* global Helper */

module.exports = class MyHelpers extends Helper {

  clickBrowserBack() {
    const page = this.helpers['Puppeteer'].page;
    return page.goBack();
  }

  reloadPage() {
    const page = this.helpers['Puppeteer'].page;
    return page.reload();
  }
};
