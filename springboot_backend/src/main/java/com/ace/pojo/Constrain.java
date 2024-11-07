package com.ace.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class Constrain {

    private int constrainId;

    private int constrainType;

    private int team1Code;

    private int team2Code;

    private Competition competition;
}
