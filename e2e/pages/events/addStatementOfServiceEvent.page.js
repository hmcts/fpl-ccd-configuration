const { I } = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: function (index) {
    return {
      recipients: {
        name: `#statementOfService_${index}_name`,
        addressCheck: `#statementOfService_${index}_addressCheck_Yes`,
        address: `#statementOfService_${index}_address_address`,
        documents: `#statementOfService_${index}_documents`,
        date: {
          day: `#statementOfService_${index}_date_day`,
          month: `#statementOfService_${index}_date_month`,
          year: `#statementOfService_${index}_date_year`,
        },
        timeSent: `#statementOfService_${index}_timeSent`,
        sentBy: {
          group: `#statementOfService_${index}_sentBy`,
          email: `#statementOfService_${index}_sentBy-EMAIL`,
          post: `#statementOfService_${index}_sentBy-POST`,
          givenInPerson: `#statementOfService_${index}_sentBy-GIVEN_IN_PERSON`,
        },
        emailAddress: `//*[@id="statementOfService_${index}_email"]`,
        post: `#statementOfService_${index}_postOfficeAddress`,
      },
    };
  },

  async enterRecipientDetails(recipients) {
    const elementIndex = await this.getActiveElementIndex();

    await I.runAccessibilityTest();
    I.fillField(this.fields(elementIndex).recipients.name, recipients.name);
    I.click(this.fields(elementIndex).recipients.addressCheck);
    this.enterRecipientsAddress(elementIndex, recipients);
    I.fillField(this.fields(elementIndex).recipients.documents, recipients.documents);
    const dateSelector = `(//*[contains(@class, "collection-title")])[${ elementIndex + 1 }]/parent::div//*[contains(@class,"form-date")]`;
    I.fillDate(recipients.date, dateSelector);
    I.fillField(this.fields(elementIndex).recipients.timeSent, recipients.timeSent);
    I.click(this.fields(elementIndex).recipients.sentBy.email);
    I.waitForText('Recipient\'s email address');
    within(this.fields(elementIndex).recipients.sentBy.email, () => {
      I.fillField(this.fields(elementIndex).recipients.emailAddress, 'email@email.com');
    },);
  },

  giveDeclaration() {
    I.checkOption('I agree with this statement');
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },

  enterRecipientsAddress: function (elementIndex, recipients) {
    within(this.fields(elementIndex).recipients.address, () => {
      //XXX removed postcode lookup due to instability
      postcodeLookup.enterAddressManually(recipients.address);
    });
  },
};
