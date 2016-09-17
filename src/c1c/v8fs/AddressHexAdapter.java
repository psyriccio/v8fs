/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author psyriccio
 */
public class AddressHexAdapter extends XmlAdapter<String, Long> {

    @Override
    public Long unmarshal(String v) throws Exception {
        return Long.parseUnsignedLong(v, 16);
    }

    @Override
    public String marshal(Long v) throws Exception {
        return Main.to8Digits(Long.toHexString(v));
    }

}
