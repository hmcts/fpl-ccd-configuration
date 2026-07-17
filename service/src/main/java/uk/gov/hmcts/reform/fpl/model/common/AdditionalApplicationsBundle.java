package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;

import java.lang.reflect.Field;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalApplicationsBundle {
    @CCD(label = "Date and time of upload", showCondition = "c2DocumentBundle=\"DO NOT SHOW\"", searchable = false)
    private final String uploadedDateTime;
    @CCD(label = "Uploaded by", showCondition = "c2DocumentBundle=\"Do Not Show\"", searchable = false)
    private final String author;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundle;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleConfidential;

    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleLA;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleResp0;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleResp1;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleResp2;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleResp3;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleResp4;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleResp5;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleResp6;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleResp7;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleResp8;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleResp9;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild0;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild1;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild2;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild3;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild4;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild5;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild6;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild7;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild8;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild9;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild10;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild11;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild12;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild13;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundleChild14;

    @CCD(label = "Other applications", searchable = false)
    private OtherApplicationsBundle otherApplicationsBundle;
    @CCD(label = "PBA Payment", searchable = false)
    private PBAPayment pbaPayment;
    @CCD(label = "Reason for removal", searchable = false)
    private String removalReason;
    @CCD(label = "Amount to pay", showCondition = "c2DocumentBundle=\"DO NOT SHOW\"", searchable = false)
    private String amountToPay;
    @CCD(label = "Application Reviewed by Judge", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesNo applicationReviewed;

    @JsonIgnore
    public String toLabel() {
        if (isNotEmpty(c2DocumentBundle) && isNotEmpty(otherApplicationsBundle)) {
            return String.format("C2, %s", otherApplicationsBundle.toLabel());
        }
        if (isNotEmpty(c2DocumentBundle)) {
            return c2DocumentBundle.toLabel();
        }
        if (isNotEmpty(otherApplicationsBundle)) {
            return otherApplicationsBundle.toLabel();
        }

        return uploadedDateTime;
    }

    public YesNo getApplicationReviewed() {
        if (applicationReviewed == null) {
            // DFPL-1047 Reviewing is not required for documents uploaded in historic cases
            return YesNo.YES;
        } else {
            return applicationReviewed;
        }
    }

    @JsonIgnore
    public String getApplicantName() {
        try {
            // check all possible C2 Bundles TODO SIMPLIFY THIS IF BUSINESS LOGIC MEANS EITHER CONF OR NON CONF FIELD
            for (Field f : getClass().getDeclaredFields()) {
                Object field = f.get(this);
                if (isNotEmpty(field) && field instanceof C2DocumentBundle
                    && isNotEmpty(((C2DocumentBundle) field).getApplicantName())) {
                    return ((C2DocumentBundle) field).getApplicantName();
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            return "Applicant";
        }
        // finally check the other applications bundle
        if (isNotEmpty(otherApplicationsBundle)) {
            return otherApplicationsBundle.getApplicantName();
        }
        return "Applicant";
    }

    public YesNo getHasConfidentialC2() {
        return YesNo.from(isNotEmpty(c2DocumentBundleConfidential));
    }

    @JsonIgnore
    public boolean isConfidentialC2UploadedByChildSolicitor() {
        return isNotEmpty(c2DocumentBundleChild0) || isNotEmpty(c2DocumentBundleChild1)
               || isNotEmpty(c2DocumentBundleChild2) || isNotEmpty(c2DocumentBundleChild3)
               || isNotEmpty(c2DocumentBundleChild4) || isNotEmpty(c2DocumentBundleChild5)
               || isNotEmpty(c2DocumentBundleChild6) || isNotEmpty(c2DocumentBundleChild7)
               || isNotEmpty(c2DocumentBundleChild8) || isNotEmpty(c2DocumentBundleChild9)
               || isNotEmpty(c2DocumentBundleChild10) || isNotEmpty(c2DocumentBundleChild11)
               || isNotEmpty(c2DocumentBundleChild12) || isNotEmpty(c2DocumentBundleChild13)
               || isNotEmpty(c2DocumentBundleChild14);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "hasConfidentialC2", showCondition = "c2DocumentBundle=\"Do Not Show\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasConfidentialC2;
  @CCD(
          label = "This is a confidential application and restricted viewing applies",
          showCondition = "hasConfidentialC2=\"YES\" AND c2DocumentBundleConfidential!=\"*\" AND c2DocumentBundleLA!=\"*\" AND c2DocumentBundleResp0!=\"*\" AND c2DocumentBundleResp1!=\"*\" AND c2DocumentBundleResp2!=\"*\" AND c2DocumentBundleResp3!=\"*\" AND c2DocumentBundleResp4!=\"*\" AND c2DocumentBundleResp5!=\"*\" AND c2DocumentBundleResp6!=\"*\" AND c2DocumentBundleResp7!=\"*\" AND c2DocumentBundleResp8!=\"*\" AND c2DocumentBundleResp9!=\"*\" AND c2DocumentBundleChild0!=\"*\" AND c2DocumentBundleChild1!=\"*\" AND c2DocumentBundleChild2!=\"*\" AND c2DocumentBundleChild3!=\"*\" AND c2DocumentBundleChild4!=\"*\" AND c2DocumentBundleChild5!=\"*\" AND c2DocumentBundleChild6!=\"*\" AND c2DocumentBundleChild7!=\"*\" AND c2DocumentBundleChild8!=\"*\" AND c2DocumentBundleChild9!=\"*\" AND c2DocumentBundleChild10!=\"*\" AND c2DocumentBundleChild11!=\"*\" AND c2DocumentBundleChild12!=\"*\" AND c2DocumentBundleChild13!=\"*\" AND c2DocumentBundleChild14!=\"*\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String confidentialC2RestrictedLabel;
  // ==== end synthesised definition-only fields ====
}
