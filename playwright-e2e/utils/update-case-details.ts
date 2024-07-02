export function setHighCourt(caseData: any) {
  let court = { 
    code: "100",
    name: "High Court Family Division",
    email: null,
    region: "London",
    epimmsId: "20262",
    regionId: "1",
    dateTransferred: "2024-03-12T15:04:39.111807767"
  };

  let caseManagementLocation = {
      region: "1",
      baseLocation: "20262"
  }

  caseData.caseData["court"] = court;
  caseData.caseData["caseManagementLocation"] = caseManagementLocation;
  caseData.caseData["caseSummaryHighCourtCase"] = "Yes";
  caseData.caseData["caseSummaryCourtName"] = 'High Court Family Division';
}
