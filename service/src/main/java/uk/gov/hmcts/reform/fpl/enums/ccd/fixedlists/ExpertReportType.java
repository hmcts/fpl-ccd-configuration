package uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExpertReportType {

    @JsonProperty("pediatric")
    PEDIATRIC("pediatric", "Pediatric"),

    @JsonProperty("pediatricRadiologist")
    PEDIATRIC_RADIOLOGIST("pediatricRadiologist", "Pediatric Radiologist"),

    @JsonProperty("OtherMedicalReport")
    OTHER_MEDICAL_REPORT("OtherMedicalReport", "Other Medical report"),

    @JsonProperty("residentialAssessment")
    RESIDENTIAL_ASSESSMENT("residentialAssessment", "Family Centre Assessments - Residential"),

    @JsonProperty("nonResidentialAssessment")
    NON_RESIDENTIAL_ASSESSMENT("nonResidentialAssessment", "Family Centre Assessments - Non-Residential"),

    @JsonProperty("psychiatricChildAndParent")
    PSYCHIATRIC_CHILD_AND_PARENT("psychiatricChildAndParent", "Psychiatric - On child and Parent(s)/carers"),

    @JsonProperty("psychiatricOnChild")
    PSYCHIATRIC_ON_CHILD("psychiatricOnChild", "Psychiatric - On child only"),

    @JsonProperty("psychiatricOnParents")
    PSYCHIATRIC_ON_PARENTS("psychiatricOnParents", "Adult Psychiatric Report on Parents(s)"),

    @JsonProperty("clinicalReportOnChild")
    CLINICAL_REPORT_ON_CHILD("clinicalReportOnChild", "Psychological Report on Child Only - Clinical"),

    @JsonProperty("educationalReportOnChild")
    EDUCATIONAL_REPORT_ON_CHILD("educationalReportOnChild", "Psychological Report on Child Only - Educational"),

    @JsonProperty("cognitiveReportOnParent")
    COGNITIVE_REPORT_ON_PARENT("cognitiveReportOnParent", "Psychological Report on Parent(s) - full cognitive"),

    @JsonProperty("functioningReportOnParent")
    FUNCTIONING_REPORT_ON_PARENT("functioningReportOnParent", "Psychological Report on Parent(s) - functioning"),

    @JsonProperty("physiologicalReportOnParentAndChild")
    PHYSIOLOGICAL_REPORT_ON_PARENT_AND_CHILD("physiologicalReportOnParentAndChild",
        "Psychological Report on Parent(s) and child"),

    @JsonProperty("multiDisciplinaryAssessment")
    MULTI_DISCIPLINARY_ASSESSMENT("multiDisciplinaryAssessment", "Multi Disciplinary Assessment"),

    @JsonProperty("independentSocialWorker")
    INDEPENDENT_SOCIAL_WORKER("independentSocialWorker", "Independent social worker"),

    @JsonProperty("haematologist")
    HAEMATOLOGISTS("haematologist", "Haematologist"),

    @JsonProperty("opthamologist")
    OPHTHALMOLOGIST("opthamologist", "Ophthalmologist"),

    @JsonProperty("neurosurgeon")
    NEUROSURGEON("neurosurgeon", "Neurosurgeon"),

    @JsonProperty("otherExpertReport")
    OTHER_EXPERT_REPORT("otherExpertReport", "Other Expert Report"),

    @JsonProperty("professionalDrug")
    PROFESSIONAL_DRUG("professionalDrug", "Professional: Drug/Alcohol"),

    @JsonProperty("professionalHair")
    PROFESSIONAL_HAIR("professionalHair", "Professional: Hair Strand"),

    @JsonProperty("professionalDNA")
    PROFESSIONAL_DNA("professionalDNA", "Professional: DNA testing"),

    @JsonProperty("professionalOther")
    PROFESSIONAL_OTHER("professionalOther", "Professional: Other"),

    @JsonProperty("toxicologyReport")
    TOXICOLOGY_REPORT("toxicologyReport", "Toxicology report/statement");

    private final String value;
    private final String label;
}
