package za.co.psybergate.chatterbox.adapter.out.delivery.model;

import lombok.Getter;

// TODO BlakeGoudemond 2026/02/03 | Should this be in domain? references tech
/**
 * Used in properties files to configure destination locations being delivered to.
 * */
public enum DeliveryMapping {

    MS_TEAMS("msTeams"), DISCORD("discord");

    @Getter
    private final String value;

    DeliveryMapping(String value) {
        this.value = value;
    }

}
