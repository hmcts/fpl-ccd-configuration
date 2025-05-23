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

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalApplicationsBundle {
    private final String uploadedDateTime;
    private final String author;
    private C2DocumentBundle c2DocumentBundle;
    private C2DocumentBundle c2DocumentBundleConfidential;

    private C2DocumentBundle c2DocumentBundleLA;
    private C2DocumentBundle c2DocumentBundleResp0;
    private C2DocumentBundle c2DocumentBundleResp1;
    private C2DocumentBundle c2DocumentBundleResp2;
    private C2DocumentBundle c2DocumentBundleResp3;
    private C2DocumentBundle c2DocumentBundleResp4;
    private C2DocumentBundle c2DocumentBundleResp5;
    private C2DocumentBundle c2DocumentBundleResp6;
    private C2DocumentBundle c2DocumentBundleResp7;
    private C2DocumentBundle c2DocumentBundleResp8;
    private C2DocumentBundle c2DocumentBundleResp9;
    private C2DocumentBundle c2DocumentBundleChild0;
    private C2DocumentBundle c2DocumentBundleChild1;
    private C2DocumentBundle c2DocumentBundleChild2;
    private C2DocumentBundle c2DocumentBundleChild3;
    private C2DocumentBundle c2DocumentBundleChild4;
    private C2DocumentBundle c2DocumentBundleChild5;
    private C2DocumentBundle c2DocumentBundleChild6;
    private C2DocumentBundle c2DocumentBundleChild7;
    private C2DocumentBundle c2DocumentBundleChild8;
    private C2DocumentBundle c2DocumentBundleChild9;
    private C2DocumentBundle c2DocumentBundleChild10;
    private C2DocumentBundle c2DocumentBundleChild11;
    private C2DocumentBundle c2DocumentBundleChild12;
    private C2DocumentBundle c2DocumentBundleChild13;
    private C2DocumentBundle c2DocumentBundleChild14;

    private OtherApplicationsBundle otherApplicationsBundle;
    private PBAPayment pbaPayment;
    private String removalReason;
    private String amountToPay;
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
}
