const {I} = inject();
const config = require('../config');

const paymentsLocator = '//span[text()="Processed payments"]';

const buildPaymentLocator = amount => config.mockedPayment ? `${paymentsLocator}/../table//tr[.//td[text()="Success"]]` : `${paymentsLocator}/../table//tr[.//td[text()="${amount}"] and .//td[text()="Success"]]`;

const buildPbaLocator = pba => `//tr[.//td[text()="PBA number"] and .//td[text()="${pba}"]]`;

module.exports = {
  async checkPayment(amount, pba) {
    const paymentLocator = buildPaymentLocator(amount);
    const pbaLocator = buildPbaLocator(pba);

    I.seeElement(paymentsLocator);
    I.seeElement(paymentLocator);
    I.click(`${paymentLocator}/td/a`);
    I.seeElement(pbaLocator);
    // await I.runAccessibilityTest();
  },
};
