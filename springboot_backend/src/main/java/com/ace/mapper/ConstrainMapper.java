package com.ace.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ConstrainMapper {
    @Insert("INSERT INTO Constrain (constrain_type, team1_code, team2_code, competition_id,round) " +
            "VALUES (#{constrainType}, #{team1Code}, #{team2Code}, #{competitionId}, #{round})")
    void insertConstrain(int constrainType, int team1Code, Integer team2Code, int competitionId, Integer round);

    @Update("UPDATE constrain SET constrain_type = #{constrainType}, \n" +
            "        team1_code = #{team1Code}, " +
            "        team2_code = #{team2Code}, " +
            "        round = #{round}" +
            "    WHERE constrain.competition_id = #{competitionId}")
    void updateConstrain(int constrainType, int team1Code, Integer team2Code, int competitionId, Integer round);

    @Delete("DELETE FROM constrain " +
            "WHERE constrain_type = #{constrainType} " +
            "AND team1_code = #{team1Code} " +
            "AND (team2_code = #{team2Code} OR #{team2Code} IS NULL) " +
            "AND competition_id = #{competitionId} " +
            "AND (round = #{round} OR #{round} IS NULL) ")
    void removeConstrain(int constrainType, int team1Code, Integer team2Code, int competitionId, Integer round);
}
