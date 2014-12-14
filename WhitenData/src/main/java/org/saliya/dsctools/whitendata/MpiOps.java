package org.saliya.dsctools.whitendata;

import mpi.MPI;
import mpi.MPIException;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public class MpiOps {
    ByteBuffer buffer;
    int numVec;
    int vecLen;
    int capacity;
    ParallelOptions pOps;

    public MpiOps(int numVec, int vecLen, ParallelOptions pOps) {
        capacity = vecLen * ComponentStatistics.extent;
        buffer = MPI.newByteBuffer(capacity);
        this.numVec = numVec;
        this.vecLen = vecLen;
        this.pOps = pOps;
    }

    public void Allreduce(ComponentStatistics[] summaries) throws MPIException {
        IntStream.range(0, vecLen).forEach(i -> summaries[i].addToBuffer(buffer, i));
        pOps.comm.allReduce(buffer, capacity, MPI.BYTE, ComponentStatistics.reduceSummaries());
        IntStream.range(0, vecLen).forEach(i -> summaries[i] = ComponentStatistics.getFromBuffer(buffer, i));
    }
}
