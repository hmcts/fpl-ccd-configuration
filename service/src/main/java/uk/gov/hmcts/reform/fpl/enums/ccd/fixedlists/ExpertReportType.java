package uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ExpertReportList", generate = true)
@Getter
@RequiredArgsConstructor
public enum ExpertReportType {

    @CCD(label = "Pediatric")
    @JsonProperty("pediatric")
    PEDIATRIC("pediatric", "Pediatric"),

    @CCD(label = "Pediatric Radiologist")
    @JsonProperty("pediatricRadiologist")
    PEDIATRIC_RADIOLOGIST("pediatricRadiologist", "Pediatric Radiologist"),

    @CCD(label = "Other Medical report")
    @JsonProperty("OtherMedicalReport")
    OTHER_MEDICAL_REPORT("OtherMedicalReport", "Other Medical report"),

    @CCD(label = "Family Centre Assessments - Residential")
    @JsonProperty("residentialAssessment")
    RESIDENTIAL_ASSESSMENT("residentialAssessment", "Family Centre Assessments - Residential"),

    @CCD(label = "Family Centre Assessments - Non-Residential")
    @JsonProperty("nonResidentialAssessment")
    NON_RESIDENTIAL_ASSESSMENT("nonResidentialAssessment", "Family Centre Assessments - Non-Residential"),

    @CCD(label = "Psychiatric - On child and Parent(s)/carers")
    @JsonProperty("psychiatricChildAndParent")
    PSYCHIATRIC_CHILD_AND_PARENT("psychiatricChildAndParent", "Psychiatric - On child and Parent(s)/carers"),

    @CCD(label = "Psychiatric - On child only")
    @JsonProperty("psychiatricOnChild")
    PSYCHIATRIC_ON_CHILD("psychiatricOnChild", "Psychiatric - On child only"),

    @CCD(label = "Adult Psychiatric Report on Parents(s)")
    @JsonProperty("psychiatricOnParents")
    PSYCHIATRIC_ON_PARENTS("psychiatricOnParents", "Adult Psychiatric Report on Parents(s)"),

    @CCD(label = "Psychological Report on Child Only - Clinical")
    @JsonProperty("clinicalReportOnChild")
    CLINICAL_REPORT_ON_CHILD("clinicalReportOnChild", "Psychological Report on Child Only - Clinical"),

    @CCD(label = "Psychological Report on Child Only - Educational")
    @JsonProperty("educationalReportOnChild")
    EDUCATIONAL_REPORT_ON_CHILD("educationalReportOnChild", "Psychological Report on Child Only - Educational"),

    @CCD(label = "Psychological Report on Parent(s) - full cognitive")
    @JsonProperty("cognitiveReportOnParent")
    COGNITIVE_REPORT_ON_PARENT("cognitiveReportOnParent", "Psychological Report on Parent(s) - full cognitive"),

    @CCD(label = "Psychological Report on Parent(s) - functioning")
    @JsonProperty("functioningReportOnParent")
    FUNCTIONING_REPORT_ON_PARENT("functioningReportOnParent", "Psychological Report on Parent(s) - functioning"),

    @CCD(label = "Psychological Report on Parent(s) and child")
    @JsonProperty("physiologicalReportOnParentAndChild")
    PHYSIOLOGICAL_REPORT_ON_PARENT_AND_CHILD("physiologicalReportOnParentAndChild",
        "Psychological Report on Parent(s) and child"),

    @CCD(label = "Multi Disciplinary Assessment")
    @JsonProperty("multiDisciplinaryAssessment")
    MULTI_DISCIPLINARY_ASSESSMENT("multiDisciplinaryAssessment", "Multi Disciplinary Assessment"),

    @CCD(label = "Independent social worker")
    @JsonProperty("independentSocialWorker")
    INDEPENDENT_SOCIAL_WORKER("independentSocialWorker", "Independent social worker"),

    @JsonProperty("haematologist")
    HAEMATOLOGISTS("haematologist", "Haematologist"),

    @JsonProperty("opthamologist")
    OPHTHALMOLOGIST("opthamologist", "Ophthalmologist"),

    @CCD(label = "Neurosurgeon")
    @JsonProperty("neurosurgeon")
    NEUROSURGEON("neurosurgeon", "Neurosurgeon"),

    @CCD(label = "Other Expert Report")
    @JsonProperty("otherExpertReport")
    OTHER_EXPERT_REPORT("otherExpertReport", "Other Expert Report"),

    @CCD(label = "Professional: Drug/Alcohol")
    @JsonProperty("professionalDrug")
    PROFESSIONAL_DRUG("professionalDrug", "Professional: Drug/Alcohol"),

    @CCD(label = "Professional: Hair Strand")
    @JsonProperty("professionalHair")
    PROFESSIONAL_HAIR("professionalHair", "Professional: Hair Strand"),

    @CCD(label = "Professional: DNA testing")
    @JsonProperty("professionalDNA")
    PROFESSIONAL_DNA("professionalDNA", "Professional: DNA testing"),

    @CCD(label = "Professional: Other")
    @JsonProperty("professionalOther")
    PROFESSIONAL_OTHER("professionalOther", "Professional: Other"),

    @CCD(label = "Toxicology report/statement")
    @JsonProperty("toxicologyReport")
    TOXICOLOGY_REPORT("toxicologyReport", "Toxicology report/statement");

    private final String value;
    private final String label;
}
