package com.ace.mapper;

import java.util.List;
import java.util.Map;

import com.ace.pojo.Competition;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CompetitionMapper {

    @Insert("INSERT INTO Competition (comp_name,admin_id) VALUES (#{compName},#{administrator.adminId})")
    @Options(useGeneratedKeys = true, keyProperty = "compId")
    void insertCompetition(Competition competition);


    
    @Select("SELECT competition.comp_id from competition WHERE comp_name = #{competitionName}")
    int getIdByName(String competitionName);
    
    @Select("SELECT c.comp_name, s.section_code, s.section_name, s.draw_code, t.team_code, cl.club_name, t.team_colour, t.outside_court, t.fixture_number " +
            "FROM Competition c " +
            "JOIN Section s ON c.comp_id = s.competition_id " +
            "JOIN Team t ON t.section_id = s.section_id " +
            "JOIN Club cl ON cl.club_id = t.club_id " +
            "WHERE c.comp_id = #{competitionId}")
    List<Map<String, Object>> getCompetitionData(int competitionId);

    @Select("SELECT * FROM Competition")
    List<Competition> getAllCompetitions();


    @Select("SELECT comp_name FROM Competition WHERE comp_id = #{competitionId}")
    String getCompetitionName(int competitionId);
}
