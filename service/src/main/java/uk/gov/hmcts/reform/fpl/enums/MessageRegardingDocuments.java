package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "MessageAttachmentSelector", generate = true)
public enum MessageRegardingDocuments {
    @CCD(label = "Application")
    APPLICATION,
    @CCD(label = "Document")
    DOCUMENT,
    @CCD(label = "No attachment")
    NONE
}
