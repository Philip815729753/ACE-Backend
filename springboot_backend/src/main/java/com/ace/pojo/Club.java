package com.ace.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Club {

    private int clubId;

    private String clubName;

    private Competition competition;

    private ArrayList<Team> teams;
}
