package com.rainday.dto;

import com.rainday.gen.tables.pojos.Application;

import java.util.List;

/**
 * Created by wyd on 2019/4/23 16:56:43.
 */
public class ApplicationDto extends Application {
    List<RelayDto> relays;

    public List<RelayDto> getRelays() {
        return relays;
    }

    public void setRelays(List<RelayDto> relays) {
        this.relays = relays;
    }
}
