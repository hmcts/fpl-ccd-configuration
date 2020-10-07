const {I} = inject();

module.exports = {

  fields: {
    hearingOptions: {
      addNewHearing: '#manageHearings_useExistingHearing-NEW_HEARING',
      editDraftHearing: '#manageHearings_useExistingHearing-EDIT_DRAFT',
      c2: '#manageDocument_type-C2',
    },
  },

  async selectAddNewHearing() {
    I.click(this.fields.hearingOptions.furtherEvidence);
  },
};
