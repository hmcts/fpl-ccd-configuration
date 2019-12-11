const {I} = inject();
let assignee;

module.exports = {
  fields: function (collection, directionIndex, responseIndex, assignee = '') {
    return {
      direction: {
        addNewButton: {css: `#${collection}_${directionIndex}_responses > div:nth-child(1) > button:nth-child(2)`},
        onBehalfOf: `#${collection}_${directionIndex}_responses_${responseIndex}_respondingOnBehalfOf${assignee}`,
        complied: {
          yes: `#${collection}_${directionIndex}_responses_${responseIndex}_complied-Yes`,
          no: `#${collection}_${directionIndex}_responses_${responseIndex}_complied-No`,
        },
        file: `#${collection}_${directionIndex}_responses_${responseIndex}_file`,
        documentDetails: `#${collection}_${directionIndex}_responses_${responseIndex}_documentDetails`,
        cannotComplyReason: `#${collection}_${directionIndex}_responses_${responseIndex}_cannotComplyReason`,
        c2Upload: `#${collection}_${directionIndex}_responses_${responseIndex}_c2Uploaded-UPLOADED`,
        supportingFile: `#${collection}_${directionIndex}_responses_${responseIndex}_cannotComplyFile`,
        supportingDocumentDetails: `#${collection}_${directionIndex}_responses_${responseIndex}_cannotComplyDocumentDetails`,
      },
    };
  },

  async addNewResponseOnBehalfOf(party, onBehalfOf, comply, index = 0, responsesNo = 0) {
    I.click(this.fields(party, index, responsesNo, assignee).direction.addNewButton);

    if (onBehalfOf === 'Respondent 1') {
      assignee = 'Respondent';
    }

    if (onBehalfOf === 'Person 1') {
      assignee = 'Other';
    }

    I.selectOption(this.fields(party, index, responsesNo, assignee).direction.onBehalfOf, onBehalfOf);

    if (comply === 'Yes') {
      I.click(this.fields(party, index, responsesNo).direction.complied.yes);
    } else {
      I.click(this.fields(party, index, responsesNo).direction.complied.no);
      I.fillField(this.fields(party, index, responsesNo).direction.cannotComplyReason, 'reason');
    }
  },
};
