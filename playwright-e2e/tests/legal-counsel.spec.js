"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
var create_fixture_1 = require("../fixtures/create-fixture");
var api_helper_1 = require("../utils/api-helper");
//import {urlConfig} from "../settings/urls";
var caseWithRespondentSolicitor_json_1 = require("../caseData/caseWithRespondentSolicitor.json");
var caseWithRespondentSolicitorAndCounsel_json_1 = require("../caseData/caseWithRespondentSolicitorAndCounsel.json");
var caseWithChildSolicitorAndCounsel_json_1 = require("../caseData/caseWithChildSolicitorAndCounsel.json");
var mandatorySubmissionFields_json_1 = require("../caseData/mandatorySubmissionFields.json");
var user_credentials_1 = require("../settings/user-credentials");
var test_1 = require("@playwright/test");
create_fixture_1.test.describe('Respondent solicitor counsel ', function () {
    var apiDataSetup = new api_helper_1.Apihelp();
    var dateTime = new Date().toISOString();
    var caseNumber;
    var casename;
    create_fixture_1.test.beforeEach(function () { return __awaiter(void 0, void 0, void 0, function () {
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, apiDataSetup.createCase('e2e case', user_credentials_1.newSwanseaLocalAuthorityUserOne)];
                case 1:
                    caseNumber = _a.sent();
                    return [2 /*return*/];
            }
        });
    }); });
    (0, create_fixture_1.test)('Respondent solicitor add counsel', function (_a) {
        var page = _a.page, signInPage = _a.signInPage, legalCounsel = _a.legalCounsel;
        return __awaiter(void 0, void 0, void 0, function () {
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0:
                        casename = 'Respondent Solicitor add Counsel ' + dateTime.slice(0, 10);
                        return [4 /*yield*/, apiDataSetup.updateCase(casename, caseNumber, caseWithRespondentSolicitor_json_1.default)];
                    case 1:
                        _b.sent();
                        return [4 /*yield*/, apiDataSetup.giveAccessToCase(caseNumber, user_credentials_1.privateSolicitorOrgUser, '[SOLICITORA]')];
                    case 2:
                        _b.sent();
                        return [4 /*yield*/, signInPage.visit()];
                    case 3:
                        _b.sent();
                        return [4 /*yield*/, signInPage.login(user_credentials_1.privateSolicitorOrgUser.email, user_credentials_1.privateSolicitorOrgUser.password)];
                    case 4:
                        _b.sent();
                        return [4 /*yield*/, signInPage.navigateTOCaseDetails(caseNumber)];
                    case 5:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.gotoNextStep('Add or remove counsel')];
                    case 6:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.clickContinue()];
                    case 7:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.toAddLegalCounsel()];
                    case 8:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.enterLegalCounselDetails()];
                    case 9:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.clickContinue()];
                    case 10:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.checkYourAnsAndSubmit()];
                    case 11:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.tabNavigation('People in the case')];
                    case 12:
                        _b.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.locator('#case-viewer-field-read--respondents1')).toContainText('Counsel 1')];
                    case 13:
                        _b.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.locator('#case-viewer-field-read--respondents1')).toContainText('FPLSolicitorOrg')];
                    case 14:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.clickSignOut()];
                    case 15:
                        _b.sent();
                        return [4 /*yield*/, signInPage.login(user_credentials_1.FPLSolicitorOrgUser.email, user_credentials_1.FPLSolicitorOrgUser.password)];
                    case 16:
                        _b.sent();
                        return [4 /*yield*/, signInPage.navigateTOCaseDetails(caseNumber)];
                    case 17:
                        _b.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByRole('heading', { name: casename })).toBeVisible()];
                    case 18:
                        _b.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.locator('h1')).toContainText(casename)];
                    case 19:
                        _b.sent();
                        return [2 /*return*/];
                }
            });
        });
    });
    (0, create_fixture_1.test)('Respondent solicitor remove counsel', function (_a) {
        var page = _a.page, signInPage = _a.signInPage, legalCounsel = _a.legalCounsel;
        return __awaiter(void 0, void 0, void 0, function () {
            var text;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0:
                        casename = 'Respondent solicitor remove counsel ' + dateTime.slice(0, 10);
                        return [4 /*yield*/, apiDataSetup.updateCase(casename, caseNumber, caseWithRespondentSolicitorAndCounsel_json_1.default)];
                    case 1:
                        _b.sent();
                        return [4 /*yield*/, apiDataSetup.giveAccessToCase(caseNumber, user_credentials_1.privateSolicitorOrgUser, '[SOLICITORA]')];
                    case 2:
                        _b.sent();
                        return [4 /*yield*/, apiDataSetup.giveAccessToCase(caseNumber, user_credentials_1.FPLSolicitorOrgUser, '[BARRISTER]')];
                    case 3:
                        _b.sent();
                        //  await apiDataSetup.updateCase(casename,caseNumber,caseWithResSolCounsel);
                        console.log("case" + caseNumber);
                        return [4 /*yield*/, signInPage.visit()];
                    case 4:
                        _b.sent();
                        return [4 /*yield*/, signInPage.login(user_credentials_1.privateSolicitorOrgUser.email, user_credentials_1.privateSolicitorOrgUser.password)];
                    case 5:
                        _b.sent();
                        return [4 /*yield*/, signInPage.navigateTOCaseDetails(caseNumber)];
                    case 6:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.gotoNextStep('Add or remove counsel')];
                    case 7:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.clickContinue()];
                    case 8:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.toRemoveLegalCounsel()];
                    case 9:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.clickContinue()];
                    case 10:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.checkYourAnsAndSubmit()];
                    case 11:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.tabNavigation('People in the case')];
                    case 12:
                        _b.sent();
                        text = page.locator('#case-viewer-field-read--respondents1').locator(':scope').allInnerTexts;
                        console.log('text' + text);
                        return [4 /*yield*/, (0, test_1.expect)(page.getByText('Counsel', { exact: true })).toBeHidden];
                    case 13:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.clickSignOut()];
                    case 14:
                        _b.sent();
                        return [4 /*yield*/, signInPage.login(user_credentials_1.FPLSolicitorOrgUser.email, user_credentials_1.FPLSolicitorOrgUser.password)];
                    case 15:
                        _b.sent();
                        return [4 /*yield*/, signInPage.navigateTOCaseDetails(caseNumber)];
                    case 16:
                        _b.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByRole('heading', { name: casename })).toBeHidden];
                    case 17:
                        _b.sent();
                        return [2 /*return*/];
                }
            });
        });
    });
    (0, create_fixture_1.test)('Legal counsel removed when respondent representation removed', function (_a) {
        var page = _a.page, signInPage = _a.signInPage, legalCounsel = _a.legalCounsel;
        return __awaiter(void 0, void 0, void 0, function () {
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0:
                        casename = 'Respondent representative removed ' + dateTime.slice(0, 10);
                        return [4 /*yield*/, apiDataSetup.updateCase(casename, caseNumber, caseWithRespondentSolicitorAndCounsel_json_1.default)];
                    case 1:
                        _b.sent();
                        return [4 /*yield*/, apiDataSetup.giveAccessToCase(caseNumber, user_credentials_1.privateSolicitorOrgUser, '[SOLICITORA]')];
                    case 2:
                        _b.sent();
                        return [4 /*yield*/, apiDataSetup.giveAccessToCase(caseNumber, user_credentials_1.FPLSolicitorOrgUser, '[BARRISTER]')];
                    case 3:
                        _b.sent();
                        console.log("case" + caseNumber);
                        return [4 /*yield*/, signInPage.visit()];
                    case 4:
                        _b.sent();
                        return [4 /*yield*/, signInPage.login(user_credentials_1.CTSCTeamLeadUser.email, user_credentials_1.CTSCTeamLeadUser.password)];
                    case 5:
                        _b.sent();
                        return [4 /*yield*/, signInPage.navigateTOCaseDetails(caseNumber)];
                    case 6:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.gotoNextStep('Respondents')];
                    case 7:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.removeRepresentative()];
                    case 8:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.clickContinue()];
                    case 9:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.checkYourAnsAndSubmit()];
                    case 10:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.tabNavigation('People in the case')];
                    case 11:
                        _b.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByRole('row', { name: 'Do they have legal representation? No', exact: true })).toBeVisible];
                    case 12:
                        _b.sent();
                        return [4 /*yield*/, legalCounsel.clickSignOut()];
                    case 13:
                        _b.sent();
                        return [4 /*yield*/, signInPage.login(user_credentials_1.FPLSolicitorOrgUser.email, user_credentials_1.FPLSolicitorOrgUser.password)];
                    case 14:
                        _b.sent();
                        return [4 /*yield*/, signInPage.navigateTOCaseDetails(caseNumber)];
                    case 15:
                        _b.sent();
                        return [4 /*yield*/, (0, test_1.expect)(page.getByRole('heading', { name: casename })).toBeHidden];
                    case 16:
                        _b.sent();
                        return [2 /*return*/];
                }
            });
        });
    });
    (0, create_fixture_1.test)('Legal counsel removed when child representation removed', function (_a) {
        var page = _a.page, signInPage = _a.signInPage, legalCounsel = _a.legalCounsel;
        return __awaiter(void 0, void 0, void 0, function () {
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0:
                        casename = 'Respondent representative removed ' + dateTime.slice(0, 10);
                        return [4 /*yield*/, apiDataSetup.updateCase(casename, caseNumber, caseWithChildSolicitorAndCounsel_json_1.default)];
                    case 1:
                        _b.sent();
                        return [4 /*yield*/, apiDataSetup.giveAccessToCase(caseNumber, user_credentials_1.privateSolicitorOrgUser, '[SOLICITORA]')];
                    case 2:
                        _b.sent();
                        return [4 /*yield*/, apiDataSetup.giveAccessToCase(caseNumber, user_credentials_1.FPLSolicitorOrgUser, '[BARRISTER]')];
                    case 3:
                        _b.sent();
                        console.log("case" + caseNumber);
                        return [4 /*yield*/, signInPage.visit()];
                    case 4:
                        _b.sent();
                        return [4 /*yield*/, signInPage.login(user_credentials_1.CTSCTeamLeadUser.email, user_credentials_1.CTSCTeamLeadUser.password)];
                    case 5:
                        _b.sent();
                        return [4 /*yield*/, signInPage.navigateTOCaseDetails(caseNumber)];
                    case 6:
                        _b.sent();
                        return [2 /*return*/];
                }
            });
        });
    });
});
;
(0, create_fixture_1.test)('Legal counsel removed when respondent representation removed', function (_a) {
    var page = _a.page, signInPage = _a.signInPage, legalCounsel = _a.legalCounsel;
    return __awaiter(void 0, void 0, void 0, function () {
        return __generator(this, function (_b) {
            switch (_b.label) {
                case 0:
                    casename = 'Respondent representative removed ' + dateTime.slice(0, 10);
                    return [4 /*yield*/, apiDataSetup.updateCase(casename, caseNumber, caseWithRespondentSolicitorAndCounsel_json_1.default)];
                case 1:
                    _b.sent();
                    return [4 /*yield*/, apiDataSetup.giveAccessToCase(caseNumber, user_credentials_1.privateSolicitorOrgUser, '[SOLICITORA]')];
                case 2:
                    _b.sent();
                    return [4 /*yield*/, apiDataSetup.giveAccessToCase(caseNumber, user_credentials_1.FPLSolicitorOrgUser, '[BARRISTER]')];
                case 3:
                    _b.sent();
                    console.log("case" + caseNumber);
                    return [4 /*yield*/, signInPage.visit()];
                case 4:
                    _b.sent();
                    return [4 /*yield*/, signInPage.login(user_credentials_1.CTSCTeamLeadUser.email, user_credentials_1.CTSCTeamLeadUser.password)];
                case 5:
                    _b.sent();
                    return [4 /*yield*/, signInPage.navigateTOCaseDetails(caseNumber)];
                case 6:
                    _b.sent();
                    return [4 /*yield*/, legalCounsel.gotoNextStep('Respondents')];
                case 7:
                    _b.sent();
                    return [4 /*yield*/, legalCounsel.removeRepresentative()];
                case 8:
                    _b.sent();
                    return [4 /*yield*/, legalCounsel.clickContinue()];
                case 9:
                    _b.sent();
                    return [4 /*yield*/, legalCounsel.checkYourAnsAndSubmit()];
                case 10:
                    _b.sent();
                    return [4 /*yield*/, legalCounsel.tabNavigation('People in the case')];
                case 11:
                    _b.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByRole('row', { name: 'Do they have legal representation? No', exact: true })).toBeVisible];
                case 12:
                    _b.sent();
                    return [4 /*yield*/, legalCounsel.clickSignOut()];
                case 13:
                    _b.sent();
                    return [4 /*yield*/, signInPage.login(user_credentials_1.FPLSolicitorOrgUser.email, user_credentials_1.FPLSolicitorOrgUser.password)];
                case 14:
                    _b.sent();
                    return [4 /*yield*/, signInPage.navigateTOCaseDetails(caseNumber)];
                case 15:
                    _b.sent();
                    return [4 /*yield*/, (0, test_1.expect)(page.getByRole('heading', { name: casename })).toBeHidden];
                case 16:
                    _b.sent();
                    return [2 /*return*/];
            }
        });
    });
});
(0, create_fixture_1.test)('Legal counsel removed when child representation removed', function (_a) {
    var page = _a.page, signInPage = _a.signInPage, legalCounsel = _a.legalCounsel;
    return __awaiter(void 0, void 0, void 0, function () {
        return __generator(this, function (_b) {
            switch (_b.label) {
                case 0:
                    casename = 'Respondent representative removed ' + dateTime.slice(0, 10);
                    return [4 /*yield*/, apiDataSetup.updateCase(casename, caseNumber, mandatorySubmissionFields_json_1.default)];
                case 1:
                    _b.sent();
                    //  await apiDataSetup.giveAccessToCase(caseNumber,privateSolicitorOrgUser,'[SOLICITORA]');
                    //  await apiDataSetup.giveAccessToCase(caseNumber,FPLSolicitorOrgUser,'[BARRISTER]');
                    console.log("case" + caseNumber);
                    return [2 /*return*/];
            }
        });
    });
});
;
