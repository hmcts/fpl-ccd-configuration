const { I } = inject();

module.exports = {
  fields: function(index) {
    return {
      email: `#gatekeeperEmails_${index}_email`,
    };
  },

  async enterEmail(email = 'familypubliclaw+gatekeeper@gmail.com') {
    const elementIndex = await I.getActiveElementIndex();
    I.fillField(this.fields(elementIndex).email, email);
    //await I.runAccessibilityTest();
  },
};
