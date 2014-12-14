package org.saliya.dsctools.whitendata;

import mpi.Datatype;
import mpi.MPI;
import mpi.MPIException;
import mpi.UserFunction;

import java.nio.ByteBuffer;
import java.util.function.DoubleConsumer;
import java.util.stream.IntStream;

/**
 * A store similar to the {@Linkplain java.util.DoubleSummaryStatistics}
 * except this includes standard deviation and ignores min and max values\
 *
 * Most of the method documentation is directly taken from {@Linkplain java.util.DoubleSummaryStatistics}
 *
 * @implNote This implementation is not thread safe as is the original
 * {@Linkplain java.util.DoubleSummaryStatistics}, but could be used
 * on a parallel stream
 */
public class ComponentStatistics implements DoubleConsumer {
    public static int extent = Long.BYTES + 6*Double.BYTES;// count(long) and 6 sum values

    private long count = 0l;
    private double sum = 0.0d;
    private double sumCompensation; // Low order bits of sum
    private double simpleSum; // Used to compute right sum for non-finite inputs
    private double sumOfSquare = 0.0d;
    private double sumOfSquareCompensation; // Low order bits of sum
    private double simpleSumOfSquare; // Used to compute right sum for non-finite inputs


    public ComponentStatistics() {
    }

    private ComponentStatistics(long count, double sum, double sumCompensation, double simpleSum, double sumOfSquare,
                               double sumOfSquareCompensation, double simpleSumOfSquare) {
        this.count = count;
        this.sum = sum;
        this.sumCompensation = sumCompensation;
        this.simpleSum = simpleSum;
        this.sumOfSquare = sumOfSquare;
        this.sumOfSquareCompensation = sumOfSquareCompensation;
        this.simpleSumOfSquare = simpleSumOfSquare;
    }

    /**
     * Records another value into the summary information.
     *
     * @param value the input value
     */
    @Override
    public void accept(double value) {
        ++count;
        simpleSum += value;
        sumWithCompensation(value);
        double squareValue = value * value;
        simpleSumOfSquare += squareValue;
        sumOfSquareWithCompensation(squareValue);
    }

    /**
     * Combines the state of another {@code DoubleSummaryStatistics} into this
     * one.
     *
     * @param other another {@code DoubleSummaryStatistics}
     * @throws NullPointerException if {@code other} is null
     */
    public void combine(ComponentStatistics other) {
        count += other.count;
        simpleSum += other.simpleSum;
        sumWithCompensation(other.sum);
        sumWithCompensation(other.sumCompensation);
        simpleSumOfSquare += other.simpleSumOfSquare;
        sumOfSquareWithCompensation(other.sumOfSquare);
        sumOfSquareWithCompensation(other.sumOfSquareCompensation);
    }

    /**
     * Incorporate a new double value using Kahan summation /
     * compensated summation.
     */
    private void sumWithCompensation(double value) {
        double tmp = value - sumCompensation;
        double velvel = sum + tmp; // Little wolf of rounding error
        sumCompensation = (velvel - sum) - tmp;
        sum = velvel;
    }

    private void sumOfSquareWithCompensation(double value) {
        double tmp = value - sumOfSquareCompensation;
        double velvel = sumOfSquare + tmp; // Little wolf of rounding error
        sumOfSquareCompensation = (velvel - sumOfSquare) - tmp;
        sumOfSquare = velvel;
    }

    /**
     * Return the count of values recorded.
     *
     * @return the count of values
     */
    public final long getCount() {
        return count;
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
        // Better error bounds to add both terms as the final sum
        double tmp =  sum + sumCompensation;
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSum))
            // If the compensated sum is spuriously NaN from
            // accumulating one or more same-signed infinite values,
            // return the correctly-signed infinity stored in
            // simpleSum.
            return simpleSum;
        else
            return tmp;
    }

    private double getSumOfSquare() {
        // Better error bounds to add both terms as the final sum
        double tmp =  sumOfSquare + sumOfSquareCompensation;
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSumOfSquare))
            // If the compensated sum is spuriously NaN from
            // accumulating one or more same-signed infinite values,
            // return the correctly-signed infinity stored in
            // simpleSum.
            return simpleSumOfSquare;
        else
            return tmp;
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
        return getCount() > 0 ? getSum() / getCount() : 0.0d;
    }

    public final double getStandardDeviation(){
        return getCount() > 0 ? Math.sqrt((getSumOfSquare() / getCount()) - Math.pow(getAverage(), 2)) : 0.0d;
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
                "%s{count=%d, sum=%f, average=%f, stddev=%f}",
                this.getClass().getSimpleName(),
                getCount(),
                getSum(),
                getAverage(),
                getStandardDeviation());
    }

    /**
     * Adds this object to the buffer as the {@code index} th element NOT at the {@code index} th position
     * @param buffer the buffer to use
     * @param index the element number NOT the position
     */
    public void addToBuffer(ByteBuffer buffer, int index){
        buffer.position(index*extent);
        buffer.putLong(count).
                putDouble(getSum()).
                putDouble(sumCompensation).
                putDouble(simpleSum).
                putDouble(getSumOfSquare()).
                putDouble(sumOfSquareCompensation).
                putDouble(simpleSumOfSquare);
    }

    /**
     * Gets the {@code index} th element from the buffer NOT at the {@code index} th position
     * @param buffer the buffer to use
     * @param index the element number NOT the position
     */
    public static ComponentStatistics getFromBuffer(ByteBuffer buffer, int index){
        buffer.position(index*extent);
        long tmpCount = buffer.getLong();
        double tmpSum = buffer.getDouble();
        double tmpSumCompensation = buffer.getDouble();
        double tmpSimpleSum = buffer.getDouble();
        double tmpSumOfSquare = buffer.getDouble();
        double tmpSumOfSquareCompensation = buffer.getDouble();
        double tmpSimpleSumOfSquare = buffer.getDouble();
        return new ComponentStatistics(tmpCount,
                tmpSum,
                tmpSumCompensation,
                tmpSimpleSum,
                tmpSumOfSquare,
                tmpSumOfSquareCompensation,
                tmpSimpleSumOfSquare);
    }

    public static mpi.Op reduceSummaries() throws MPIException {
        return new mpi.Op(new UserFunction() {
            @Override
            public void call(Object inVec, Object inOutVec, int count, Datatype datatype) throws MPIException {
                // Nothing to do here
            }

            @Override
            public void call(ByteBuffer in, ByteBuffer inOut, int count, Datatype datatype) throws MPIException {
                if (count % extent != 0) {
                    System.out.println(
                            "invalid extent on reduce operation " + count + " expected " + extent + "*n where n>=0");
                    MPI.COMM_WORLD.abort(1);
                }
                int vecLen = count / extent;
                IntStream.range(0, vecLen).forEach(i -> {
                    ComponentStatistics inStat = ComponentStatistics.getFromBuffer(in, i);
                    ComponentStatistics inOutStat = ComponentStatistics.getFromBuffer(inOut, i);
                    inOutStat.combine(inStat);
                    inOutStat.addToBuffer(inOut, i);
                });
            }
        }, true);
    }



}
