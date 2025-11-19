
export const getCurrentdate = async ()  => {
    let date = new Date();
    return new Intl.DateTimeFormat('en', {year: 'numeric', month: 'long', day: 'numeric'}).format(date);
}

export const get = async ()  => {
    let date = new Date();
    return new Intl.DateTimeFormat('en', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
    }).format(date);
}

export const subtractMonthDate = async (subMonth:number)  => {
    let date = new Date();
    date.setMonth(date.getMonth() - subMonth);
    return date
}
export function addMonthsToDate(date: Date, months: number): Date {
    const newDate = new Date(date);
    newDate.setMonth(newDate.getMonth() + months);
    return newDate;
}
export function formatToLongDate12hrTime(date: Date): string {
    const day = date.getDate();
    const month = date.toLocaleString('en-GB', { month: 'long' });
    const year = date.getFullYear();

    let hours = date.getHours();
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const period = hours >= 12 ? 'pm' : 'am';
    hours = hours % 12 || 12;

    return `${day} ${month} ${year} at ${hours}:${minutes}${period}`;
}
