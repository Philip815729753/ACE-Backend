//package com.ace.service;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import com.ace.pojo.*;
//import com.ace.mapper.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//@Service
//public class ScheduleService {
//    // Constants
//    private static final int[] FIXTURE_PAIRS = {1, 2, 3, 4, 5, 6, 7, 8};
//    private static final int MAX_ITERATIONS = 10000;
//    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
//    // Dependency Injections
//    @Autowired
//    private TeamMapper teamMapper;
//    @Autowired
//    private SectionMapper sectionMapper;
//    @Autowired
//    private ClubMapper clubMapper;
//
//    // Data structures
//    private Map<Integer, Team> globalTeams = new HashMap<>();
//    private Map<Integer, Section> sections = new HashMap<>();
//    private Map<Integer, List<Team>> clubTeams = new HashMap<>();
//
//    // Main public method
//    public void scheduleFixtures(int competitionId) {
//        loadDataFromDatabase(competitionId);
//        ScheduleResult bestResult = findOptimalSchedule();
//        if (bestResult != null) {
//            System.out.println("Found the best possible schedule.");
//            applyScheduleResult(bestResult);
//            saveResults();
//            System.out.println("reload");
//            reloadTeamsFromDatabase();
//            System.out.println("reload finish & report");
//            reportUnbalancedClubs();
//            System.out.println("report");
//        } else {
//            System.out.println("Failed to find a valid schedule.");
//        }
//    }
//
//    // Data loading and reloading methods
//    private void loadDataFromDatabase(int competitionId) {
//        List<Section> sectionList = sectionMapper.getSectionsByCompetitionId(competitionId);
//        for (Section section : sectionList) {
//            sections.put(section.getSectionId(), section);
//            List<Team> teams = teamMapper.getTeamsBySectionId(section.getSectionId());
//            for (Team team : teams) {
//                team.setSection(section);
//                if (team.getClub() == null || team.getClub().getClubId() == 0) {
//                    Club club = clubMapper.getClubByTeamId(team.getTeamId());
//                    if (club == null) {
//                        throw new RuntimeException("Club not found for team " + team.getTeamId());
//                    }
//                    team.setClub(club);
//                }
//                globalTeams.put(team.getTeamId(), team);
//                clubTeams.computeIfAbsent(team.getClub().getClubId(), k -> new ArrayList<>()).add(team);
//            }
//        }
//        System.out.println("Number of teams loaded: " + globalTeams.size());
//    }
//
//    private void reloadTeamsFromDatabase() {
//        logger.info("Starting to reload teams from database");
//        Map<Integer, Team> updatedTeams = new HashMap<>();
//        for (int teamId : globalTeams.keySet()) {
//            Team updatedTeam = teamMapper.getTeamById(teamId);
//            if (updatedTeam != null) {
//                updatedTeams.put(teamId, updatedTeam);
//                logger.debug("Reloaded team {}: Fixture number {}", teamId, updatedTeam.getFixtureNumber());
//            } else {
//                logger.warn("Failed to reload team {}", teamId);
//            }
//        }
//        globalTeams = updatedTeams;
//
//        // 更新 clubTeams
//        clubTeams.clear();
//        for (Team team : globalTeams.values()) {
//            clubTeams.computeIfAbsent(team.getClub().getClubId(), k -> new ArrayList<>()).add(team);
//        }
//        logger.info("Finished reloading {} teams from database", globalTeams.size());
//    }
//
//    // Schedule generation and optimization methods
//    private ScheduleResult findOptimalSchedule() {
//        ScheduleResult bestResult = new ScheduleResult();
//        int bestScore = Integer.MIN_VALUE;
//
//        for (int i = 0; i < MAX_ITERATIONS; i++) {
//            ScheduleResult currentResult = generateRandomSchedule();
//            int currentScore = evaluateSchedule(currentResult);
//
//            if (currentScore > bestScore) {
//                bestScore = currentScore;
//                bestResult = currentResult;
//            }
//
//            if (isScheduleValid(currentResult)) {
//                return currentResult; // Found a valid schedule
//            }
//        }
//
//        return bestResult; // Return the best schedule found, even if not fully valid
//    }
//
//    private ScheduleResult generateRandomSchedule() {
//        ScheduleResult result = new ScheduleResult();
//        List<List<Team>> sortedClubs = new ArrayList<>(clubTeams.values());
//        sortedClubs.sort((a, b) -> b.size() - a.size()); // Sort clubs by team count (descending)
//
//        for (List<Team> club : sortedClubs) {
//            allocateClub(club, result);
//        }
//
//        return result;
//    }
//
//    private void allocateClub(List<Team> club, ScheduleResult result) {
//        List<Integer> availableFixtures = new ArrayList<>(Arrays.stream(FIXTURE_PAIRS).boxed().collect(Collectors.toList()));
//        Collections.shuffle(availableFixtures);
//
//        int homeCount = 0;
//        int awayCount = 0;
//
//        for (Team team : club) {
//            if (availableFixtures.isEmpty()) {
//                availableFixtures = new ArrayList<>(Arrays.stream(FIXTURE_PAIRS).boxed().collect(Collectors.toList()));
//                Collections.shuffle(availableFixtures);
//            }
//
//            int fixtureNumber = availableFixtures.remove(0);
//
//            // Try to balance home and away
//            if (club.size() % 2 == 0) {
//                if (homeCount < club.size() / 2 && fixtureNumber % 2 != 0) {
//                    homeCount++;
//                } else if (awayCount < club.size() / 2 && fixtureNumber % 2 == 0) {
//                    awayCount++;
//                } else {
//                    // Swap if needed
//                    fixtureNumber = (fixtureNumber % 2 == 0) ? fixtureNumber - 1 : fixtureNumber + 1;
//                }
//            }
//
//            // Ensure unique fixture number within section
//            while (result.isFixtureNumberUsedInSection(team.getSection(), fixtureNumber)) {
//                fixtureNumber = availableFixtures.isEmpty() ?
//                        FIXTURE_PAIRS[new Random().nextInt(FIXTURE_PAIRS.length)] :
//                        availableFixtures.remove(0);
//            }
//
//            result.addAllocation(team, fixtureNumber);
//        }
//    }
//
//    // Schedule evaluation methods
//    private int evaluateSchedule(ScheduleResult result) {
//        int score = 0;
//        score += evaluateSectionUniqueness(result) * 1000; // High priority
//        score += evaluateClubBalance(result) * 100;
//        score += evaluateFixturePairs(result) * 10;
//        return score;
//    }
//
//    private int evaluateSectionUniqueness(ScheduleResult result) {
//        int uniqueCount = 0;
//        for (Section section : sections.values()) {
//            Set<Integer> usedFixtures = new HashSet<>();
//            for (Team team : globalTeams.values()) {
//                if (team.getSection().equals(section)) {
//                    usedFixtures.add(result.getFixtureNumber(team));
//                }
//            }
//            uniqueCount += usedFixtures.size();
//        }
//        return uniqueCount;
//    }
//
//    private int evaluateClubBalance(ScheduleResult result) {
//        int balancedClubs = 0;
//        for (List<Team> club : clubTeams.values()) {
//            if (isBalanced(club, result)) {
//                balancedClubs++;
//            }
//        }
//        return balancedClubs;
//    }
//
//    private int evaluateFixturePairs(ScheduleResult result) {
//        int correctPairs = 0;
//        for (int i = 0; i < FIXTURE_PAIRS.length; i += 2) {
//            int home = FIXTURE_PAIRS[i];
//            int away = FIXTURE_PAIRS[i + 1];
//            for (Team team : globalTeams.values()) {
//                int fixture = result.getFixtureNumber(team);
//                if (fixture == home || fixture == away) {
//                    correctPairs++;
//                }
//            }
//        }
//        return correctPairs;
//    }
//
//    private boolean isScheduleValid(ScheduleResult result) {
//        // Check section uniqueness
//        for (Section section : sections.values()) {
//            Set<Integer> usedFixtures = new HashSet<>();
//            for (Team team : globalTeams.values()) {
//                if (team.getSection().equals(section)) {
//                    if (!usedFixtures.add(result.getFixtureNumber(team))) {
//                        return false; // Duplicate fixture in section
//                    }
//                }
//            }
//        }
//
//        // Check club balance
//        for (List<Team> club : clubTeams.values()) {
//            if (!isBalanced(club, result)) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    private boolean isBalanced(List<Team> clubTeams, ScheduleResult result) {
//        int homeCount = 0;
//        int totalCount = clubTeams.size();
//        for (Team team : clubTeams) {
//            int fixtureNumber = result.getFixtureNumber(team);
//            if (fixtureNumber % 2 != 0) {
//                homeCount++;
//            }
//        }
//        int awayCount = totalCount - homeCount;
//        return Math.abs(homeCount - awayCount) <= 1;
//    }
//
//    // Result application and saving methods
//    private void applyScheduleResult(ScheduleResult result) {
//        for (Map.Entry<Team, Integer> entry : result.getAllocations().entrySet()) {
//            entry.getKey().setFixtureNumber(entry.getValue());
//        }
//    }
//
//    private void saveResults() {
//        for (Team team : globalTeams.values()) {
//            try {
//                teamMapper.updateTeam(team);
//                System.out.println("Updated team " + team.getTeamId() + " with fixture number " + team.getFixtureNumber());
//            } catch (Exception e) {
//                System.err.println("Error updating team " + team.getTeamId() + ": " + e.getMessage());
//            }
//        }
//    }
//
//    // Reporting method
//    private void reportUnbalancedClubs() {
//        List<String> unbalancedClubs = new ArrayList<>();
//        for (Map.Entry<Integer, List<Team>> entry : clubTeams.entrySet()) {
//            List<Team> clubTeamList = entry.getValue();
//            if (!isBalanced(clubTeamList)) {
//                String clubName = clubTeamList.get(0).getClub().getClubName();
//                unbalancedClubs.add(clubName);
//            }
//        }
//
//        if (!unbalancedClubs.isEmpty()) {
//            System.out.println("The following clubs do not have balanced home/away allocations:");
//            for (String clubName : unbalancedClubs) {
//                System.out.println("- " + clubName);
//                List<Team> teams = clubTeams.entrySet().stream()
//                        .filter(e -> e.getValue().get(0).getClub().getClubName().equals(clubName))
//                        .findFirst()
//                        .map(Map.Entry::getValue)
//                        .orElse(Collections.emptyList());
////                for (Team team : teams) {
////                    System.out.println("  Team " + team.getTeamId() + ": Fixture " + team.getFixtureNumber());
////                }
//            }
//        } else {
//            System.out.println("All clubs have balanced home/away allocations.");
//        }
//    }
//
//    // Helper method for final balance check
//    private boolean isBalanced(List<Team> clubTeams) {
//        int homeCount = 0;
//        int totalCount = clubTeams.size();
//        for (Team team : clubTeams) {
//            int fixtureNumber = team.getFixtureNumber();
//            if (fixtureNumber % 2 != 0) {
//                homeCount++;
//            }
//        }
//        int awayCount = totalCount - homeCount;
//        return Math.abs(homeCount - awayCount) <= 1;
//    }
//
//    // Inner class for schedule result
//    private class ScheduleResult {
//        private Map<Team, Integer> allocations = new HashMap<>();
//        private Map<Integer, Set<Integer>> sectionAllocations = new HashMap<>();
//
//        public void addAllocation(Team team, int fixtureNumber) {
//            allocations.put(team, fixtureNumber);
//            sectionAllocations.computeIfAbsent(team.getSection().getSectionId(), k -> new HashSet<>()).add(fixtureNumber);
//        }
//
//        public Map<Team, Integer> getAllocations() {
//            return allocations;
//        }
//
//        public int getFixtureNumber(Team team) {
//            return allocations.getOrDefault(team, -1);
//        }
//
//        public boolean isFixtureNumberUsedInSection(Section section, int fixtureNumber) {
//            return sectionAllocations.getOrDefault(section.getSectionId(), new HashSet<>()).contains(fixtureNumber);
//        }
//    }
//}