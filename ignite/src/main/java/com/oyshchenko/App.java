package com.oyshchenko;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

import java.util.Arrays;

/**
 * Hello world!
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {

        Ignite ignite = Ignition.start("ignite-server.xml");
        ignite.destroyCaches(Arrays.asList("Position", "Price", "Portfolio"));

    }
}
