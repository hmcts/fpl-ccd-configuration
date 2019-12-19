const { I } = inject();

module.exports = {
  fields: {
    orderAction: {
      requestChange: '#orderAction_changeRequestedByJudge',
    },
    nextHearingDateList: '#nextHearingDateList',
  },

  staticFields: {
    statusRadioGroup: {
      groupName: '#orderAction_type',
      options: {
        sendToAllParties: 'Yes, send this to all parties',
        judgeRequestedChanges: 'No, the local authority needs to make changes',
        selfReview: 'No, I need to make changes',
      },
    },
    nextHearingRadioGroup: {
      groupName: '#orderAction_nextHearingType',
      options: {
        furtherCaseManagementHearing: 'Further case management hearing',
        issueResolutionHearing: 'Issues resolution hearing',
        finalHearing: 'Final hearing',
      },
    },
    placeholderLabels: {
      heading: '#actionCMOPlaceholderHeading',
      hintText: '#actionCMOPlaceholderHint'
    }
  },

  labels: {
    files: {
      draftCaseManagementOrder: 'draft-case_management_order.pdf',
      sealedCaseManagementOrder: 'case_management_order.pdf',
    },
  },

  async enterRequestedChange(reason) {
    await I.fillField(this.fields.orderAction.requestChange, reason);
  },

  markToBeSentToAllParties() {
    within(this.staticFields.statusRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.statusRadioGroup.options.sendToAllParties));
    });
  },

  markToBeSentToLocalAuthority() {
    within(this.staticFields.statusRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.statusRadioGroup.options.judgeRequestedChanges));
    });
  },

  markToBeReviewedBySelf() {
    within(this.staticFields.statusRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.statusRadioGroup.options.selfReview));
    });
  },

  markNextHearingToBeCaseManagement() {
    within(this.staticFields.nextHearingRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.nextHearingRadioGroup.options.furtherCaseManagementHearing));
    });
  },

  markNextHearingToBeIssueResolution() {
    within(this.staticFields.nextHearingRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.nextHearingRadioGroup.options.issueResolutionHearing));
    });
  },

  markNextHearingToBeFinalHearing() {
    within(this.staticFields.nextHearingRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.nextHearingRadioGroup.options.finalHearing));
    });
  },

  selectNextHearingDate(date) {
    I.waitForElement(this.fields.nextHearingDateList);
    I.selectOption(this.fields.nextHearingDateList, date);
  },
};
