package org.saliya.dsctools.whitendata;


import mpi.Intracomm;
import mpi.MPI;
import mpi.MPIException;

import static edu.rice.hj.Module0.finalizeHabanero;
import static edu.rice.hj.Module0.initializeHabanero;

public class ParallelOptions {
    int rank = 0;
    int size = 1;
    Intracomm comm;

    int myNumVec;
    int globalVecStartIdx;


    public ParallelOptions(String [] args, int numVec) {
        // Initialize Habanero for shared memory parallelism
        initializeHabanero();
        // Set up MPI
        try {
            MPI.Init(args);
            comm = MPI.COMM_WORLD;
            rank = comm.getRank();
            size = comm.getSize();
        } catch (MPIException e) {
            throw new RuntimeException(e);
        }
        decomposeDomain(numVec);
    }



    public void endParallelism() throws MPIException {
        finalizeHabanero();
        MPI.Finalize();
    }

    private void decomposeDomain(int numVec){
        int div = numVec / size;
        int rem = numVec % size;
        myNumVec = rank < rem ? div+1 : div;
        globalVecStartIdx = rank *div + (rank < rem ? rank : rem);
    }
}
