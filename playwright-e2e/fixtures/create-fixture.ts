import { test as base } from "@playwright/test";
import { SignInPage } from "../pages/sign-in";
import { CreateCase } from "../pages/create-case";
import { StartApplication } from "../pages/start-application";
import { OrdersAndDirectionSought } from "../pages/orders-and-directions";
import { HearingUrgency } from "../pages/hearing-urgency";
import { GroundsForTheApplication } from "../pages/grounds-for-the-application";
import { JudicialMessage } from "../pages/judicial-message";
import { RiskAndHarmToChildren } from "../pages/risk-and-harm-to-children";
import { FactorsAffectingParenting } from "../pages/factors-affecting-parenting";
import { UploadDraftOrders } from "../pages/upload-draft-orders";
import { AllocationProposal } from "../pages/allocation-proposal";
import { AdditionalApplications } from "../pages/additional-applications";
import { ManageDocuments } from "../pages/manage-documents";
import { CaseFileView } from "../pages/case-file-view";
import { AddApplicationDocuments } from "../pages/add-application-documents";
import { ManageHearings } from "../pages/manage-hearings";
import { GatekeepingListing } from "../pages/gatekeeping-listing";
import { CaseDetails } from "../pages/case-details";
import { ApplicantDetails } from "../pages/applicant-details";
import { RespondentDetails } from "../pages/respondent-details";
import { LegalCounsel } from "../pages/legal-counsel";
import { ChildDetails } from "../pages/child-details";
import { WelshLangRequirements } from "../pages/welsh-lang-requirements";
import { C1WithSupplement } from "../pages/c1-with-supplement";
import { InternationalElement } from "../pages/international-element";
import { CourtServicesNeeded } from "../pages/court-services-needed";
import { AddAndRemoveAdminCaseFlag } from "../pages/add-and-remove-admin-case-flag";
import { SubmitCase } from "../pages/submit-case";

type CreateFixtures = {
  signInPage: SignInPage;
  createCase: CreateCase;
  startApplication: StartApplication;
  ordersAndDirectionSought: OrdersAndDirectionSought;
  hearingUrgency: HearingUrgency;
  groundsForTheApplication: GroundsForTheApplication;
  judicialMessages: JudicialMessage;
  riskAndHarmToChildren: RiskAndHarmToChildren;
  factorsAffectingParenting: FactorsAffectingParenting;
  uploadDraftOrders: UploadDraftOrders;
  manageDocuments: ManageDocuments;
  caseFileView: CaseFileView;
  allocationProposal : AllocationProposal;
  additionalApplications: AdditionalApplications;
  addApplicationDocuments : AddApplicationDocuments;
  manageHearings: ManageHearings;
  gateKeepingListing: GatekeepingListing;
  caseDetails: CaseDetails;
  applicantDetails: ApplicantDetails;
  childDetails: ChildDetails;
  respondentDetails: RespondentDetails;
  legalCounsel: LegalCounsel;
  welshLangRequirements: WelshLangRequirements;
  submitCase: SubmitCase;
  internationalElement: InternationalElement;
  courtServicesNeeded: CourtServicesNeeded;
  addAdminCaseFlag: AddAndRemoveAdminCaseFlag;
  c1WithSupplement: C1WithSupplement;
};

export const test = base.extend<CreateFixtures>({
  signInPage: async ({ page }, use) => {
    await use(new SignInPage(page));
  },

  createCase: async ({ page }, use) => {
    await use(new CreateCase(page));
  },

  startApplication: async ({ page }, use) => {
    await use(new StartApplication(page));
  },

  ordersAndDirectionSought: async ({ page }, use) => {
    await use(new OrdersAndDirectionSought(page));
  },

  hearingUrgency: async ({ page }, use) => {
    await use(new HearingUrgency(page));
  },

  groundsForTheApplication: async ({ page }, use) => {
    await use(new GroundsForTheApplication(page));
  },

  judicialMessages: async ({ page }, use) => {
    await use(new JudicialMessage(page));
  },

  riskAndHarmToChildren: async ({ page }, use) => {
    await use(new RiskAndHarmToChildren(page));
  },

  factorsAffectingParenting: async ({ page }, use) => {
    await use(new FactorsAffectingParenting(page));
  },

  allocationProposal: async ({ page }, use) => {
    await use(new AllocationProposal(page));
  },

  additionalApplications: async ({ page }, use) => {
    await use(new AdditionalApplications(page));
  },

  uploadDraftOrders: async ({ page }, use) => {
    await use(new UploadDraftOrders(page));
   },

  manageDocuments: async ({ page }, use) => {
    await use(new ManageDocuments(page));
  },

  caseFileView: async ({ page }, use) => {
    await use(new CaseFileView(page));
  },

  addApplicationDocuments: async ({ page }, use) => {
    await use(new AddApplicationDocuments(page));
  },

  manageHearings: async ({ page }, use) => {
    await use(new ManageHearings(page));
  },

  gateKeepingListing: async ({ page }, use) => {
    await use(new GatekeepingListing(page));
  },

  caseDetails: async ({ page }, use) => {
    await use(new CaseDetails(page));
  },

  applicantDetails: async ({ page }, use) => {
    await use(new ApplicantDetails(page));
  },

  childDetails: async ({ page }, use) => {
    await use(new ChildDetails(page));
  },

  respondentDetails: async ({ page }, use) => {
    await use(new RespondentDetails(page));
  },

  legalCounsel: async ({ page }, use) => {
    await use(new LegalCounsel(page));
  },

  welshLangRequirements: async ({ page }, use) => {
    await use(new WelshLangRequirements(page));
  },

  internationalElement: async ({ page }, use) => {
    await use(new InternationalElement(page));
  },
  courtServicesNeeded: async ({ page }, use) => {
    await use(new CourtServicesNeeded(page));
  },
  
    addAdminCaseFlag: async ({ page }, use) => {
        await use(new AddAndRemoveAdminCaseFlag(page));
  },
  
  c1WithSupplement: async ({ page }, use) => {
     await use(new C1WithSupplement(page));
  },
  
  submitCase: async ({ page }, use) => {
    await use(new SubmitCase(page));
  },
});
