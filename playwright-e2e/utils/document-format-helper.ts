const DOCUMENT_DATE_FORMATTER = new Intl.DateTimeFormat('en-GB', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
});

const DOCUMENT_TIME_FORMATTER = new Intl.DateTimeFormat('en-GB', {
    hour: 'numeric',
    minute: 'numeric',
    hourCycle: 'h12',
    timeZone: 'UTC',
});

export function formatDateToString(date: Date): string {
    return DOCUMENT_DATE_FORMATTER.format(date);
}

export function formatDateToStringWithOrdinalSuffix(date: Date): string {
    let dateString = formatDateToString(date);
    let dateStringSplit = dateString.split(" ");
    
    return `${dateStringSplit[0]}${getDayOfMonthSuffix(date.getDate())} ${dateStringSplit[1]} ${dateStringSplit[2]}`;
}

export function formatDateToStringAsDateTime(date: Date, separator: string = " at "): string {
    let dateString = formatDateToString(date);
    let timeString = formatTimeToString(date)

    return `${dateString}${separator}${timeString}`;
}

export function formatTimeToString(date: Date): string {
    return DOCUMENT_TIME_FORMATTER.format(date).replace(" ", "");
}

function getDayOfMonthSuffix(day: number) {
    if (day <= 0 || day >= 32) {
        throw "Illegal day of month: " + day;
    }

    if (day >= 11 && day <= 13) {
        return "th";
    }

    switch (day % 10) {
        case 1:
            return "st";
        case 2:
            return "nd";
        case 3:
            return "rd";
        default:
            return "th";
    }
}

export function getAge(birthDate: string): number {
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDifference = today.getMonth() - birth.getMonth();

    if (monthDifference < 0 || (monthDifference === 0 && today.getDate() < birth.getDate())) {
        age--;
    }

    return age;
}

export function formatCCDCaseNumber(caseNumber: string | number) {
    const ccdCaseNumber = String(caseNumber);
    let matchedArray = ccdCaseNumber.match(/.{1,4}/g);
    return (matchedArray) ? matchedArray.join("-") :"";
}