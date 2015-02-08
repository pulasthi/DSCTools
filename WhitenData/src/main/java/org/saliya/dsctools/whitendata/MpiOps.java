package org.saliya.dsctools.whitendata;

import mpi.MPI;
import mpi.MPIException;
import mpi.Op;

import java.nio.*;
import java.util.stream.IntStream;

public class MpiOps {
    IntBuffer intBuffer;
    LongBuffer longBuffer;
    ByteBuffer buffer;
    ByteBuffer flagBuffer;
    int numVec;
    int vecLen;
    int capacity;
    ParallelOptions pOps;

    public MpiOps(int numVec, int vecLen, ParallelOptions pOps) {
        capacity = vecLen * ComponentStatistics.extent;
        buffer = MPI.newByteBuffer(capacity);
        flagBuffer = MPI.newByteBuffer(1);
        intBuffer = MPI.newIntBuffer(1);
        longBuffer = MPI.newLongBuffer(1);
        this.numVec = numVec;
        this.vecLen = vecLen;
        this.pOps = pOps;
    }


    public void notify (int destination) throws MPIException {
        pOps.comm.send(flagBuffer.put(0,((byte)1)), 1, MPI.BYTE, destination, 99);
    }

    public void receive(int source) throws MPIException {
        pOps.comm.recv(flagBuffer, 1, MPI.BYTE, source, 99);
    }


    public void allReduce(ComponentStatistics[] summaries) throws MPIException {
        IntStream.range(0, vecLen).forEach(i -> summaries[i].addToBuffer(buffer, i));
        pOps.comm.allReduce(buffer, capacity, MPI.BYTE, ComponentStatistics.reduceSummaries());
        IntStream.range(0, vecLen).forEach(i -> summaries[i] = ComponentStatistics.getFromBuffer(buffer, i));
    }

    public int allReduce(int value, Op reduceOp) throws MPIException {
        intBuffer.put(0,value);
        pOps.comm.allReduce(intBuffer, 1, MPI.INT, reduceOp);
        return intBuffer.get(0);
    }

    public long allReduce(long value, Op reduceOp) throws MPIException {
        longBuffer.put(0,value);
        pOps.comm.allReduce(longBuffer, 1, MPI.LONG, reduceOp);
        return longBuffer.get(0);
    }
}
