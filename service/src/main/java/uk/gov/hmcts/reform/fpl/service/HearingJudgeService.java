package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.config.TimeConfiguration.LONDON_TIMEZONE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.HEARING_LEGAL_ADVISER;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingJudgeService {

    private final RoleAssignmentService roleAssignmentService;
    private final JudicialService judicialService;

    public static final List<String> HEARING_ROLES = List.of(
        HEARING_JUDGE.getRoleName(),
        HEARING_LEGAL_ADVISER.getRoleName());

    /**
     * Assign a hearing-judge on a case.
     *
     * @param caseId   the case id to add a case role on
     * @param userId   the user to assign hearing-judge to
     * @param starting the time to start the new role at, and the old roles to END at (- HEARING_EXPIRY_OFFSET_MINS)
     */
    private void assignHearingJudgeRole(Long caseId, String userId, ZonedDateTime starting, ZonedDateTime ending,
                                        boolean isLegalAdviser) {
        if (isLegalAdviser) {
            roleAssignmentService.assignLegalAdvisersRole(caseId, List.of(userId), HEARING_LEGAL_ADVISER, starting,
                ending);
        } else {
            roleAssignmentService.assignJudgesRole(caseId, List.of(userId), HEARING_JUDGE, starting, ending);
        }
    }

    private void assignHearingJudge(Long caseId, HearingBooking hearing, Optional<HearingBooking> next) {
        Optional<String> judgeId = getJudgeIdFromHearing(hearing);

        // end this new role at the start of the next hearing (if present) MINUS 5 minutes to avoid overlapping roles
        ZonedDateTime possibleEndDate = next.map(HearingBooking::getStartDate)
            .map(ld -> ld.atZone(LONDON_TIMEZONE))
            .orElse(null);

        final ZonedDateTime hearingStart = hearing.getStartDate().atZone(LONDON_TIMEZONE);

        judgeId.ifPresentOrElse(s -> assignHearingJudgeRole(caseId,
                s,
                hearingStart,
                possibleEndDate,
                hearing.isLegalAdviserHearing()),
            () -> log.error("No judge details on hearing starting at {} on case {} to assign roles to",
                hearing.getStartDate(), caseId));
    }

    public void syncHearingJudgeRoles(Long caseId, List<Element<HearingBooking>> hearingBookings) {
        // 1. Get all valid hearing-judge/hearing-legal-adviser roles on case
        List<HearingBooking> bookings = hearingBookings
            .stream()
            .map(Element::getValue)
            .sorted(Comparator.comparing(HearingBooking::getStartDate))
            .toList();

        List<RoleAssignment> roles = roleAssignmentService.getRolesOnCase(caseId, HEARING_ROLES);
        List<String> validRoleIds = new ArrayList<>();

        if (!bookings.isEmpty()) {
            // 2. For each hearing booking (sorted by startDate):
            for (int i = 0; i < bookings.size(); i++) {
                HearingBooking hearing = bookings.get(i);
                Optional<HearingBooking> nextHearing = i < bookings.size() - 1
                    ? Optional.of(bookings.get(i + 1))
                    : Optional.empty();

                // 2.1. Calculate the timing of the role for that hearing (i.e. start -> next hearing start)
                Pair<ZonedDateTime, ZonedDateTime> expectedRoleTimes = getExpectedRoleTime(hearing, nextHearing);

                // 2.2. If the timing of role ends in past, don't need it, continue to next booking
                if (isNotEmpty(expectedRoleTimes.getRight())
                    && expectedRoleTimes.getRight().isBefore(ZonedDateTime.now(LONDON_TIMEZONE))) {
                    log.info("skipping role, it ends in past");
                    continue;
                }

                // 2.3. Else:
                Optional<RoleAssignment> role = getRoleAssociatedWithHearing(roles, hearing);
                log.info("Does role exist? {}", role.isPresent());
                if (role.isPresent() && doesRoleMatch(role.get(), expectedRoleTimes, hearing)) {
                    // 2.3.1: If current roles has a role that matches this timeframe (& user), mark it as valid,
                    // continue to next hearing
                    log.info("role matches");
                    validRoleIds.add(role.get().getId());
                } else {
                    log.info("role doesn't match or exist");
                    // 2.3.2: Else, create a new role for this timeframe (& user)
                    assignHearingJudge(caseId, hearing, nextHearing);
                }
            }

            // 3. If first hearing was in the future (if not, skip this part)
            HearingBooking firstHearing = bookings.get(0);
            if (firstHearing.getStartDate().atZone(LONDON_TIMEZONE).isAfter(ZonedDateTime.now(LONDON_TIMEZONE))) {
                //  3.1. Check if a role exists, starting in past, ending at the first hearing
                ZonedDateTime expectedEndTime = firstHearing.getStartDate().atZone(LONDON_TIMEZONE);
                Optional<RoleAssignment> gapRole = roles.stream()
                    .filter(role -> Objects.equals(role.getEndTime(), expectedEndTime)
                        && role.getBeginTime().isBefore(ZonedDateTime.now(LONDON_TIMEZONE))
                        && role.getActorId().equals(getJudgeIdFromHearing(firstHearing).orElse(null)))
                    .findFirst();
                //  3.2. if it does, mark valid
                if (gapRole.isPresent()) {
                    validRoleIds.add(gapRole.get().getId());
                } else {
                    //  3.3. If doesn't exist, create it
                    //  (ensure we always have a hearing-judge prior to first hearing)
                    assignHearingJudgeRole(caseId,
                        getJudgeIdFromHearing(firstHearing).orElse(UUID.randomUUID().toString()),
                        ZonedDateTime.now(LONDON_TIMEZONE),
                        firstHearing.getStartDate().atZone(LONDON_TIMEZONE),
                        firstHearing.isLegalAdviserHearing());
                }
            }
        }

        // 4. Check all current roles - If role is NOT marked as valid, DELETE /role-assignment/{id}
        log.info("On case {}: Had {} valid roles, {} invalid roles being deleted",
            caseId, validRoleIds.size(), roles.size() - validRoleIds.size());

        roles.forEach(role -> {
            if (!validRoleIds.contains(role.getId())) {
                roleAssignmentService.deleteRoleAssignment(role);
            }
        });

    }

    private Optional<RoleAssignment> getRoleAssociatedWithHearing(List<RoleAssignment> roles, HearingBooking hearing) {
        ZonedDateTime roleStart = hearing.getStartDate().atZone(LONDON_TIMEZONE);
        return roles.stream()
            .filter(role -> timesEqualAtInstant(role.getBeginTime(), roleStart))
            .findFirst();
    }

    private Pair<ZonedDateTime, ZonedDateTime> getExpectedRoleTime(HearingBooking hearing,
                                                                   Optional<HearingBooking> nextHearing) {
        // if we have a next hearing, role should start at time of hearing +5 -> next hearing - 5
        if (nextHearing.isPresent()) {
            return Pair.of(hearing.getStartDate().atZone(LONDON_TIMEZONE),
                           nextHearing.get().getStartDate().atZone(LONDON_TIMEZONE));
        } else {
            return Pair.of(hearing.getStartDate().atZone(LONDON_TIMEZONE), null);
        }
    }

    private boolean doesRoleMatch(RoleAssignment role,
                                  Pair<ZonedDateTime, ZonedDateTime> times,
                                  HearingBooking hearing) {
        log.info("Begin role = {}, hearing = {}", role.getBeginTime(), times.getLeft());
        log.info("End role = {}, hearing = {}", role.getEndTime(), times.getRight());
        log.info("ActorId role = {}, hearing = {}", role.getActorId(), getJudgeIdFromHearing(hearing).orElse(null));
        return timesEqualAtInstant(role.getBeginTime(), times.getLeft())
            && timesEqualAtInstant(role.getEndTime(), times.getRight())
            && Objects.equals(role.getActorId(), getJudgeIdFromHearing(hearing).orElse(null));
    }

    private boolean timesEqualAtInstant(ZonedDateTime a, ZonedDateTime b) {
        Instant left = Optional.ofNullable(a).map(ZonedDateTime::toInstant).orElse(null);
        Instant right = Optional.ofNullable(b).map(ZonedDateTime::toInstant).orElse(null);
        return Objects.equals(left, right);
    }

    Optional<String> getJudgeIdFromHearing(HearingBooking booking) {
        if (isEmpty(booking)
            || isEmpty(booking.getJudgeAndLegalAdvisor())
            || isEmpty(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser())) {
            return Optional.empty();
        }

        if (!isEmpty(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getIdamId())) {
            return Optional.of(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getIdamId());
        }

        if (!isEmpty(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getPersonalCode())) {
            return judicialService.getJudge(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getPersonalCode())
                .map(JudicialUserProfile::getSidamId);
        }
        return Optional.empty();
    }


}
