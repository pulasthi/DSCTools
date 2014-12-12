package org.saliya.dsctools.whitendata;


import mpi.Intracomm;
import mpi.MPI;
import mpi.MPIException;

import static edu.rice.hj.Module0.finalizeHabanero;
import static edu.rice.hj.Module0.initializeHabanero;

public class ParallelOptions {
    int mpiRank;
    int mpiSize;
    Intracomm mpiWorld;

    int myNumVec;
    int globalVecStartIdx;


    public ParallelOptions(String [] args, int numVec) {
        // Initialize Habanero for shared memory parallelism
        initializeHabanero();
        // Set up MPI
        try {
            MPI.Init(args);
            mpiWorld = MPI.COMM_WORLD;
            mpiRank = mpiWorld.getRank();
            mpiSize = mpiWorld.getSize();
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
        int div = numVec / mpiSize;
        int rem = numVec % mpiSize;
        myNumVec = mpiRank < rem ? div+1 : div;
        globalVecStartIdx = mpiRank*div + (mpiRank < rem ? mpiRank : rem);
    }
}
