
const { I } = inject();

module.exports = {
  fields: function(index) {
    return {
      email: `#gatekeeperEmails_${index}_email`,
    };
  },

  async enterEmail(email = 'familypubliclaw+gatekeeper@gmail.com') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).email, email);
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
