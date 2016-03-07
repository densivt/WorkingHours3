package com.densivt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;

public class Main {
    enum Flag {WD, FR, WE, HD}; //WD - понедельник-четверг, FR - пятница, WE - выходные, HD - праздничные
    static DateTimeFormatter inputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    static DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("dd.MM.yyyy");
    private static final String FILENAME = "days.json";

    public static void main(String[] args) throws Throwable {
        double workingHours = 0.0;
        DateTime dateTime1 = inputFormatter.parseDateTime(args[0]);
        DateTime dateTime2 = inputFormatter.parseDateTime(args[1]);
        if (dateTime2.isBefore(dateTime1)){
            DateTime tmp = dateTime1;
            dateTime1 = dateTime2;
            dateTime2 = tmp;
        }
        ArrayList<Day> days = new ArrayList<Day>();
        for (LocalDate date = new LocalDate(dateTime1); date.isBefore(dateTime2.toLocalDate().plusDays(1)); date = date.plusDays(1)) {
            days.add(new Day(date));
        }
        days.get(0).setStartTime(dateTime1.toLocalTime());
        days.get(days.size() - 1).setEndTime(dateTime2.toLocalTime());
        ReadyForGsoning readyForGsoning = new ReadyForGsoning(days);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(readyForGsoning);
        FileWriter writer = new FileWriter(FILENAME);
        writer.write(json);
        writer.flush();
        writer.close();
        for (Day day : days) {
            workingHours += day.getWorkingHours();
        }
        System.out.println(workingHours);
    }

    static class Day {
        LocalDate localDate;
        Flag flag;
        LocalTime startTime;
        LocalTime endTime;

        public Day(LocalDate localDate) {
            this.localDate = localDate;
            if (isHoliday(localDate)) {
                this.flag = Flag.HD;
                this.startTime = new LocalTime(10, 0);
                this.endTime = new LocalTime( 10, 0);
            }
            else if (isWeekend(localDate)) {
                this.flag = Flag.WE;
                this.startTime = new LocalTime(10, 0);
                this.endTime = new LocalTime( 10, 0);
            }
            else if (isFriday(localDate)) {
                this.flag = Flag.FR;
                this.startTime = new LocalTime(10, 0);
                this.endTime = new LocalTime( 17, 0);
            }
            else {
                this.flag = Flag.WD;
                this.startTime = new LocalTime(10, 0);
                this.endTime = new LocalTime( 18, 0);
            }
        }

        public void setStartTime(LocalTime localTime) {
            this.startTime = localTime;
        }

        public void setEndTime(LocalTime localTime) {
            this.endTime = localTime;
        }

        private boolean isFriday(LocalDate localDate) {
            if (localDate.getDayOfWeek() == 5)
                return true;
            else
                return false;
        }

        private boolean isWeekend(LocalDate localDate) {
            if (localDate.getDayOfWeek() == 6 || localDate.getDayOfWeek() == 7)
                return true;
            else
                return false;
        }

        private boolean isHoliday(LocalDate localDate) {
            if ((localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 1) ||
                    (localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 1) ||
                    (localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 2) ||
                    (localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 3) ||
                    (localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 4) ||
                    (localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 5) ||
                    (localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 6) ||
                    (localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 7) ||
                    (localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 8) ||
                    (localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 9) ||
                    (localDate.getMonthOfYear() == 1 && localDate.getDayOfMonth() == 10) ||
                    (localDate.getMonthOfYear() == 2 && localDate.getDayOfMonth() == 23) ||
                    (localDate.getMonthOfYear() == 3 && localDate.getDayOfMonth() == 8) ||
                    (localDate.getMonthOfYear() == 5 && localDate.getDayOfMonth() == 1) ||
                    (localDate.getMonthOfYear() == 5 && localDate.getDayOfMonth() == 9) ||
                    (localDate.getMonthOfYear() == 11 && localDate.getDayOfMonth() == 4))
                return true;
            else
                return false;
        }

        public String getDate(){
            return outputFormatter.print(localDate);
        }
        public double getWorkingHours(){
            if (this.flag == Flag.HD || this.flag == Flag.WE || endTime.isBefore(startTime))
                return 0.0;
            else
                return new BigDecimal((Seconds.secondsBetween(startTime, endTime).getSeconds()) / 3600.0).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        }
    }

    static class ReadyForGsoning{
        LinkedList<DayForGsoning> workingDays = new LinkedList<DayForGsoning>();
        LinkedList<DayForGsoning> weekends = new LinkedList<DayForGsoning>();
        LinkedList<DayForGsoning> celebrateDays = new LinkedList<DayForGsoning>();
        ReadyForGsoning(ArrayList<Day> days){
            for (Day day : days) {
                if (day.flag == Flag.HD){
                    celebrateDays.add(new DayForGsoning(day.getDate(), day.getWorkingHours()));
                }
                else if (day.flag == Flag.WE){
                    weekends.add(new DayForGsoning(day.getDate(), day.getWorkingHours()));
                }
                else {
                    workingDays.add(new DayForGsoning(day.getDate(), day.getWorkingHours()));
                }
            }
        }
    }

    static class DayForGsoning {
        String date;
        double workingHours;

        public DayForGsoning(String date, double workingHours) {
            this.date = date;
            this.workingHours = workingHours;
        }
    }
}
