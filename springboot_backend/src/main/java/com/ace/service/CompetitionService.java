package com.ace.service;

import com.ace.mapper.CompetitionMapper;
import com.ace.pojo.Competition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetitionService {

    @Autowired
    private CompetitionMapper competitionMapper;

    public List<Competition> getAllCompetitions() {
        return competitionMapper.getAllCompetitions();
    }
}
