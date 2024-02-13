export class CreateCaseName {
  static async getCaseName() : Promise<string> {

    const date = new Date();
    const formattedDate = date.toISOString();
    // Create the case name using a timestamp string
    return `Smoke Test ${formattedDate}`;
  }
}
