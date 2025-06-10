package com.swisscom.travelmate.engine.shared.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

public class DateUtils {

    public static final Calendar calendar = Calendar.getInstance();

    public static LocalDate convertToLocalDate(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static List<Date> create10DayDateRange(){
        // Create a stream of 5 days before, current day, and 5 days after
        Date currentDate = calendar.getTime();
        List<Date> dateRange = IntStream.rangeClosed(-5, 4) // Generate a range from -5 to 5 (inclusive)
                .mapToObj(i -> {
                    calendar.setTime(currentDate);
                    calendar.add(Calendar.DAY_OF_MONTH, i); // Adjust the day by i
                    return calendar.getTime();
                })
                .toList();

        calendar.clear();
        return dateRange;
    }

    public static Date addDays(Date date, int daysToAdd){
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);

        Date updatedDate = calendar.getTime();
        calendar.clear();
        return updatedDate;
    }

    public static Date removeDays(Date date, int daysToRemove){
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, daysToRemove * -1);

        Date updatedDate = calendar.getTime();
        calendar.clear();
        return updatedDate;
    }
}
