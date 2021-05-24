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
    reportApproval: '#expertReport_0_reportApproval-Yes',
  },

  async addExpertReportLog(expertReportLog) {
    I.click('Add new');
    await I.runAccessibilityTest();
    I.selectOption(this.fields.reportList, expertReportLog.reportList.value);

    I.wait(0.5);

    I.fillField(this.fields.reportRequestDate.day, expertReportLog.reportRequestDate.day);
    I.fillField(this.fields.reportRequestDate.month, expertReportLog.reportRequestDate.month);
    I.fillField(this.fields.reportRequestDate.year, expertReportLog.reportRequestDate.year);
    I.click(this.fields.reportApproval);
    I.fillField(this.fields.reportApprovalDate.day, expertReportLog.reportApprovalDate.day);
    I.fillField(this.fields.reportApprovalDate.month, expertReportLog.reportApprovalDate.month);
    I.fillField(this.fields.reportApprovalDate.year, expertReportLog.reportApprovalDate.year);
  },
};
