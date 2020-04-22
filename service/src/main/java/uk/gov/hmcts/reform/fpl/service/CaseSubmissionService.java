package uk.gov.hmcts.reform.fpl.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisSubmittedForm;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseSubmissionService extends DocmosisTemplateDataGeneration<DocmosisSubmittedForm> {
    private static final String NEW_LINE = "\n";
    private static final String DEFAULT_STRING = "-";
    private static LocalDate TODAY = LocalDate.now();

    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;

    public Document generateSubmittedFormPDF(CaseData caseData, String userFullName, String pdfFileName)
            throws IOException {
        DocmosisSubmittedForm submittedFormData = buildSubmittedFormData(caseData, userFullName);

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(submittedFormData, C110A);

        return uploadDocumentService.uploadPDF(document.getBytes(), pdfFileName);
    }

    public DocmosisSubmittedForm buildSubmittedFormData(CaseData caseData, String userFullName) throws IOException {
        DocmosisSubmittedForm.Builder applicationFormBuilder = DocmosisSubmittedForm.builder();

        applicationFormBuilder
                .applicantOrganisations(getApplicantsOrganisations(caseData.getAllApplicants()))
                .respondentNames(getRespondentsNames(caseData.getAllRespondents()))
                .submittedDate(formatLocalDateToString(TODAY, DATE))
                .ordersNeeded(getOrdersNeeded(caseData.getOrders()))
                .directionsNeeded(getDirectionsNeeded(caseData.getOrders()))
                .allocation(caseData.getAllocationProposal())
                .hearing(caseData.getHearing())
                .hearingPref(caseData.getHearingPreferences())
                .userFullName(userFullName);
        applicationFormBuilder.courtseal(format(BASE_64, generateCourtSealEncodedString()));

        return applicationFormBuilder.build();
    }

    public String getApplicantsOrganisations(List<Element<Applicant>> applicants) {
        return applicants.stream().map(Element::getValue).filter(Objects::nonNull).map(Applicant::getParty)
                .filter(Objects::nonNull).map(ApplicantParty::getOrganisationName).filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(NEW_LINE));
    }

    public String getRespondentsNames(List<Element<Respondent>> respondents) {
        return respondents.stream().map(Element::getValue).filter(Objects::nonNull).map(Respondent::getParty)
                .filter(Objects::nonNull).map(RespondentParty::getFullName).filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(NEW_LINE));
    }

    public String getOrdersNeeded(Orders orders) {
        StringBuilder sb = new StringBuilder();
        if (orders != null && orders.getOrderType() != null) {
            sb.append(orders.getOrderType().stream()
                    .map(OrderType::getLabel)
                    .collect(Collectors.joining(NEW_LINE)));
            if (orders.getOtherOrder() != null) {
                sb.append(orders.getOtherOrder());
                sb.append(NEW_LINE);
            }
            if (orders.getEmergencyProtectionOrders() != null) {
                sb.append(orders.getEmergencyProtectionOrders().stream()
                    .map(EmergencyProtectionOrdersType::getLabel)
                    .collect(Collectors.joining(NEW_LINE)));
                if (orders.getEmergencyProtectionOrderDetails() != null) {
                    sb.append(orders.getEmergencyProtectionOrderDetails());
                }
            }
        } else {
            sb.append(DEFAULT_STRING);
        }
        return sb.toString();
    }

    public String getDirectionsNeeded(Orders orders) {
        StringBuilder sb = new StringBuilder();
        if (orders != null && (orders.getOrderType() != null || orders.getDirections() != null)) {
            if (orders.getEmergencyProtectionOrderDirections() != null) {
                sb.append(orders.getEmergencyProtectionOrderDirections().stream()
                    .map(EmergencyProtectionOrderDirectionsType::getLabel)
                    .collect(Collectors.joining(NEW_LINE)));
            }
            if (orders.getEmergencyProtectionOrderDirectionDetails() != null) {
                sb.append(orders.getEmergencyProtectionOrderDirectionDetails());
                sb.append(NEW_LINE);
            }
            if (orders.getDirections() != null) {
                sb.append(orders.getDirections());
                sb.append(NEW_LINE);
            }
            if (orders.getDirectionDetails() != null) {
                sb.append(orders.getDirectionDetails());
            }
        } else {
            sb.append(DEFAULT_STRING);
        }
        return sb.toString();
    }

    private String removeNewLineAtTheEnd(String documentData) {
        if (documentData.contains(NEW_LINE)) {
            StringBuilder stringBuilder = new StringBuilder(documentData);
            stringBuilder.delete(documentData.lastIndexOf(NEW_LINE), documentData.lastIndexOf(NEW_LINE) + 1);

            documentData = stringBuilder.toString();
        }

        return documentData;
    }

    @Override
    public DocmosisSubmittedForm getTemplateData(CaseData caseData) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
