package com.ace.mapper;

import com.ace.pojo.Team;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface TeamMapper {
    @Insert("INSERT INTO Team (team_code, team_colour, outside_court, section_id, club_id) " +
            "VALUES (#{teamCode}, #{teamColour}, #{outsideCourt}, #{section.sectionId}, #{club.clubId})")
    @Options(useGeneratedKeys = true, keyProperty = "teamId")
    void insertTeam(Team team);

    @Select("SELECT t.*, s.section_id AS sectionId, s.section_code AS sectionCode, s.section_name AS sectionName, s.draw_code AS drawCode " +
            "FROM Team t " +
            "JOIN Section s ON t.section_id = s.section_id " +
            "WHERE t.section_id = #{sectionId}")
    @Results({
            @Result(property = "section.sectionId", column = "sectionId"),
            @Result(property = "section.sectionCode", column = "sectionCode"),
            @Result(property = "section.sectionName", column = "sectionName"),
            @Result(property = "section.drawCode", column = "drawCode")
    })
    List<Team> getTeamsBySectionId(int sectionId);

    @Update("UPDATE Team SET fixture_number = #{fixtureNumber} WHERE team_id = #{teamId}")
    int updateTeam(Team team);

    @Select("SELECT t.*, s.section_id AS sectionId, s.section_code AS sectionCode, s.section_name AS sectionName, s.draw_code AS drawCode " +
            "FROM Team t " +
            "JOIN Section s ON t.section_id = s.section_id " +
            "WHERE t.club_id = #{clubId}")
    @Results({
            @Result(property = "section.sectionId", column = "sectionId"),
            @Result(property = "section.sectionCode", column = "sectionCode"),
            @Result(property = "section.sectionName", column = "sectionName"),
            @Result(property = "section.drawCode", column = "drawCode")
    })
    ArrayList<Team> getTeamsByClubId(int clubId);

    @Select("SELECT t.team_code, t.fixture_number, c.club_name, t.team_colour, s.section_name, comp.comp_name " +
            "FROM Team t " +
            "INNER JOIN Club c ON t.club_id = c.club_id " +
            "INNER JOIN Section s ON t.section_id = s.section_id " +
            "INNER JOIN Competition comp ON s.competition_id = comp.comp_id " +
            "WHERE s.section_id = #{sectionId} " +
            "ORDER BY t.fixture_number ASC")
    List<Map<String, Object>> getTeamsBySection(@Param("sectionId") int sectionId);
}
