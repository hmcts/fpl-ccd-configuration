package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey.NEW_HEARING_LABEL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey.NEW_HEARING_SELECTOR;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.prepareJudgeFields;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingBookingService {
    public static final String HEARING_DETAILS_KEY = "hearingDetails";
    public static final String SELECTED_HEARING_IDS = "selectedHearingIds";

    private final Time time;
    private final NoticeOfHearingGenerationService noticeOfHearingGenerationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;

    public List<Element<HearingBooking>> expandHearingBookingCollection(CaseData caseData) {
        return ofNullable(caseData.getHearingDetails())
            .orElse(newArrayList(element(HearingBooking.builder().build())));
    }

    public List<Element<HearingBooking>> getPastHearings(List<Element<HearingBooking>> hearingDetails) {
        return hearingDetails.stream().filter(this::isPastHearing).collect(toList());
    }

    public List<Element<HearingBooking>> getFutureHearings(List<Element<HearingBooking>> hearingDetails) {
        return hearingDetails.stream().filter(element -> !isPastHearing(element)).collect(toList());
    }

    public void removePastHearings(List<Element<HearingBooking>> hearingDetails) {
        if (isNotEmpty(hearingDetails)) {
            hearingDetails.removeAll(getPastHearings(hearingDetails));
        }
    }

    public HearingBooking getMostUrgentHearingBooking(List<Element<HearingBooking>> hearingDetails) {
        return unwrapElements(hearingDetails).stream()
            .filter(hearing -> hearing.getStartDate().isAfter(time.now()))
            .min(comparing(HearingBooking::getStartDate))
            .orElseThrow(NoHearingBookingException::new);
    }

    //Can be removed in future, see getFirstHearing in CaseData.java
    public Optional<HearingBooking> getFirstHearing(List<Element<HearingBooking>> hearingDetails) {
        return unwrapElements(hearingDetails).stream()
            .min(comparing(HearingBooking::getStartDate));
    }

    public static HearingBooking getHearingBookingByUUID(List<Element<HearingBooking>> hearingDetails, UUID elementId) {
        return hearingDetails.stream()
            .filter(hearingBookingElement -> hearingBookingElement.getId().equals(elementId))
            .map(Element::getValue)
            .findFirst()
            .orElse(null);
    }

    /**
     * Combines two lists of hearings into one, ordered by start date.
     * Implemented due to work around with hearing start date validation.
     *
     * @param newHearings the first list of hearing bookings to combine.
     * @param oldHearings the second list of hearing bookings to combine.
     * @return an ordered list of hearing bookings.
     */
    public List<Element<HearingBooking>> combineHearingDetails(List<Element<HearingBooking>> newHearings,
                                                               List<Element<HearingBooking>> oldHearings) {
        List<Element<HearingBooking>> combinedHearingDetails = newArrayList();
        combinedHearingDetails.addAll(newHearings);

        oldHearings.forEach(hearing -> {
            UUID id = hearing.getId();
            if (combinedHearingDetails.stream().noneMatch(oldHearing -> oldHearing.getId().equals(id))) {
                combinedHearingDetails.add(hearing);
            }
        });

        combinedHearingDetails.sort(comparing(element -> element.getValue().getStartDate()));

        return combinedHearingDetails;
    }

    public List<Element<HearingBooking>> setHearingJudge(List<Element<HearingBooking>> hearingBookings,
                                                         Judge allocatedJudge) {
        return hearingBookings.stream()
            .map(element -> {
                HearingBooking hearingBooking = element.getValue();

                JudgeAndLegalAdvisor selectedJudge =
                    getSelectedJudge(hearingBooking.getJudgeAndLegalAdvisor(), allocatedJudge);

                removeAllocatedJudgeProperties(selectedJudge);
                hearingBooking.setJudgeAndLegalAdvisor(selectedJudge);

                return buildHearingBookingElement(element.getId(), hearingBooking);
            }).collect(toList());
    }

    public List<Element<HearingBooking>> resetHearingJudge(List<Element<HearingBooking>> hearingBookings,
                                                           Judge allocatedJudge) {
        return hearingBookings.stream()
            .map(element -> {
                HearingBooking hearingBooking = element.getValue();
                JudgeAndLegalAdvisor judgeAndLegalAdvisor = hearingBooking.getJudgeAndLegalAdvisor();

                if (judgeAndLegalAdvisor != null) {
                    judgeAndLegalAdvisor = prepareJudgeFields(judgeAndLegalAdvisor, allocatedJudge);
                    hearingBooking.setJudgeAndLegalAdvisor(judgeAndLegalAdvisor);
                    return buildHearingBookingElement(element.getId(), hearingBooking);
                }

                return element;
            }).collect(toList());
    }

    public List<Element<HearingBooking>> getNewHearings(
        List<Element<HearingBooking>> newHearings, List<Element<HearingBooking>> oldHearings) {
        List<UUID> oldHearingIDs = oldHearings.stream()
            .map(Element::getId)
            .collect(Collectors.toList());

        return newHearings.stream()
            .filter(newHearing -> !oldHearingIDs.contains(newHearing.getId()))
            .collect(Collectors.toList());
    }

    public Map<String, Object> getHearingNoticeCaseFields(
        List<Element<HearingBooking>> newHearings, List<Element<HearingBooking>> oldHearings) {
        StringBuilder stringBuilder = new StringBuilder();
        List<UUID> oldHearingIDs = oldHearings.stream()
            .map(Element::getId)
            .collect(Collectors.toList());

        int selectorMinValue = -1;
        for (int i = 0; i < newHearings.size(); i++) {
            if (!oldHearingIDs.contains(newHearings.get(i).getId())) {
                Element<HearingBooking> hearingElement = newHearings.get(i);
                HearingBooking hearingBooking = hearingElement.getValue();
                String newHearingLabel = buildNewHearingLabel(hearingBooking, i);

                stringBuilder.append(newHearingLabel).append("\n");

                if (selectorMinValue < 0) {
                    selectorMinValue = i;
                }
            }
        }
        Selector selector = Selector.newSelector(newHearings.size(), selectorMinValue, newHearings.size());

        return Map.of(NEW_HEARING_LABEL.getKey(), stringBuilder.toString(), NEW_HEARING_SELECTOR.getKey(), selector);
    }

    public List<Element<HearingBooking>> getSelectedHearings(Selector hearingSelector,
                                                             List<Element<HearingBooking>> hearings) {
        if (hearings == null || hearings.isEmpty() || hearingSelector == null) {
            return List.of();
        } else {
            return hearingSelector.getSelected().stream()
                .map(hearings::get)
                .collect(toList());
        }
    }

    public List<Element<HearingBooking>> getSelectedHearings(List<UUID> selectedHearingIDs,
                                                             List<Element<HearingBooking>> hearings) {
        if (hearings == null || hearings.isEmpty() || selectedHearingIDs == null || selectedHearingIDs.isEmpty()) {
            return List.of();
        } else {
            List<Element<HearingBooking>> selectedHearings = newArrayList();
            hearings.forEach(hearing -> {
                if (selectedHearingIDs.stream().anyMatch(id -> id.equals(hearing.getId()))) {
                    selectedHearings.add(hearing);
                }
            });
            return selectedHearings;
        }
    }

    public void attachDocumentsForSelectedHearings(CaseData caseData, List<Element<HearingBooking>> selectedHearings) {
        selectedHearings
            .forEach(hearing -> {
                HearingBooking booking = hearing.getValue();
                DocmosisNoticeOfHearing dnof = noticeOfHearingGenerationService.getTemplateData(caseData,
                    hearing.getValue());
                DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(dnof,
                    NOTICE_OF_HEARING);
                Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
                    NOTICE_OF_HEARING.getDocumentTitle(now()));
                booking.setNoticeOfHearing(DocumentReference.buildFromDocument(document));
            });
    }

    private boolean isPastHearing(Element<HearingBooking> element) {
        return ofNullable(element.getValue())
            .map(HearingBooking::getStartDate)
            .filter(hearingDate -> hearingDate.isBefore(time.now()))
            .isPresent();
    }

    private Element<HearingBooking> buildHearingBookingElement(UUID id, HearingBooking hearingBooking) {
        return Element.<HearingBooking>builder()
            .id(id)
            .value(hearingBooking)
            .build();
    }

    private String buildNewHearingLabel(HearingBooking hearingBooking, int i) {
        return format("Hearing %d: %s hearing %s", i + 1, hearingBooking.getType() != OTHER
                ? hearingBooking.getType().getLabel() : capitalize(hearingBooking.getTypeDetails()),
            formatLocalDateTimeBaseUsingFormat(hearingBooking.getStartDate(), DATE));
    }
}
