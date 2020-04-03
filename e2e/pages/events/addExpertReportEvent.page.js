const {I} = inject();

module.exports = {
  fields: {
    expertReportList: '#expertReport_0_expertReportList',
    expertReportDate: {
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

  addExpertReportLog() {
    I.click('Add new');
    I.selectOption(this.fields.expertReportList, 'Peadiatric');
    I.fillField(this.fields.expertReportDate.day, '01');
    I.fillField(this.fields.expertReportDate.month, '03');
    I.fillField(this.fields.expertReportDate.year, '2003');
    I.click(this.fields.reportApproval);
    I.fillField(this.fields.reportApprovalDate.day, '1');
    I.fillField(this.fields.reportApprovalDate.month, '3');
    I.fillField(this.fields.reportApprovalDate.year, '2003');
  },
};
