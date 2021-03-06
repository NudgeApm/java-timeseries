/*
 * Copyright (c) 2016 Jacob Rachiele
 */

package timeseries;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * A set of constants representing standard time units, raging from nanoseconds to centuries. This class
 * wraps a {@link ChronoUnit} together with a length of time to create a broader set of time units than
 * those provided by ChronoUnit and to allow additional functionality. For even more fine-tuned time modeling
 * use the {@link TimePeriod} class.
 * 
 * @author Jacob Rachiele
 *
 */
public enum TimeUnit {

  CENTURY(ChronoUnit.CENTURIES, 1L),
  DECADE(ChronoUnit.DECADES, 1L),
  YEAR(ChronoUnit.YEARS, 1L),
  QUARTER(ChronoUnit.MONTHS, 3L),
  MONTH(ChronoUnit.MONTHS, 1L),
  WEEK(ChronoUnit.WEEKS, 1L),
  DAY(ChronoUnit.DAYS, 1L),
  HOUR(ChronoUnit.HOURS, 1L),
  MINUTE(ChronoUnit.MINUTES, 1L),
  SECOND(ChronoUnit.SECONDS, 1L),
  MILLISECOND(ChronoUnit.MILLIS, 1L),
  MICROSECOND(ChronoUnit.MICROS, 1L),
  NANOSECOND(ChronoUnit.NANOS, 1L);

  private final TemporalUnit temporalUnit;
  private final long unitLength;

  TimeUnit(final TemporalUnit timeUnit, final long unitLength) {
    this.temporalUnit = timeUnit;
    this.unitLength = unitLength;
  }

  /**
   * The TemporalUnit used as the basis for this TimeUnit.
   * @return the TemporalUnit used as the basis for this TimeUnit.
   */
  public TemporalUnit temporalUnit() {
    return this.temporalUnit;
  }

  /**
   * The length of this TimeUnit relative to the underlying TemporalUnit.
   * @return the length of this TimeUnit relative to the underlying TemporalUnit.
   */
  public long unitLength() {
    return this.unitLength;
  }

  /**
   * Compute and return the number of times this TimeUnit occurs in another TimeUnit.
   * <p>
   * For example, if this time unit is a month and the other time unit is a year, the return value should equal 12,
   * since a month occurs 12 times in one year. Note that for practical purposes the double returned by this method will
   * very often be coerced to a long or integer.
   * </p>
   * 
   * @param otherTimeUnit the time unit for which the frequency of occurrence of this time unit is to be found.
   * @return the number of times this TimeUnit occurs in the provided TimeUnit.
   */
  public double frequencyPer(final TimeUnit otherTimeUnit) {
    return otherTimeUnit.totalDuration() / this.totalDuration();

  }

  /**
   * Compute and return the number of times this TimeUnit occurs in the given TimePeriod.
   * <p>
   * For example, if this time unit is a month and the given time period is half a year, the return value should equal 6,
   * since a month occurs 6 times in one year. Note that for practical purposes the double returned by this method will
   * very often be coerced to a long or integer.
   * </p>
   * 
   * @param timePeriod the time period for which the frequency of occurrence of this time unit is to be found.
   * @return the number of times this TimeUnit occurs in the provided TimePeriod.
   */
  public double frequencyPer(final TimePeriod timePeriod) {
    return timePeriod.totalSeconds() / this.totalDuration();
  }

  /**
   * The total amount of time in this time unit measured in seconds, the base SI unit of time.
   * 
   * @return the total amount of time in this time unit measured in seconds.
   */
  double totalDuration() {
    
    Duration thisDuration = this.temporalUnit.getDuration();
    return thisDuration.getSeconds() * this.unitLength + ((thisDuration.getNano() * this.unitLength) / 1E9);

  }
}
