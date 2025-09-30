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
import { ManageDocuments } from "../pages/manage-documents";
import { CaseFileView } from "../pages/case-file-view";
import { AddApplicationDocuments } from "../pages/add-application-documents";
import { ManageHearings } from "../pages/manage-hearings";
import { GatekeepingListing } from "../pages/gatekeeping-listing";
import { CaseDetails } from "../pages/case-details";
import { AdditionalApplications } from "../pages/additional-applications";
import { ApproveOrders } from "../pages/approve-orders";
import { Placement } from "../pages/placement";
import { ApplicantDetails } from "../pages/applicant-details";
import { RespondentDetails } from "../pages/respondent-details";
import { LegalCounsel } from "../pages/legal-counsel";
import { ChildDetails } from "../pages/child-details";
import { WelshLangRequirements } from "../pages/welsh-lang-requirements";
import { OtherProceedings } from "../pages/other-proceedings";
import { C1WithSupplement } from "../pages/c1-with-supplement";
import { InternationalElement } from "../pages/international-element";
import { CaseLink } from "../pages/link-cases"
import { CourtServices } from "../pages/court-services";
import { AddAndRemoveAdminCaseFlag } from "../pages/add-and-remove-admin-case-flag";
import { SubmitCase } from "../pages/submit-case";
import { Organisation } from "../pages/manage-organisation";
import { ShareCase } from "../pages/share-case";
import { OtherPeopleInCase } from "../pages/other-people-in-the-case";
import { ReturnApplication } from "../pages/return-application";
import { Orders } from "../pages/orders";
import { CaseProgressionReport } from "../pages/case-progression-report";
import { LogExpertReport } from "../pages/log-expert-report";
import { ChangeCaseName } from "../pages/change-case-name";
import { ManageLaTransferToCourts } from "../pages/manage-la-transfer-to-courts";
import { ManageRepresentatives } from "../pages/manage-representatives";
import {QueryManagement} from "../pages/query-management";
import {ManageTTL} from "../pages/manage-t-t-l";
import { OthersToBeGivenNotice } from "../pages/others-to-be-given-notice";
import { ChangeOtherToRespondent } from "../pages/change-other-to-respondent";
import {ManageOrdersChildrenDetails} from "../pages/manage-orders/manage-orders-children-details";
import {ManageOrdersHearingDetails} from "../pages/manage-orders/manage-orders-hearing-details";
import {ManageOrdersManageOrdersOperations} from "../pages/manage-orders/manage-orders-manage-orders-operations";
import {ManageOrdersOrderDetails} from "../pages/manage-orders/manage-orders-order-details";
import {ManageOrdersIssuingDetails} from "../pages/manage-orders/manage-orders-issuing-details";
import {ManageOrdersReview} from "../pages/manage-orders/manage-orders-review";
import {Submit} from "../pages/manage-orders/submit";
import {ManageOrdersOrderSelection} from "../pages/manage-orders/manage-orders-order-selection";
import {Applications} from "../pages/applications/upload-additional/applications";
import {ApplicationFee} from "../pages/applications/upload-additional/application-fee";
import {SuppliedDocuments} from "../pages/applications/upload-additional/supplied-documents";


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
  allocationProposal: AllocationProposal;
  addApplicationDocuments: AddApplicationDocuments;
  manageHearings: ManageHearings;
  gateKeepingListing: GatekeepingListing;
  caseDetails: CaseDetails;
  additionalApplications: AdditionalApplications;
  approveOrders: ApproveOrders;
  placement: Placement;
  applicantDetails: ApplicantDetails;
  childDetails: ChildDetails;
  respondentDetails: RespondentDetails;
  legalCounsel: LegalCounsel;
  welshLangRequirements: WelshLangRequirements;
  otherProceedings: OtherProceedings;
  submitCase: SubmitCase;
  internationalElement: InternationalElement;
  caseLink : CaseLink ;
  courtServices: CourtServices;
  addAdminCaseFlag: AddAndRemoveAdminCaseFlag;
  c1WithSupplement: C1WithSupplement;
  organisation: Organisation;
  shareCase: ShareCase;
  otherPeopleInCase: OtherPeopleInCase;
  returnApplication: ReturnApplication;
  logExpertReport: LogExpertReport;
  changeCaseName: ChangeCaseName;
  caseProgressionReport: CaseProgressionReport;
  orders: Orders;
  manageLaTransferToCourts: ManageLaTransferToCourts
  manageRepresentatives: ManageRepresentatives;
  queryManagement: QueryManagement;
  manageTTL: ManageTTL;
  othersToBeGivenNotice: OthersToBeGivenNotice;
  changeOtherToRespondent: ChangeOtherToRespondent;


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

  approveOrders: async ({ page }, use) => {
    await use(new ApproveOrders(page));
  },

  placement: async ({ page }, use) => {
    await use(new Placement(page));
  },

  respondentDetails: async ({ page }, use) => {
    await use(new RespondentDetails(page));
  },

  applicantDetails: async ({ page }, use) => {
    await use(new ApplicantDetails(page));
  },

  childDetails: async ({ page }, use) => {
    await use(new ChildDetails(page));
  },

  legalCounsel: async ({ page }, use) => {
    await use(new LegalCounsel(page));
  },

  welshLangRequirements: async ({ page }, use) => {
    await use(new WelshLangRequirements(page));
  },

  otherProceedings: async ({ page }, use) => {
    await use(new OtherProceedings(page));
  },

  internationalElement: async ({ page }, use) => {
    await use(new InternationalElement(page));
  },
    caseLink: async ({ page }, use) => {
        await use(new CaseLink(page));
    },
  courtServices: async ({ page }, use) => {
    await use(new CourtServices(page));
  },

  addAdminCaseFlag: async ({ page }, use) => {
    await use(new AddAndRemoveAdminCaseFlag(page));
  },

  c1WithSupplement: async ({ page }, use) => {
    await use(new C1WithSupplement(page));
  },

  otherPeopleInCase: async ({ page }, use) => {
    await use(new OtherPeopleInCase(page));
  },

  returnApplication: async ({ page }, use) => {
    await use(new ReturnApplication(page));
  },

  submitCase: async ({ page }, use) => {
    await use(new SubmitCase(page));
  },

  organisation: async ({ page }, use) => {
    await use(new Organisation(page));
  },

  shareCase: async ({ page }, use) => {
    await use(new ShareCase(page));
  },

  orders: async ({ page }, use) => {
    await use(new Orders(page));
  },

  logExpertReport: async ({ page }, use) => {
    await use(new LogExpertReport(page));
  },

  changeCaseName: async ({ page }, use) => {
    await use(new ChangeCaseName(page));
  },

  caseProgressionReport: async ({ page }, use) => {
    await use(new CaseProgressionReport(page));
  },

  manageLaTransferToCourts: async ({ page }, use) => {
    await use(new ManageLaTransferToCourts(page));
},


