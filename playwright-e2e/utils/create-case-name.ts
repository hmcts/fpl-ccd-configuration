export class CreateCaseName {
  static async getFormattedDate() : Promise<any> {
    const date = new Date();
    return date.toISOString();
  }
}
