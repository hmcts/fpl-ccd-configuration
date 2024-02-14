export function caseNumberHyphenated(caseNumber: string): string {
  return caseNumber.replace(/(\d{4})(\d{4})(\d{4})(\d{4})/, '$1-$2-$3-$4');
}
