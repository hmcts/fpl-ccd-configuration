export class CreateCaseName {
  static getFormattedDate() : string {
    const date = new Date();
    return date.toISOString();
  }
    static generateFileName(docType: string){
        let date = new Date();
        let year  = date.getFullYear();
        let month = (date.getMonth() + 1).toString().padStart(2, "0");
        let  day   = date.getDate().toString().padStart(2, "0");
        let hour = date.getHours().toString().padStart(2, "0");
        let min = date.getMinutes().toString().padStart(2, "0");
        let fileName = docType + '-'+ day+month+year+ ' ' + hour + min + '.pdf';

return fileName
    }
}
