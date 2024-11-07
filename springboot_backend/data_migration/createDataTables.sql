-- Administrator Table
CREATE TABLE Administrator (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- Competition Table
CREATE TABLE Competition (
    comp_id INT AUTO_INCREMENT PRIMARY KEY,
    comp_name VARCHAR(100) NOT NULL,
    admin_id INT,
    FOREIGN KEY (admin_id) REFERENCES Administrator(admin_id)
);

-- Section Table
CREATE TABLE Section (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    section_code INT NOT NULL,
    section_name VARCHAR(100) NOT NULL,
    draw_code INT,
    competition_id INT,
    FOREIGN KEY (competition_id) REFERENCES Competition(comp_id)
);
-- Club Table
CREATE TABLE Club (
    club_id INT AUTO_INCREMENT PRIMARY KEY,
    club_name VARCHAR(100) NOT NULL,
    competition_id INT,
    FOREIGN KEY (competition_id) REFERENCES Competition(comp_id)
);

-- Team Table
CREATE TABLE Team (
    team_id INT AUTO_INCREMENT PRIMARY KEY,
    team_code INT NOT NULL,
    team_colour VARCHAR(50),
    fixture_number INT,
    outside_court VARCHAR(50),
    section_id INT,
    club_id INT,
    FOREIGN KEY (section_id) REFERENCES Section(section_id),
    FOREIGN KEY (club_id) REFERENCES Club(club_id)
);



-- Constrain Table
CREATE TABLE Constrain (
    constrain_id INT AUTO_INCREMENT PRIMARY KEY,
    constrain_type INT NOT NULL,
    team1_code INT,
    team2_code INT,
    competition_id INT,
    round INT,
    FOREIGN KEY (competition_id) REFERENCES Competition(comp_id)
);

