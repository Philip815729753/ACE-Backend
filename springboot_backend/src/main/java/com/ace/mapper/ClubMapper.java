package com.ace.mapper;

import com.ace.pojo.Club;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClubMapper {
    @Insert("INSERT INTO Club (club_name, competition_id) VALUES (#{clubName}, #{competition.compId})")
    @Options(useGeneratedKeys = true, keyProperty = "clubId")
    void insertClub(Club club);

    @Select("SELECT club_id FROM Club WHERE club_name = #{clubName} AND competition_id = #{competitionId} LIMIT 1;")
    Integer getClubIdByNameAndCompetition(String clubName, int competitionId);

    @Select("SELECT club_name FROM Club WHERE club_id = #{clubId}")
    String getClubNameById(int clubId);

    @Select("SELECT * FROM Club WHERE club_id = #{clubId}")
    Club getClubById(int clubId);

    @Select("SELECT c.* FROM Club c " +
            "JOIN Team t ON c.club_id = t.club_id " +
            "WHERE t.team_id = #{teamId}")
    Club getClubByTeamId(int teamId);

    @Select("SELECT * FROM Club WHERE competition_id = #{competitionId}")
    List<Club> getClubsByCompetitionId(int competitionId);
}