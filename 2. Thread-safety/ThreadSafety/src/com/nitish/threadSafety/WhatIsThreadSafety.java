/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nitish.threadSafety;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple class to test "State less object" concept from
 * java-concurrency-in-practice book.<br/>
 * For Chapter-2.1 - What is thread safety.<br/>
 *
 * @author nitish
 */
public class WhatIsThreadSafety {

    public static void main(String[] args) {
        //A single stateless object is created.
        StateLessObject stateLessObj = new StateLessObject();
        //Same object is shared between 2 threads.
        StateLessObjectRunnable run1 = new StateLessObjectRunnable(stateLessObj);
        StateLessObjectRunnable run2 = new StateLessObjectRunnable(stateLessObj);
        Thread th1 = new Thread(run1, "Thread-1");
        Thread th2 = new Thread(run2, "Thread-2");
        /*
         * Both the threads use the same stateLessObj instance to validate the
         * hexadecimal addresses, but since stateLessObj is "Stateless", there
         * will be no thread issues.
         */
        th1.start();
        th2.start();
        //Wait until both the threads have finished execution.
        try {
            th1.join();
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        try {
            th2.join();
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

/**
 * This runnable class tries to validate a list of hexadecimal address. It uses
 * the "StateLessObject" for verifying it.
 *
 * @author nitish
 */
class StateLessObjectRunnable implements Runnable {

    private final StateLessObject stateLessObj;
    //This variable is created only once, & its contents are not modified.
    private final String[] values = {
        "0x1", "error", "x1", "0x12345678",
        "0x123456789", null
    };

    public StateLessObjectRunnable(StateLessObject stateLess) {
        stateLessObj = stateLess;
    }

    @Override
    public void run() {
        /**
         * Iterate over the list of address, to check if they are valid 32bit
         * address or not.
         */
        for (String addr : values) {
            try {
                if (stateLessObj.isIt32bitHexAddress(addr)) {
                    //Valid address.
                    System.out.println(Thread.currentThread().getName()
                            + ": \"" + addr + "\" is-a-valid 32bit address.");
                } else {
                    //Invalid address.
                    System.out.println(Thread.currentThread().getName()
                            + ": \"" + addr + "\" is-not-a-valid 32bit address.");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println(Thread.currentThread().getName()
                        + ": " + ex.getMessage());
            }
        }
    }

}

class StateLessObject {

    /**
     * This is the regex pattern for hexadecimal address. This variable is
     * created only once (i.e during class loading) and since its final it can
     * never be changed.
     */
    private static final Pattern regex = Pattern.compile(
            "(0x)?" //Group-1.
            + "([0-9A-Fa-f]{0,8})" //Group-2
    );

    /**
     * This method is completely stateless (of course it refers the instance
     * field, but thats ok since its a "static final" variable which is only
     * created once when this class is loaded by the JVM). It does its entire
     * operation inside the method stack (i.e inside the stack of the thread
     * which calls this method).
     *
     * @param hexaAddress
     * @return
     */
    public boolean isIt32bitHexAddress(String hexaAddress) {
        //Class Invariant - checking the validity of the given data
        if (hexaAddress == null || hexaAddress.isEmpty()) {
            throw new IllegalArgumentException("Invalid argument \"" + hexaAddress + "\"");
        }
        //This "matcher" variable created and destroyed within this method-stack.
        Matcher matcher = regex.matcher(hexaAddress);
        return matcher.matches();
    }

}
