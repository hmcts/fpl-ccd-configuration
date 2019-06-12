const I = actor();
const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

module.exports = {
  changeStateFilter(desiredState) {
    I.selectOption('#wb-case-state', desiredState);
    I.click('Apply');
  },

  findCase(caseId) {
    return locate('.//tr').withChild(`.//td/a[text()='${caseId.slice(1)}']`);
  },

  seeSubmissionDate(row){
    let currentDate = new Date();
    I.seeElement(locate(row.withChild('.//td[4]').withText(currentDate.getDate() + ' ' + monthNames[currentDate.getMonth()] + ' ' + currentDate.getFullYear())));
  },
};
