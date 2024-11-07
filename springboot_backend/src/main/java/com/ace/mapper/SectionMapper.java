package com.ace.mapper;

import com.ace.pojo.Section;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SectionMapper {
    @Select("SELECT * FROM Section WHERE section_id = #{sectionId}")
    Section getSectionById(int sectionId);

    @Insert("INSERT INTO Section (section_code, section_name, draw_code, competition_id) " +
            "VALUES (#{sectionCode}, #{sectionName}, #{drawCode}, #{competition.compId})")
    @Options(useGeneratedKeys = true, keyProperty = "sectionId")
    void insertSection(Section section);


    @Select("SELECT section_id FROM Section WHERE section_code = #{sectionCode} AND competition_id = #{competitionId}\n" +
            "LIMIT 1;")
    Integer getSectionIdByCodeAndCompetition(int sectionCode, int competitionId);


    @Select("SELECT * FROM Section WHERE competition_id = #{competitionId}")
    List<Section> getSectionsByCompetitionId(int competitionId);


    @Select("SELECT section_id, section_name FROM Section WHERE competition_id = #{competitionId}")
    List<Map<String, Object>> getAllSections(int competitionId);
}
