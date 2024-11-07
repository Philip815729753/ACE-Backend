package com.ace.service;

import com.ace.mapper.*;
import com.ace.pojo.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class FileService {

    @Autowired
    private AdministratorMapper administratorMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private SectionMapper sectionMapper;

    @Autowired
    private ClubMapper clubMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private ConstrainMapper constrainMapper;

    // Method to upload and process the Excel file
    public void uploadFile(MultipartFile file) throws IOException {
        // Get the Competition obj from the file name
        Competition competition = createCompetitionFromFile(file);

        competitionMapper.insertCompetition(competition);
        int competitionId = competition.getCompId();

        // Read and process Excel file
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);  // Assuming the data is in the first worksheet

        // Iterate through each row and process it
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {  // Skip the header
                continue;
            }

            // Read and insert Section data
            int sectionCode = (int) row.getCell(1).getNumericCellValue();
            String sectionName = row.getCell(2).getStringCellValue();
            int drawCode = Integer.parseInt(row.getCell(3).getStringCellValue());

            // Check if the Section already exists
            Section section;
            Integer sectionId = sectionMapper.getSectionIdByCodeAndCompetition(sectionCode, competitionId);
            if (sectionId == null) {
                section = new Section();
                section.setSectionCode(sectionCode);
                section.setSectionName(sectionName);
                section.setDrawCode(drawCode);
                section.setCompetition(competition);
                section.setFixtureNums(null);

                sectionMapper.insertSection(section);
                sectionId = section.getSectionId();
            } else {
                section = new Section();
                section.setSectionId(sectionId);
            }

            // Read and insert Club data
            String clubName = row.getCell(5).getStringCellValue();
            Club club;
            Integer clubId = clubMapper.getClubIdByNameAndCompetition(clubName, competitionId);
            if (clubId == null) {
                club = new Club();
                club.setClubName(clubName);
                club.setCompetition(competition);
                club.setTeams(null);
                clubMapper.insertClub(club);
                clubId = club.getClubId();
            } else {
                club = new Club();
                club.setClubId(clubId);
            }

            // Read and insert Team data
            int teamCode = (int) row.getCell(4).getNumericCellValue();
            String teamColour = row.getCell(6).getStringCellValue();
            String outsideCourt = row.getCell(7).getStringCellValue();

            Team team = new Team();
            team.setTeamCode(teamCode);
            team.setTeamColour(teamColour);
            team.setOutsideCourt(outsideCourt);
            team.setClub(club);
            team.setSection(section);
            team.setFixtureNumber(-1);

            teamMapper.insertTeam(team);
        }

        // Close the workbook
        workbook.close();
    }


    public ByteArrayInputStream exportCompetitionDataToExcel(int competitionId) throws IOException {

        List<Map<String, Object>> competitionData = competitionMapper.getCompetitionData(competitionId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Competition Data");
            Row headerRow = sheet.createRow(0);

            // Create header
            String[] headers = {"comp_name", "section_code", "section_name", "draw_code", "team_code", "club_name", "team_colour", "outside_court", "fixture_number"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Create data rows
            int rowIdx = 1;
            for (Map<String, Object> data : competitionData) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(data.get("comp_name").toString());
                row.createCell(1).setCellValue((Integer) data.get("section_code"));
                row.createCell(2).setCellValue(data.get("section_name").toString());
                row.createCell(3).setCellValue((Integer) data.get("draw_code"));
                row.createCell(4).setCellValue((Integer) data.get("team_code"));
                row.createCell(5).setCellValue(data.get("club_name").toString());
                row.createCell(6).setCellValue(data.get("team_colour") != null ? data.get("team_colour").toString() : "");
                row.createCell(7).setCellValue(data.get("outside_court") != null ? data.get("outside_court").toString() : "");

                if (data.get("fixture_number") == null) {
                    System.out.println(data.get("club_name").toString().concat(data.get("team_colour") != null ? data.get("team_colour").toString() : "") + "'s fixture number is missing!");

                }
                row.createCell(8).setCellValue(data.get("fixture_number") != null ? data.get("fixture_number").toString() : "");

            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // Get the Competition obj from the file
    private Competition createCompetitionFromFile(MultipartFile file) {
        //get the competition name
        String compName = file.getOriginalFilename();
        if (compName != null && compName.contains(".")) {
            compName = compName.substring(0, compName.lastIndexOf('.'));  // Remove the extension
        }

        // Get the currently logged in administrator information from JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Administrator admin = administratorMapper.findByUsername(userDetails.getUsername());


        // Create a Competition object
        Competition competition = new Competition();
        competition.setCompName(compName);
        competition.setAdministrator(admin);

        return competition;
    }


    public byte[] generateDetailedExcelReport(int competitionId) throws IOException {
    // Create a new Excel workbook
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Competition");

    // Set the title
    String competitionName = competitionMapper.getCompetitionName(competitionId);
    Row titleRow = sheet.createRow(0);
    Cell titleCell = titleRow.createCell(0);
    titleCell.setCellValue(competitionName);
    titleCell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);

    // Get all sections and process them
    List<Map<String, Object>> sections = sectionMapper.getAllSections(competitionId);
    int sectionIndex = 0;
    int rowIndex = 2;

    while (sectionIndex < sections.size()) {
        // Create header rows for up to 3 sections per row
        Row headerRow = sheet.createRow(rowIndex);
        int maxTeamRows = 0;

        for (int colIndex = 0; colIndex < 3 && sectionIndex < sections.size(); colIndex++, sectionIndex++) {
            Map<String, Object> section = sections.get(sectionIndex);
            Cell sectionHeaderCell = headerRow.createCell(colIndex * 3);
            sectionHeaderCell.setCellValue(section.get("section_name").toString());

            // Fetch the teams for the current section
            List<Map<String, Object>> teams = teamMapper.getTeamsBySection((int) section.get("section_id"));

            // Sort teams by their fixture_number
            teams.sort((team1, team2) -> {
                Integer fixture1 = (Integer) team1.get("fixture_number");
                Integer fixture2 = (Integer) team2.get("fixture_number");
                if (fixture1 == null) return 1;  // Place teams with null fixture_number at the end
                if (fixture2 == null) return -1;
                return fixture1.compareTo(fixture2);
            });

            // Write the sorted teams starting from rowIndex + 1
            for (int i = 0; i < teams.size(); i++) {
                Row teamRow;
                if (rowIndex + 1 + i > sheet.getLastRowNum()) {
                    teamRow = sheet.createRow(rowIndex + 1 + i);
                } else {
                    teamRow = sheet.getRow(rowIndex + 1 + i);
                }

                // Safely handle fixture_number, if it's null, leave the cell empty
                Object fixtureNumber = teams.get(i).get("fixture_number");
                if (fixtureNumber != null) {
                    teamRow.createCell(colIndex * 3).setCellValue(fixtureNumber.toString());  // Fixture number goes in the first column of each section
                } else {
                    teamRow.createCell(colIndex * 3);  // Leave empty if fixture number is null
                }

                // Safely handle club_name and team_colour
                String clubNameWithColour = teams.get(i).get("club_name") != null ? teams.get(i).get("club_name").toString() : "Unknown Club";
                Object teamColour = teams.get(i).get("team_colour");
                if (teamColour != null && !teamColour.toString().isEmpty()) {
                    clubNameWithColour += " " + teamColour;
                }

                // Set the club name and colour in the Excel cell (next to fixture number)
                teamRow.createCell(colIndex * 3 + 1).setCellValue(clubNameWithColour);
            }

            maxTeamRows = Math.max(maxTeamRows, teams.size());
        }

        // Move the row index below the longest team list
        rowIndex += maxTeamRows + 2;  // Leave a gap of 2 rows before the next set of sections
    }

    // Write the workbook to a byte array output stream
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    workbook.close();

    return out.toByteArray();
}

}
