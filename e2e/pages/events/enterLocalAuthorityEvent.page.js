const {I} = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: {
    localAuthority: {
      name: '#localAuthority_name',
      email: '#localAuthority_email',
      legalTeamManager: '#localAuthority_legalTeamManager',
      pbaNumber: '#localAuthority_pbaNumber',
      customerReference: '#localAuthority_customerReference',
      clientCode: '#localAuthority_clientCode',
      address: '#localAuthority_address_address',
      phone: '#localAuthority_phone',
    },
    colleague: index => ({
      solicitorRole: `#localAuthorityColleagues_${index}_role-SOLICITOR`,
      socialWorkerRole: `#localAuthorityColleagues_${index}_role-SOCIAL_WORKER`,
      otherRole: `#localAuthorityColleagues_${index}_role-OTHER`,
      title: `#localAuthorityColleagues_${index}_title`,
      dx: `#localAuthorityColleagues_${index}_dx`,
      reference: `#localAuthorityColleagues_${index}_reference`,
      name: `#localAuthorityColleagues_${index}_fullName`,
      email: `#localAuthorityColleagues_${index}_email`,
      phone: `#localAuthorityColleagues_${index}_phone`,
      notificationRecipient: {
        yes: `#localAuthorityColleagues_${index}_notificationRecipient_Yes`,
        no: `#localAuthorityColleagues_${index}_notificationRecipient_No`,
      },
    }),
    mainContact: '#localAuthorityColleaguesList',
  },

  async enterDetails(localAuthority) {
    I.fillField(this.fields.localAuthority.name, localAuthority.name);
    I.fillField(this.fields.localAuthority.email, localAuthority.email);
    I.fillField(this.fields.localAuthority.legalTeamManager, localAuthority.legalTeamManager);
    I.fillField(this.fields.localAuthority.pbaNumber, localAuthority.pbaNumber);
    I.fillField(this.fields.localAuthority.customerReference, localAuthority.customerReference);
    I.fillField(this.fields.localAuthority.clientCode, localAuthority.clientCode);

    await within(this.fields.localAuthority.address, async () => {
      await postcodeLookup.enterAddressIfNotPresent(localAuthority.address);
    });

    I.fillField(this.fields.localAuthority.phone, localAuthority.phone);
  },

  async enterColleague(colleague, index = 0) {
    await I.addElementToCollection(index);

    if (colleague.role === 'Solicitor') {
      I.checkOption(this.fields.colleague(index).solicitorRole);
    }
    if (colleague.role === 'Social worker') {
      I.checkOption(this.fields.colleague(index).socialWorkerRole);
    }
    if (colleague.role === 'Other colleague') {
      I.checkOption(this.fields.colleague(index).otherRole);
    }
    if (colleague.title) {
      I.fillField(this.fields.colleague(index).title, colleague.title);
    }
    if (colleague.dx) {
      I.fillField(this.fields.colleague(index).dx, colleague.dx);
    }
    if (colleague.reference) {
      I.fillField(this.fields.colleague(index).reference, colleague.reference);
    }
    if (colleague.fullName) {
      I.fillField(this.fields.colleague(index).name, colleague.fullName);
    }
    if (colleague.email) {
      I.fillField(this.fields.colleague(index).email, colleague.email);
    }
    if (colleague.phone) {
      I.fillField(this.fields.colleague(index).phone, colleague.phone);
    }
    if (colleague.notificationRecipient === 'Yes') {
      I.checkOption(this.fields.colleague(index).notificationRecipient.yes);
    }
    if (colleague.notificationRecipient === 'No') {
      I.checkOption(this.fields.colleague(index).notificationRecipient.no);
    }
  },

  selectMainContact(colleague) {
    I.selectOption(this.fields.mainContact, colleague.fullName);
  },

};
