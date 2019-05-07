package com.rainday.dto;

import com.rainday.gen.tables.pojos.Parampair;
import com.rainday.gen.tables.pojos.Relay;

import java.util.List;

/**
 * Created by wyd on 2019/4/23 16:58:52.
 */
public class RelayDto extends Relay {
    List<Parampair> parampairs;

    public List<Parampair> getParampairs() {
        return parampairs;
    }

    public void setParampairs(List<Parampair> parampairs) {
        this.parampairs = parampairs;
    }
}
