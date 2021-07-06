package uk.gov.hmcts.reform.fpl.model.order.generated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.converter.BasicChildConverter;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrderTypeDescriptor;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Data
@Builder(toBuilder = true)
public class GeneratedOrder implements RemovableOrder {

    // this is the new type
    private final String orderType;
    private final String type;
    private final String title;
    private final String details;
    private final DocumentReference document;
    private final DocumentReference unsealedDocumentCopy;
    private final String dateOfIssue;
    private final LocalDateTime dateTimeIssued;
    private final LocalDate approvalDate;
    private final LocalDateTime approvalDateTime;
    private final String date;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final FurtherDirections furtherDirections;
    private final String expiryDate;
    private final String courtName;
    private final String uploader;
    private final String uploadedOrderDescription;
    @JsonSerialize(contentConverter = BasicChildConverter.class)
    private final List<Element<Child>> children;
    private final List<Element<Other>> others;
    private final String childrenDescription;
    private final String specialGuardians;
    private final String othersNotified;
    private String removalReason;
    private String linkedApplicationId;
    private String markedFinal;

    @JsonIgnore
    public boolean isRemovable() {
        return true;
    }

    @JsonIgnore
    public boolean isFinalOrder() {
        if (isNewVersion()) {
            return YesNo.YES == YesNo.fromString(markedFinal);
        }

        GeneratedOrderTypeDescriptor descriptor = GeneratedOrderTypeDescriptor.fromType(this.type);

        if (EMERGENCY_PROTECTION_ORDER.equals(descriptor.getType())) {
            return true;
        }

        return FINAL.equals(descriptor.getSubtype());
    }

    public String asLabel() {
        return String.format("%s - %s",
            defaultIfEmpty(title, type),
            isNewVersion() ? formatLocalDateTimeBaseUsingFormat(dateTimeIssued, "d MMMM yyyy") : dateOfIssue
        );
    }

    @JsonIgnore
    public List<UUID> getChildrenIDs() {
        if (ObjectUtils.isEmpty(children)) {
            return List.of();
        }

        return children.stream().map(Element::getId).collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean isNewVersion() {
        return Objects.nonNull(dateTimeIssued);
    }

}
