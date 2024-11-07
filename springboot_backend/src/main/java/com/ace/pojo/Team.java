package com.ace.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Team {

    private int teamId;

    private int teamCode;

    private String teamColour;

    private int fixtureNumber;

    private String outsideCourt;

    private Section section;

    private Club club;

}
