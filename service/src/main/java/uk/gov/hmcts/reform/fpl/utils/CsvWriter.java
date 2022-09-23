package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import uk.gov.hmcts.reform.fpl.model.HearingInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

public final class CsvWriter {
    public static final FileAttribute<Set<PosixFilePermission>> ATTRIBUTE = PosixFilePermissions
            .asFileAttribute(PosixFilePermissions.fromString("rwx------"));
    private static final String[] CMS_REPORT_CSV_HEADERS = {
           "Case Number", "CCD Number", "Receipt date", "Last PLO hearing", "Next hearing" , "Age of case (weeks)", "PLO stage", "Expected FH date"
    };

    public static File writeHearingInfoToCsv(
            List<HearingInfo> hearingInfoList
    ) throws IOException {
        Path path = Files.createTempFile("CaseProgressionReport", ".xlsx", ATTRIBUTE);
        File file = path.toFile();
        CSVFormat csvFileHeader = CSVFormat.DEFAULT.builder().setHeader(CMS_REPORT_CSV_HEADERS).build();

        try (FileWriter fileWriter = new FileWriter(file);
             CSVPrinter printer = new CSVPrinter(fileWriter, csvFileHeader)) {

            for (HearingInfo hearingInfo : hearingInfoList) {
                printer.printRecord(
                    hearingInfo.getFamilyManCaseNumber(),
                    hearingInfo.getCcdNumber(),
                    hearingInfo.getDateSubmitted(),
                    hearingInfo.getLastHearing(),
                    hearingInfo.getNextHearing(),
                    hearingInfo.getAgeInWeeks(),
                    hearingInfo.getPloStage(),
                    hearingInfo.getExpectedFinalHearing()
                );
            }
        }
        return file;
    }

}
