package es.karmadev.api.time;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Time calculator
 */
@SuppressWarnings("unused")
public class TimeCalculator {

    private final long source;

    /**
     * Initialize the time calculator
     *
     * @param source the time
     */
    public TimeCalculator(final long source) {
        this.source = source;
    }

    /**
     * Initialize the time calculator
     *
     * @param source the time
     */
    public TimeCalculator(final Instant source) {
        this.source = source.toEpochMilli();
    }

    /**
     * Initialize the time calculator
     *
     * @param source the time
     */
    public TimeCalculator(final ZonedDateTime source) {
        this.source = source.toInstant().toEpochMilli();
    }

    /**
     * Get if the time is before
     * now
     *
     * @return if the time is before now
     */
    public boolean isBefore() {
        return source - Instant.now().toEpochMilli() < 0;
    }

    /**
     * Get if the time is after now
     *
     * @return if the time is after now
     */
    public boolean isAfter() {
        return source - Instant.now().toEpochMilli() > 0;
    }

    /**
     * Get the time left from the specified
     * time to now
     *
     * @return the time left in milliseconds
     */
    public long timeLeft() {
        return timeLeft(TimeUnit.MILLISECONDS);
    }

    /**
     * Get the time left from the specified
     * time to now
     *
     * @param unit the unit to retrieve the time left
     *             as
     * @return the time left
     */
    public long timeLeft(final TimeUnit unit) {
        long value = Math.max(source - Instant.now().toEpochMilli(), 0);
        return unit.convert(value, TimeUnit.MILLISECONDS);
    }

    /**
     * Get the seconds left from the
     * specified time to now
     *
     * @return the seconds left
     */
    public long millisecondsLeft() {
        return ChronoUnit.MILLIS.between(Instant.now(), Instant.ofEpochMilli(source)) % 1000;
    }

    /**
     * Get the seconds left from the
     * specified time to now
     *
     * @return the seconds left
     */
    public long secondsLeft() {
        return ChronoUnit.SECONDS.between(Instant.now(), Instant.ofEpochMilli(source)) % 60;
    }

    /**
     * Get the minutes left from the
     * specified time to now
     *
     * @return the minutes left
     */
    public long minutesLeft() {
        return ChronoUnit.MINUTES.between(Instant.now(), Instant.ofEpochMilli(source)) % 60;
    }

    /**
     * Get the hours left from the
     * specified time to now
     *
     * @return the hours left
     */
    public long hoursLeft() {
        return ChronoUnit.HOURS.between(Instant.now(), Instant.ofEpochMilli(source)) % 24;
    }

    /**
     * Get the days left from the
     * specified time to now
     *
     * @return the days left
     */
    public int daysLeft() {
        Instant exp = Instant.ofEpochMilli(source);

        LocalDate now = LocalDate.now();
        LocalDate expire = exp.atZone(ZoneId.systemDefault()).toLocalDate();

        Period period = Period.between(now, expire);
        return period.getDays();
    }

    /**
     * Get the weeks left from the
     * specified time to now
     *
     * @return the weeks left
     */
    public int weeksLeft() {
        Instant exp = Instant.ofEpochMilli(source);

        LocalDate now = LocalDate.now();
        LocalDate expire = exp.atZone(ZoneId.systemDefault()).toLocalDate();

        Period period = Period.between(now, expire);
        int days = period.getDays();
        return days / 7;
    }

    /**
     * Get the months left from the
     * specified time to now
     *
     * @return the months left
     */
    public int monthsLeft() {
        Instant exp = Instant.ofEpochMilli(source);

        LocalDate now = LocalDate.now();
        LocalDate expire = exp.atZone(ZoneId.systemDefault()).toLocalDate();

        Period period = Period.between(now, expire);
        return period.getMonths();
    }

    /**
     * Get the years left from the
     * specified time to now
     *
     * @return the years left
     */
    public int yearsLeft() {
        Instant exp = Instant.ofEpochMilli(source);

        LocalDate now = LocalDate.now();
        LocalDate expire = exp.atZone(ZoneId.systemDefault()).toLocalDate();

        Period period = Period.between(now, expire);
        return period.getYears();
    }

    /**
     * Format the time left in a string
     *
     * @param format the time format
     * @return the formatted time left,
     * For example, if the source time is
     * now plus 1 month and the format is
     * (Let's imagine today is: 04 April 2023 05:06:07)
     * <p>
     * "yyyy years, mm months (ww weeks), dd days, hh hours, MM minutes and ss seconds left for {dd}/{mm}/{yyyy} ({ww} week) {hh}:{MM}:{ss}"
     * </p>
     * It would print back:
     * <p>
     * 00 years, 00 months (00 weeks), 29 days, 59 hours, 59 minutes and 59 seconds left for 23/05/2023 (01 week) 05:06:07
     * </p>
     */
    public String format(final String format) {
        return format(format, UnitName.create());
    }

    /**
     * Format the time left in a string
     *
     * @param format the time format
     * @param unitName the time unit names
     * @return the formatted time left,
     * For example, if the source time is
     * now plus 1 month and the format is
     * (Let's imagine today is: 04 April 2023 05:06:07)
     * <p>
     * "yyyy %year%, mm %month% (ww %week%), dd %day%, hh %hour%, MM %minute% and ss %second% left for {dd}/{mm}/{yyyy} ({ww} week) {hh}:{MM}:{ss}"
     * </p>
     * It would print back:
     * <p>
     * 00 years, 00 months (00 weeks), 29 days, 59 hours, 59 minutes and 59 seconds left for 23/05/2023 (01 week) 05:06:07
     * </p>
     */
    public String format(final String format, final UnitName unitName) {
        Instant expire = Instant.ofEpochMilli(source);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(expire, ZoneId.systemDefault());

        String year = String.format("%02d", zdt.getYear());
        String month =  String.format("%02d", zdt.getMonthValue() + 1);
        int week = zdt.get(ChronoField.ALIGNED_WEEK_OF_MONTH);
        String day = String.format("%02d", zdt.getDayOfMonth());
        String hour = String.format("%02d", zdt.getHour());
        String minute = String.format("%02d", zdt.getMinute());
        String second = String.format("%02d", zdt.getSecond());

        int yearsLeft = yearsLeft();
        int monthsLeft = monthsLeft();
        int weeksLeft = weeksLeft();
        int daysLeft = daysLeft();
        long hoursLeft = hoursLeft();
        long minutesLeft = minutesLeft();
        long secondsLeft = secondsLeft();
        long millisLeft = millisecondsLeft();

        TimeFormatter formatter = TimeFormatter.builder()
                .yyyy(yearsLeft)
                .mm(monthsLeft)
                .ww(weeksLeft)
                .dd(daysLeft)
                .hh(hoursLeft)
                .MM(minutesLeft)
                .ss(secondsLeft)
                .msms(millisLeft)
                ._yyyy_(year)
                ._mm_(month)
                ._ww_(week)
                ._dd_(day)
                ._hh_(hour)
                ._MM_(minute)
                ._ss_(second)
                .name(unitName).build();

        return formatter.parse(format);
    }
}
