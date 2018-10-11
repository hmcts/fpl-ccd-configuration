module.exports = class MyHelpers extends Helper {

	async clickBrowserBack() {
		const page = this.helpers['Puppeteer'].page;
		return page.goBack();
	}
};
