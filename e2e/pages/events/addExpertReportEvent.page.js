const {I} = inject();

module.exports = {
  fields: {
    reportList: '#expertReport_0_expertReportList',
    reportRequestDate: {
      day: '#expertReport_0_expertReportDateRequested-day',
      month: '#expertReport_0_expertReportDateRequested-month',
      year: '#expertReport_0_expertReportDateRequested-year',
    },
    reportApprovalDate: {
      day: '#expertReport_0_reportApprovalDate-day',
      month: '#expertReport_0_reportApprovalDate-month',
      year: '#expertReport_0_reportApprovalDate-year',
    },
    reportApproval: '#expertReport_0_reportApproval_Yes',
  },

  async addExpertReportLog(expertReportLog) {
    I.click('Add new');
    await I.runAccessibilityTest();
    I.selectOption(this.fields.reportList, expertReportLog.reportList.value);

    I.wait(0.5);
    I.fillDate(expertReportLog.reportRequestDate, '#expertReportDateRequested');
    I.click(this.fields.reportApproval);
    I.fillDate(expertReportLog.reportApprovalDate, '#reportApprovalDate');
  },
};
