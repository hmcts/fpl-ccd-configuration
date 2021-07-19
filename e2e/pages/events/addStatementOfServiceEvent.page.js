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
        timeSent: `#statementOfService_${index}_timeSent`,
        date: `(//*[contains(@class, "collection-title")])[${ index + 1 }]/parent::div//*[contains(@class,"form-date")]`,
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
    await this.enterRecipientsAddress(elementIndex, recipients);
    I.fillField(this.fields(elementIndex).recipients.documents, recipients.documents);
    I.fillDate(recipients.date, this.fields(elementIndex).recipients.date);
    I.fillField(this.fields(elementIndex).recipients.timeSent, recipients.timeSent);
    I.click(this.fields(elementIndex).recipients.sentBy.email);
    I.waitForText('Recipient\'s email address');
    await within(this.fields(elementIndex).recipients.sentBy.email, () => {
      I.fillField(this.fields(elementIndex).recipients.emailAddress, 'email@email.com');
    },);
  },

  giveDeclaration() {
    I.checkOption('I agree with this statement');
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },

  enterRecipientsAddress: async function (elementIndex, recipients) {
    await within(this.fields(elementIndex).recipients.address, () => {
      //XXX removed postcode lookup due to instability
      postcodeLookup.enterAddressManually(recipients.address);
    });
  },
};
