const I = actor();

module.exports = {
  openExistingCase(caseId) {
    let href = `/case/PUBLICLAW/Shared_Storage_DRAFTType/${caseId.replace(/\D/g, '')}`;
    let caseLink = this.locate('a').withAttr({href: href});
    I.click(caseLink);
  },
};
