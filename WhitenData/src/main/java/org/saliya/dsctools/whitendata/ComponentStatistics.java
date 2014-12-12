package org.saliya.dsctools.whitendata;

import java.util.DoubleSummaryStatistics;

/**
 * A wrapper extension of the {@Linkplain java.util.DoubleSummaryStatistics}
 * to include standard deviation.
 *
 * @implNote This implementation is not thread safe as is the original
 * {@Linkplain java.util.DoubleSummaryStatistics}, but could be used
 * on a parallel stream
 */
public class ComponentStatistics {
    private double squareSum = 0.0d;
    private DoubleSummaryStatistics statistics;

    public ComponentStatistics() {
        statistics = new DoubleSummaryStatistics();
    }

    public void accept(double value){
        statistics.accept(value);
        squareSum += value*value;
    }

    public void combine(ComponentStatistics other){
        statistics.combine(other.statistics);
        squareSum += other.squareSum;
    }

    /**
     * Return the count of values recorded.
     *
     * @return the count of values
     */
    public final long getCount() {
        return statistics.getCount();
    }

    /**
     * Returns the sum of values recorded, or zero if no values have been
     * recorded. The sum returned can vary depending upon the order in which
     * values are recorded. This is due to accumulated rounding error in
     * addition of values of differing magnitudes. Values sorted by increasing
     * absolute magnitude tend to yield more accurate results.  If any recorded
     * value is a {@code NaN} or the sum is at any point a {@code NaN} then the
     * sum will be {@code NaN}.
     *
     * @return the sum of values, or zero if none
     */
    public final double getSum() {
        return statistics.getSum();
    }

    /**
     * Returns the minimum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.POSITIVE_INFINITY} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the minimum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.POSITIVE_INFINITY} if no values were
     * recorded
     */
    public final double getMin() {
        return statistics.getMin();
    }

    /**
     * Returns the maximum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.NEGATIVE_INFINITY} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the maximum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.NEGATIVE_INFINITY} if no values were
     * recorded
     */
    public final double getMax() {
        return statistics.getMax();
    }

    /**
     * Returns the arithmetic mean of values recorded, or zero if no values have been
     * recorded. The average returned can vary depending upon the order in
     * which values are recorded. This is due to accumulated rounding error in
     * addition of values of differing magnitudes. Values sorted by increasing
     * absolute magnitude tend to yield more accurate results. If any recorded
     * value is a {@code NaN} or the sum is at any point a {@code NaN} then the
     * average will be {@code NaN}.
     *
     * @return the arithmetic mean of values, or zero if none
     */
    public final double getAverage() {
        return statistics.getAverage();
    }

    public final double getStandardDeviation(){
        return statistics.getCount() > 0 ? Math.sqrt((squareSum / statistics.getCount()) - Math.pow(statistics.getAverage(), 2)) : 0.0d;
    }

    /**
     * {@inheritDoc}
     *
     * Returns a non-empty string representation of this object suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     */
    @Override
    public String toString() {
        return String.format(
                "%s{count=%d, sum=%f, min=%f, average=%f, max=%f, stddev=%f}",
                this.getClass().getSimpleName(),
                getCount(),
                getSum(),
                getMin(),
                getAverage(),
                getMax(),
                getStandardDeviation());
    }

}
