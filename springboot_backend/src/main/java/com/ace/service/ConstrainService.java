package com.ace.service;

import com.ace.mapper.CompetitionMapper;
import com.ace.mapper.ConstrainMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConstrainService {

    @Autowired
    private ConstrainMapper constrainMapper;
    @Autowired
    private CompetitionMapper competitionMapper;

    public void addConstrain(int constrainType, int team1Code, Integer team2Code, String competitionName,Integer round) {
        int competitionId = competitionMapper.getIdByName(competitionName);
        constrainMapper.insertConstrain(constrainType, team1Code, team2Code, competitionId,round);
    }

    public void editConstrain(int constrainType, int team1Code, Integer team2Code, String competitionName, Integer round) {
    int competitionId = competitionMapper.getIdByName(competitionName);
        constrainMapper.updateConstrain(constrainType, team1Code, team2Code, competitionId,round);
    }

    public void removeConstrain(int constrainType, int team1Code, Integer team2Code, String competitionName, Integer round) {
         int competitionId = competitionMapper.getIdByName(competitionName);
        constrainMapper.removeConstrain(constrainType, team1Code, team2Code, competitionId,round);
    }
    public int getConstrainType(String constrainName) {
        if(constrainName.equals("Home together")){
            return 1;
        }
        else if(constrainName.equals("Opposite")){
            return 2;
        }
        else return 3;
    }
}
