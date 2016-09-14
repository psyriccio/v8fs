/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import java.nio.ByteBuffer;

/**
 * Reading/Writing from/to buffer base feature interface
 *
 * @author psyriccio
 */
public interface Bufferable {

    public void writeToBuffer(ByteBuffer buffer);

    public void readFromBuffer(ByteBuffer buffer);

}
