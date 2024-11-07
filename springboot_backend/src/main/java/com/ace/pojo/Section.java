package com.ace.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Section {

    private int sectionId;

    private int sectionCode;

    private String sectionName;

    private int drawCode;

    private Competition competition;

    private int[] fixtureNums;
}
