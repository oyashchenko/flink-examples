package com.oyshchenko;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

/**
 * Hello world!
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {

        Ignite ignite = Ignition.start("ignite-server.xml");

    }
}
