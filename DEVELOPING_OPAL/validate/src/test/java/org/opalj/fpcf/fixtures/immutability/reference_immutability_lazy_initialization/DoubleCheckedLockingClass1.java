package org.opalj.fpcf.fixtures.immutability.reference_immutability_lazy_initialization;//source: https://javarevisited.blogspot.com/2014/05/double-checked-locking-on-singleton-in-java.html


import org.opalj.fpcf.properties.reference_immutability.LazyInitializedThreadSafeReferenceAnnotation;

public class DoubleCheckedLockingClass1{

    @LazyInitializedThreadSafeReferenceAnnotation("")
    private DoubleCheckedLockingClass1 instance;
    public DoubleCheckedLockingClass1 getInstance() {
        if(instance==null){
            synchronized(this) {
                if(instance==null){
                    instance = new DoubleCheckedLockingClass1();
                }
            }
        }
        return instance;
    }
}


