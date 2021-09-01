DROP TABLE IF EXISTS people;

CREATE TABLE people  (
                         person_id INT IDENTITY NOT NULL PRIMARY KEY,
                         first_name VARCHAR(20),
                         last_name VARCHAR(20)
);

INSERT INTO people (person_id, first_name, last_name) VALUES (0, 'Justin', 'Doe');
INSERT INTO people (person_id, first_name, last_name) VALUES (1, 'Jane', 'Doe');
INSERT INTO people (person_id, first_name, last_name) VALUES (2, 'Joe', 'Doe');
INSERT INTO people (person_id, first_name, last_name) VALUES (3, 'Jill', 'Doe');
INSERT INTO people (person_id, first_name, last_name) VALUES (4, 'John', 'Doe');