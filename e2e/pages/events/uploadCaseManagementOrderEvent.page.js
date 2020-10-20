const { I } = inject();

module.exports = {
  fields: {
    pastHearingDropdown: '#pastHearingsForCMO',
    uploadCmo: '#uploadedCaseManagementOrder',
  },

  associateHearing(date) {
    I.waitForElement(this.fields.pastHearingDropdown);
    I.selectOption(this.fields.pastHearingDropdown, `Case management hearing, ${date}`);
  },

  uploadCaseManagementOrder(file) {
    I.attachFile(this.fields.uploadCmo, file);
  },
};
