const {I} = inject();

module.exports = {
  fields: {
    reportList: '#expertReport_0_expertReportList',
    reportRequestDate: '#expertReportDateRequested',
    reportApproval: '#expertReport_0_reportApproval_Yes',
    reportApprovalDate: '#reportApprovalDate',
  },

  async addExpertReportLog(expertReportLog) {
    I.click('Add new');
    await I.runAccessibilityTest();
    I.selectOption(this.fields.reportList, expertReportLog.reportList.value);

    I.wait(0.5);
    I.fillDate(expertReportLog.reportRequestDate, this.fields.reportRequestDate);
    I.click(this.fields.reportApproval);
    I.fillDate(expertReportLog.reportApprovalDate, this.fields.reportApprovalDate);
  },
};
