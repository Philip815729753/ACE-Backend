package com.ace.service;

import com.ace.mapper.ClubMapper;
import com.ace.mapper.SectionMapper;
import com.ace.mapper.TeamMapper;
import com.ace.pojo.Club;
import com.ace.pojo.Section;
import com.ace.pojo.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScheduleService {
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private SectionMapper sectionMapper;
    @Autowired
    private ClubMapper clubMapper;

    // Global list for initial floaters (e.g., 'Bye' clubs, clubs with one team)
    private List<Team> globalFloaters = new ArrayList<>();

    public String scheduleFixture(int competitionId) {
        // Load data from the database.
        List<Section> sectionList = sectionMapper.getSectionsByCompetitionId(competitionId);
        List<Club> clubList = clubMapper.getClubsByCompetitionId(competitionId);
        List<Team> teamList = new ArrayList<>();
        for (Section section : sectionList) {
            teamList.addAll(teamMapper.getTeamsBySectionId(section.getSectionId()));
        }
        System.out.println("loaded team number: " + teamList.size()); // Check loaded team size.
        for (Club club : clubList) {
            ArrayList<Team> teamsForClub = teamMapper.getTeamsByClubId(club.getClubId());
            club.setTeams(teamsForClub);
            // Set the club reference in each team
            for (Team team : teamsForClub) {
                team.setClub(club);
            }
        }

        // If a team has an outside court, move the team from the original club to the club corresponding to the outside court.
        Map<String, Club> clubNameToClub = new HashMap<>();
        for (Club club : clubList) {
            clubNameToClub.put(club.getClubName(), club);
        }
        for (Club club : clubList) {
            Iterator<Team> teamIterator = club.getTeams().iterator();
            while (teamIterator.hasNext()) {
                Team team = teamIterator.next();
                if (team.getOutsideCourt() != null && !team.getOutsideCourt().isEmpty()) {
                    Club targetClub = clubNameToClub.get(team.getOutsideCourt());
                    if (targetClub != null) {
                        teamIterator.remove();
                        targetClub.getTeams().add(team);
                        team.setClub(targetClub); // Update the club reference in the team
                    }
                }
            }
        }

        // Remove clubs that don't need to be considered in default rules, including 'Bye' club and clubs that only have one team.
        Iterator<Club> clubIterator = clubList.iterator();
        while (clubIterator.hasNext()) {
            Club club = clubIterator.next();
            if (club.getClubName().equalsIgnoreCase("Bye")) {
                globalFloaters.addAll(club.getTeams());
                clubIterator.remove();
            } else if (club.getTeams().size() == 1) {
                globalFloaters.addAll(club.getTeams());
                clubIterator.remove();
            }
        }

        // Initialize used fixture numbers map.
        Map<Integer, Set<Integer>> usedFixtureNumbersBySection = new HashMap<>();
        // Do not clear globalFloaters here, as it contains initial floaters

        // Start the recursive scheduling.
        List<Team> schedulingFloaters = new ArrayList<>();
        boolean success = scheduleClubFixtures(clubList, 0, usedFixtureNumbersBySection, schedulingFloaters);

        // Combine initial floaters with floaters found during scheduling
        List<Team> allFloaters = new ArrayList<>(globalFloaters);
        allFloaters.addAll(schedulingFloaters);

        // Handle the floaters.
        handleFloaters(allFloaters, usedFixtureNumbersBySection);

        if (success) {
            // Update the fixture numbers in the database.
            updateFixtureNumbers(clubList, allFloaters);

            printFixtureResultsByClub(clubList);
            // Print floaters
            printFloaters(allFloaters);
            return "Fixture scheduled successfully.";
        } else {
            printFixtureResultsByClub(clubList);
            return "Failed to schedule fixtures.";
        }
    }

    private boolean scheduleClubFixtures(List<Club> clubs, int clubIndex, Map<Integer, Set<Integer>> usedFixtureNumbersBySection, List<Team> schedulingFloaters) {
        // Base case: All clubs have been processed.
        if (clubIndex >= clubs.size()) {
            return true;
        }
        Club club = clubs.get(clubIndex);
        List<Team> teams = club.getTeams();

        // Generate all possible groupings for the current club.
        List<List<List<Team>>> allGroupings = generateAllGroupings(teams);

        // Try each grouping.
        for (List<List<Team>> grouping : allGroupings) {
            // Create copies for backtracking
            Map<Integer, Set<Integer>> usedFixtureNumbersCopy = deepCopyUsedFixtureNumbers(usedFixtureNumbersBySection);
            List<Team> floatersInThisPath = new ArrayList<>();

            // Attempt to assign pairing numbers to this grouping.
            if (assignPairingNumbersToGrouping(grouping, usedFixtureNumbersCopy, floatersInThisPath)) {
                // Proceed to the next club recursively.
                if (scheduleClubFixtures(clubs, clubIndex + 1, usedFixtureNumbersCopy, schedulingFloaters)) {
                    // Update the original map with successful assignments
                    usedFixtureNumbersBySection.clear();
                    usedFixtureNumbersBySection.putAll(usedFixtureNumbersCopy);
                    // Add floaters from this path to the main scheduling floaters list
                    schedulingFloaters.addAll(floatersInThisPath);
                    return true;
                }
                // Backtrack: assignments are discarded due to the copies
            }
        }
        // No valid grouping found for this club.
        return false;
    }

    private Map<Integer, Set<Integer>> deepCopyUsedFixtureNumbers(Map<Integer, Set<Integer>> original) {
        Map<Integer, Set<Integer>> copy = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    // Generate all possible groupings of teams in a club, considering floaters.
    private List<List<List<Team>>> generateAllGroupings(List<Team> teams) {
        List<List<List<Team>>> allGroupings = new ArrayList<>();
        int numTeams = teams.size();

        if (numTeams == 0) {
            allGroupings.add(new ArrayList<>());
            return allGroupings;
        }

        // Handle even and odd number of teams.
        if (numTeams % 2 == 0) {
            // Generate all pairings.
            generateGroupingsHelper(teams, new ArrayList<>(), allGroupings);
        } else {
            // For each team, consider it as the floater and generate pairings for the rest.
            for (int i = 0; i < numTeams; i++) {
                Team floater = teams.get(i);
                List<Team> remainingTeams = new ArrayList<>(teams);
                remainingTeams.remove(i);

                List<List<List<Team>>> groupingsWithoutFloater = new ArrayList<>();
                generateGroupingsHelper(remainingTeams, new ArrayList<>(), groupingsWithoutFloater);

                // Add the floater as a group of size 1 to each grouping.
                for (List<List<Team>> grouping : groupingsWithoutFloater) {
                    List<List<Team>> groupingWithFloater = new ArrayList<>(grouping);
                    groupingWithFloater.add(Collections.singletonList(floater));
                    allGroupings.add(groupingWithFloater);
                }
            }
        }
        return allGroupings;
    }

    private void generateGroupingsHelper(List<Team> teams, List<List<Team>> currentGrouping, List<List<List<Team>>> allGroupings) {
        if (teams.isEmpty()) {
            allGroupings.add(new ArrayList<>(currentGrouping));
            return;
        }
        Team firstTeam = teams.get(0);
        for (int i = 1; i < teams.size(); i++) {
            Team secondTeam = teams.get(i);
            // Create a new group.
            List<Team> group = Arrays.asList(firstTeam, secondTeam);
            currentGrouping.add(group);
            // Create a list of remaining teams.
            List<Team> remainingTeams = new ArrayList<>(teams);
            remainingTeams.remove(firstTeam);
            remainingTeams.remove(secondTeam);
            generateGroupingsHelper(remainingTeams, currentGrouping, allGroupings);
            // Backtrack.
            currentGrouping.remove(currentGrouping.size() - 1);
        }
    }

    // Attempt to assign pairing numbers to all groups in the grouping.
    private boolean assignPairingNumbersToGrouping(List<List<Team>> grouping, Map<Integer, Set<Integer>> usedFixtureNumbersBySection, List<Team> floaters) {
        for (List<Team> group : grouping) {
            if (group.size() == 2) {
                // Try to assign pairing numbers to this pair.
                if (!assignPairingNumberToGroup(group.get(0), group.get(1), usedFixtureNumbersBySection)) {
                    return false;
                }
            } else if (group.size() == 1) {
                // Handle floater team.
                Team floater = group.get(0);
                floaters.add(floater);
            } else {
                // Invalid group size (should not happen).
                return false;
            }
        }
        return true;
    }

    private boolean assignPairingNumberToGroup(Team team1, Team team2, Map<Integer, Set<Integer>> usedFixtureNumbersBySection) {
        Section section1 = team1.getSection();
        Section section2 = team2.getSection();
        int size1 = section1.getDrawCode();
        int size2 = section2.getDrawCode();

        // Get available pairing numbers.
        List<Pair<Integer, Integer>> pairingNumbers = getAvailablePairingNumbers(size1, size2);

        // For each pairing number (a,b).
        for (Pair<Integer, Integer> pairingNumber : pairingNumbers) {
            int a = pairingNumber.getKey();
            int b = pairingNumber.getValue();
            // Try to assign a to team1, b to team2.
            if (isFixtureNumberAvailable(section1, a, usedFixtureNumbersBySection)
                    && isFixtureNumberAvailable(section2, b, usedFixtureNumbersBySection)) {
                // Assign fixture numbers.
                team1.setFixtureNumber(a);
                team2.setFixtureNumber(b);
                // Mark fixture numbers as used.
                markFixtureNumberAsUsed(section1, a, usedFixtureNumbersBySection);
                markFixtureNumberAsUsed(section2, b, usedFixtureNumbersBySection);
                return true;
            }
            // Try to assign b to team1, a to team2.
            if (isFixtureNumberAvailable(section1, b, usedFixtureNumbersBySection)
                    && isFixtureNumberAvailable(section2, a, usedFixtureNumbersBySection)) {
                // Assign fixture numbers.
                team1.setFixtureNumber(b);
                team2.setFixtureNumber(a);
                // Mark fixture numbers as used.
                markFixtureNumberAsUsed(section1, b, usedFixtureNumbersBySection);
                markFixtureNumberAsUsed(section2, a, usedFixtureNumbersBySection);
                return true;
            }
        }
        // Failed to assign pairing numbers to this group.
        return false;
    }

    // Generate available pairing numbers based on section sizes.
    private List<Pair<Integer, Integer>> getAvailablePairingNumbers(int size1, int size2) {
        int maxNumber = Math.min(size1, size2);
        if (maxNumber % 2 == 1) {
            maxNumber--; // Make it even.
        }
        List<Pair<Integer, Integer>> pairingNumbers = new ArrayList<>();
        for (int i = 1; i <= maxNumber; i += 2) {
            pairingNumbers.add(new Pair<>(i, i + 1));
        }
        return pairingNumbers;
    }

    private boolean isFixtureNumberAvailable(Section section, int number, Map<Integer, Set<Integer>> usedFixtureNumbersBySection) {
        int sectionId = section.getSectionId();
        Set<Integer> usedNumbers = usedFixtureNumbersBySection.getOrDefault(sectionId, new HashSet<>());
        return !usedNumbers.contains(number);
    }

    private void markFixtureNumberAsUsed(Section section, int number, Map<Integer, Set<Integer>> usedFixtureNumbersBySection) {
        int sectionId = section.getSectionId();
        Set<Integer> usedNumbers = usedFixtureNumbersBySection.computeIfAbsent(sectionId, k -> new HashSet<>());
        usedNumbers.add(number);
    }

    private void handleFloaters(List<Team> floaters, Map<Integer, Set<Integer>> usedFixtureNumbersBySection) {
        for (Team floater : floaters) {
            Section section = floater.getSection();
            int sectionId = section.getSectionId();
            int sectionSize = section.getDrawCode(); // Total number of fixture numbers available.

            // Get the set of used fixture numbers for this section.
            Set<Integer> usedNumbers = usedFixtureNumbersBySection.getOrDefault(sectionId, new HashSet<>());

            boolean assigned = false;
            // Iterate over all possible fixture numbers.
            for (int number = 1; number <= sectionSize; number++) {
                if (!usedNumbers.contains(number)) {
                    // Assign the fixture number to the floater.
                    floater.setFixtureNumber(number);
                    // Mark the fixture number as used.
                    usedNumbers.add(number);
                    usedFixtureNumbersBySection.put(sectionId, usedNumbers);
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                // No available fixture number found.
                System.out.println("No available fixture number for team " + floater.getTeamId() +
                        " in section " + section.getSectionName());
            }
        }
    }

    private void updateFixtureNumbers(List<Club> clubList, List<Team> floaters) {
        // Update teams in clubs
        for (Club club : clubList) {
            for (Team team : club.getTeams()) {
                teamMapper.updateTeam(team);
            }
        }

        // Update all floaters (initial and scheduling floaters)
        for (Team floater : floaters) {
            teamMapper.updateTeam(floater);
        }
    }

    private void printFixtureResultsByClub(List<Club> clubList) {
        for (Club club : clubList) {
            System.out.println("Club: " + club.getClubName());

            // Sort teams by fixture number
            club.getTeams().sort(Comparator.comparingInt(Team::getFixtureNumber));

            for (Team team : club.getTeams()) {
                System.out.println(" - Team ID: " + team.getTeamId() + ", Fixture Number: " + team.getFixtureNumber());
            }
        }
    }

    private void printFloaters(List<Team> floaters) {
        System.out.println("Floaters:");
        for (Team team : floaters) {
            System.out.println(" - Team ID: " + team.getTeamId() + ", Fixture Number: " + team.getFixtureNumber());
        }
    }

    // Simple Pair class since javafx.util.Pair may not be available
    private static class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() { return key; }

        public V getValue() { return value; }
    }
}