othersToBeGivenNotice: async ({ page }, use) => {
  await use(new OthersToBeGivenNotice(page));
},

  manageRepresentatives: async ({ page }, use) => {
    await use(new ManageRepresentatives(page));

  },

    queryManagement: async ({ page }, use) => {
        await use(new QueryManagement(page));

  },

    manageTTL: async ({ page }, use) => {
        await use(new ManageTTL(page));
    },

  changeOtherToRespondent: async ({ page }, use) => {
    await use(new ChangeOtherToRespondent(page));
  },

    manageOrdersChildrenDetails: async ({ page }, use) => {
      await use(new ManageOrdersChildrenDetails(page));
    },

    manageOrderHearingDetails: async ({ page }, use) => {
      await use(new ManageOrdersHearingDetails(page));
    },

    manageOrdersManageOrdersOperations: async ({ page }, use) => {
      await use(new ManageOrdersManageOrdersOperations(page));
    },

    manageOrdersOrderDetails: async ({ page }, use) => {
      await use(new ManageOrdersOrderDetails(page));
    },

    manageOrdersIssuingDetails: async ({ page }, use) => {
      await use(new ManageOrdersIssuingDetails(page));
    },

    manageOrdersOrderSelection: async ({ page }, use) => {
      await use(new ManageOrdersOrderSelection(page));
    },

    manageOrdersReview: async ({ page }, use) => {
      await use(new ManageOrdersReview(page));
    },

    uploadAdditionalApplications: async ({ page }, use) => {
      await use(new Applications(page));
    },

    uploadAdditionalApplicationsApplicationFee: async ({ page }, use) => {
      await use(new ApplicationFee(page));
    },

    uploadAdditionalApplicationsSuppliedDocuments: async ({ page }, use) => {
      await use(new SuppliedDocuments(page));
    },

    submit: async ({ page }, use) => {
      await use(new Submit(page));
    }
});
