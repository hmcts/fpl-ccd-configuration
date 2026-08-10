package uk.gov.hmcts.reform.fpl.enums.notification;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum DocumentUploaderType {
    @CCD(label = "Solicitor")
    SOLICITOR,
    @CCD(label = "Designated Local Authority")
    DESIGNATED_LOCAL_AUTHORITY,
    @CCD(label = "Secondary Local Authority")
    SECONDARY_LOCAL_AUTHORITY,
    HMCTS,
    @CCD(label = "Barrister")
    BARRISTER,
    CAFCASS,
    ROBOTICS
}
