//package com.ace.service;
//
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
//    private static final int[] FIXTURE_PAIRS = {1, 2, 3, 4, 5, 6, 7, 8};
//    private static final int MAX_ITERATIONS = 10000;
//    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
//
//    @Autowired
//    private TeamMapper teamMapper;
//    @Autowired
//    private SectionMapper sectionMapper;
//    @Autowired
//    private ClubMapper clubMapper;
//
//    private Map<Integer, Team> globalTeams = new HashMap<>();
//    private Map<Integer, Section> sections = new HashMap<>();
//    private Map<Integer, List<Team>> clubTeams = new HashMap<>();
//
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
//        clubTeams.clear();
//        for (Team team : globalTeams.values()) {
//            clubTeams.computeIfAbsent(team.getClub().getClubId(), k -> new ArrayList<>()).add(team);
//        }
//        logger.info("Finished reloading {} teams from database", globalTeams.size());
//    }
//
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
//                return currentResult;
//            }
//        }
//
//        return bestResult;
//    }
//
//    private ScheduleResult generateRandomSchedule() {
//        ScheduleResult result = new ScheduleResult();
//        Map<Integer, List<Integer>> availableFixturesBySectionId = new HashMap<>();
//
//        for (Section section : sections.values()) {
//            int teamCount = (int) globalTeams.values().stream()
//                    .filter(t -> t.getSection().getSectionId() == section.getSectionId())
//                    .count();
//            List<Integer> fixtureNumbers = IntStream.rangeClosed(1, teamCount)
//                    .boxed().collect(Collectors.toList());
//            Collections.shuffle(fixtureNumbers);
//            availableFixturesBySectionId.put(section.getSectionId(), fixtureNumbers);
//        }
//
//        List<List<Team>> sortedClubs = new ArrayList<>(clubTeams.values());
//        sortedClubs.sort((a, b) -> b.size() - a.size());
//
//        for (List<Team> club : sortedClubs) {
//            allocateClub(club, result, availableFixturesBySectionId);
//        }
//
//        return result;
//    }
//
//    private void allocateClub(List<Team> club, ScheduleResult result, Map<Integer, List<Integer>> availableFixturesBySectionId) {
//        for (Team team : club) {
//            int sectionId = team.getSection().getSectionId();
//            List<Integer> availableFixtures = availableFixturesBySectionId.get(sectionId);
//
//            if (availableFixtures.isEmpty()) {
//                throw new RuntimeException("No available fixtures for section " + sectionId);
//            }
//
//            int fixtureNumber = availableFixtures.remove(0);
//            result.addAllocation(team, fixtureNumber);
//        }
//    }
//
//    private int evaluateSchedule(ScheduleResult result) {
//        int score = 0;
//        score += evaluateSectionUniqueness(result) * 1000;
//        score += evaluateFixturePairs(result) * 100;
//        score += evaluateClubBalance(result) * 10;
//        return score;
//    }
//
//    private int evaluateSectionUniqueness(ScheduleResult result) {
//        return sections.values().stream()
//                .mapToInt(section -> (int) globalTeams.values().stream()
//                        .filter(team -> team.getSection().equals(section))
//                        .map(result::getFixtureNumber)
//                        .distinct()
//                        .count())
//                .sum();
//    }
//
//    private int evaluateFixturePairs(ScheduleResult result) {
//        int correctPairs = 0;
//        for (int i = 0; i < FIXTURE_PAIRS.length; i += 2) {
//            int fixture1 = FIXTURE_PAIRS[i];
//            int fixture2 = FIXTURE_PAIRS[i + 1];
//            boolean hasFixture1 = globalTeams.values().stream().anyMatch(team -> result.getFixtureNumber(team) == fixture1);
//            boolean hasFixture2 = globalTeams.values().stream().anyMatch(team -> result.getFixtureNumber(team) == fixture2);
//            if (hasFixture1 && hasFixture2) correctPairs++;
//        }
//        return correctPairs;
//    }
//
//    private int evaluateClubBalance(ScheduleResult result) {
//        return (int) clubTeams.values().stream()
//                .filter(club -> isBalanced(club, result))
//                .count();
//    }
//
//    private boolean isScheduleValid(ScheduleResult result) {
//        for (Section section : sections.values()) {
//            Set<Integer> usedFixtures = new HashSet<>();
//            int teamCount = 0;
//            for (Team team : globalTeams.values()) {
//                if (team.getSection().equals(section)) {
//                    int fixtureNumber = result.getFixtureNumber(team);
//                    if (fixtureNumber < 1 || fixtureNumber > 8 || !usedFixtures.add(fixtureNumber)) {
//                        return false;
//                    }
//                    teamCount++;
//                }
//            }
//            if (teamCount != usedFixtures.size() || (teamCount != 6 && teamCount != 8)) {
//                return false;
//            }
//        }
//
//        for (int i = 0; i < FIXTURE_PAIRS.length; i += 2) {
//            int fixture1 = FIXTURE_PAIRS[i];
//            int fixture2 = FIXTURE_PAIRS[i + 1];
//            boolean hasFixture1 = globalTeams.values().stream().anyMatch(team -> result.getFixtureNumber(team) == fixture1);
//            boolean hasFixture2 = globalTeams.values().stream().anyMatch(team -> result.getFixtureNumber(team) == fixture2);
//            if (hasFixture1 != hasFixture2) return false;
//        }
//
//        return true;
//    }
//
//    private boolean isBalanced(List<Team> clubTeams, ScheduleResult result) {
//        Map<Integer, Integer> pairCounts = clubTeams.stream()
//                .collect(Collectors.groupingBy(
//                        team -> (result.getFixtureNumber(team) - 1) / 2,
//                        Collectors.summingInt(e -> 1)
//                ));
//
//        int maxCount = Collections.max(pairCounts.values());
//        return maxCount <= 2 || (maxCount == 3 && Collections.frequency(pairCounts.values(), 3) == 1);
//    }
//
//    private void applyScheduleResult(ScheduleResult result) {
//        result.getAllocations().forEach((team, fixtureNumber) -> team.setFixtureNumber(fixtureNumber));
//    }
//
//    private void saveResults() {
//        globalTeams.values().forEach(team -> {
//            try {
//                teamMapper.updateTeam(team);
//                System.out.println("Updated team " + team.getTeamId() + " with fixture number " + team.getFixtureNumber());
//            } catch (Exception e) {
//                System.err.println("Error updating team " + team.getTeamId() + ": " + e.getMessage());
//            }
//        });
//    }
//
//    private void reportUnbalancedClubs() {
//        List<String> unbalancedClubs = clubTeams.entrySet().stream()
//                .filter(entry -> !isBalanced(entry.getValue()))
//                .map(entry -> entry.getValue().get(0).getClub().getClubName())
//                .collect(Collectors.toList());
//
//        if (!unbalancedClubs.isEmpty()) {
//            System.out.println("The following clubs have significantly unbalanced fixture pair allocations:");
//            for (String clubName : unbalancedClubs) {
//                System.out.println("- " + clubName);
//                List<Team> teams = clubTeams.values().stream()
//                        .flatMap(List::stream)
//                        .filter(team -> team.getClub().getClubName().equals(clubName))
//                        .collect(Collectors.toList());
//
//                Map<Integer, List<Integer>> pairAllocations = teams.stream()
//                        .collect(Collectors.groupingBy(
//                                team -> (team.getFixtureNumber() - 1) / 2,
//                                Collectors.mapping(Team::getFixtureNumber, Collectors.toList())
//                        ));
//
//                pairAllocations.forEach((pairIndex, fixtures) ->
//                        System.out.println("  Pair " + (pairIndex + 1) + ": " + fixtures)
//                );
//            }
//        } else {
//            System.out.println("All clubs have reasonably balanced fixture pair allocations.");
//        }
//    }
//
//    private boolean isBalanced(List<Team> clubTeams) {
//        Map<Integer, Integer> pairCounts = clubTeams.stream()
//                .collect(Collectors.groupingBy(
//                        team -> (team.getFixtureNumber() - 1) / 2,
//                        Collectors.summingInt(e -> 1)
//                ));
//
//        int tripleCount = (int) pairCounts.values().stream().filter(count -> count == 3).count();
//        return pairCounts.values().stream().allMatch(count -> count <= 3) && tripleCount <= 1;
//    }
//
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