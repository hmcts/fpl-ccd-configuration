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
import { AllocationProposal } from "../pages/allocation-proposal";
import { AddApplicationDocuments } from "../pages/add-application-documents";
import { ManageHearings } from "../pages/manage-hearings";
import { GatekeepingListing } from "../pages/gatekeeping-listing";
import {CaseDetails} from "../pages/case-details";

type CreateFixtures = {
  signInPage: SignInPage;
  createCase: CreateCase;
  startApplication: StartApplication;
  ordersAndDirectionSought: OrdersAndDirectionSought;
  hearingUrgency : HearingUrgency;
  groundsForTheApplication : GroundsForTheApplication;
  judicialMessages : JudicialMessage;
  riskAndHarmToChildren : RiskAndHarmToChildren;
  factorsAffectingParenting: FactorsAffectingParenting;
  allocationProposal : AllocationProposal;
  addApplicationDocuments : AddApplicationDocuments;
  manageHearings: ManageHearings;
  gateKeepingListing: GatekeepingListing;
  caseDetails: CaseDetails;
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
});
