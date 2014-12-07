package org.saliya.dsctools.whitendata;


import static edu.rice.hj.Module0.initializeHabanero;

public class ParallelOptions {
    public void setupParallelism(String [] arg){
        // Initialize Habanero for shared memory parallelism
        initializeHabanero();

        // Set up MPI
        MPI.Init
    }
}
