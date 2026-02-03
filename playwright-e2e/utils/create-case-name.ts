export class CreateCaseName {
  static getFormattedDate() : string {
    const date = new Date();
    return date.toISOString();
  }
    static generateFileName(docType: string){
        let date = new Date().toLocaleString('en-gb',{timeZone:'Europe/London' });
        date= date.replaceAll('/','');
        date= date.replaceAll(',','');
        date= date.replaceAll(':','');
        date= date.substring(0,13);
        let fileName = docType + '-'+ date + '.pdf';
    return fileName
    }
}
