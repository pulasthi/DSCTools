package org.saliya.dsctools.whitendata;

public class Test {
    public static void main(String[] args) {
        int numVec = 14;
        int mpiSize = 8;
        for (int mpiRank = 0; mpiRank < mpiSize; ++mpiRank) {
            int div = numVec / mpiSize;
            int rem = numVec % mpiSize;
            int myNumVec = mpiRank < rem ? div + 1 : div;
            int vecBeforeMe = mpiRank * div + (mpiRank < rem ? mpiRank : rem);
            System.out.println(myNumVec +"\t" + vecBeforeMe);
        }
    }
}
