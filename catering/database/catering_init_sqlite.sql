DROP TABLE IF EXISTS Roles;
CREATE TABLE Roles (
    id TEXT PRIMARY KEY,
    role TEXT NOT NULL
);
INSERT INTO Roles (id, role) VALUES ('c', 'cuoco'), ('h', 'chef'), ('o', 'organizzatore'), ('s', 'servizio');

DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL DEFAULT ''
);
INSERT INTO Users (id, username) VALUES
(1, 'Carlin'), (2, 'Lidia'), (3, 'Tony'), (4, 'Marinella'), (5, 'Guido'),
(6, 'Antonietta'), (7, 'Paola'), (8, 'Silvia'), (9, 'Marco'), (10, 'Piergiorgio');

DROP TABLE IF EXISTS UserRoles;
CREATE TABLE UserRoles (
    user_id INTEGER NOT NULL,
    role_id TEXT NOT NULL DEFAULT 'c'
);
INSERT INTO UserRoles (user_id, role_id) VALUES
(1, 'o'), (2, 'c'), (2, 'h'), (3, 'c'), (4, 'h'), (4, 'c'), (5, 'c'),
(6, 'c'), (7, 's'), (8, 'o'), (9, 's'), (10, 'o'), (7, 'c');

DROP TABLE IF EXISTS Menus;
CREATE TABLE Menus (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    owner_id INTEGER,
    published INTEGER DEFAULT 0
);
INSERT INTO Menus (id, title, owner_id, published) VALUES
(80, 'Coffee break mattutino', 2, 1),
(82, 'Cena di compleanno pesce', 3, 1),
(86, 'Menu Pinco Pallino', 2, 1);

DROP TABLE IF EXISTS MenuFeatures;
CREATE TABLE MenuFeatures (
    menu_id INTEGER NOT NULL,
    name TEXT NOT NULL DEFAULT '',
    value INTEGER DEFAULT 0
);
INSERT INTO MenuFeatures (menu_id, name, value) VALUES
(80, 'Richiede cuoco', 0), (80, 'Buffet', 0), (80, 'Richiede cucina', 0), (80, 'Finger food', 0), (80, 'Piatti caldi', 0),
(82, 'Richiede cuoco', 0), (82, 'Buffet', 0), (82, 'Richiede cucina', 0), (82, 'Finger food', 0), (82, 'Piatti caldi', 0),
(86, 'Richiede cuoco', 0), (86, 'Buffet', 0), (86, 'Richiede cucina', 0), (86, 'Finger food', 0), (86, 'Piatti caldi', 0);

DROP TABLE IF EXISTS MenuSections;
CREATE TABLE MenuSections (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    menu_id INTEGER NOT NULL,
    name TEXT,
    position INTEGER
);
INSERT INTO MenuSections (id, menu_id, name, position) VALUES
(41, 86, 'Antipasti', 0), (42, 86, 'Primi', 1), (43, 86, 'Secondi', 2), (44, 86, 'Dessert', 3),
(45, 80, 'Antipasti', 0);

DROP TABLE IF EXISTS Recipes;
CREATE TABLE Recipes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    description TEXT
);
INSERT INTO Recipes (id, name, description) VALUES
(1, 'Vitello tonnato', 'Vitello tonnato tradizionale'),
(2, 'Carpaccio di spada', 'Carpaccio di pesce spada affumicato'),
(3, 'Alici marinate', 'Alici marinate al limone'),
(4, 'Insalata di riso', 'Insalata di riso estiva'),
(5, 'Penne al sugo di baccalà', 'Primo di pesce'),
(6, 'Pappa al pomodoro', 'Zuppa toscana'),
(7, 'Hamburger con bacon e cipolla caramellata', 'Secondo di carne'),
(8, 'Salmone al forno', 'Salmone gratinato'),
(9, 'Croissant', 'Brioche sfogliata'),
(10, 'Pane al cioccolato', 'Pain au chocolat'),
(11, 'Girelle all''uvetta', 'Dolce lievitato'),
(12, 'Panini al latte', 'Panini morbidi da buffet'),
(13, 'Biscotti di pasta frolla', 'Frollini classici'),
(14, 'Lingue di gatto', 'Biscotti sottili'),
(15, 'Bigné farciti', 'Bigné salati'),
(16, 'Pizzette', 'Pizzette sfogliate'),
(17, 'Tramezzini', 'Tramezzini assortiti'),
(18, 'Sorbetto al limone', 'Dessert al cucchiaio'),
(19, 'Torta Saint Honoré', 'Torta classica'),
(20, 'Risotto alla milanese', 'Risotto allo zafferano');

DROP TABLE IF EXISTS Preparations;
CREATE TABLE Preparations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    description TEXT
);
INSERT INTO Preparations (id, name, description) VALUES
(1, 'Salsa tonnata', 'Salsa per vitello tonnato'),
(2, 'Pasta frolla', 'Base per crostate e biscotti'),
(3, 'Pasta sfoglia', 'Base sfogliata'),
(4, 'Crema pasticcera', 'Crema base per dolci'),
(5, 'Ragù di baccalà', 'Condimento per pasta'),
(6, 'Cipolle caramellate', 'Contorno agrodolce'),
(7, 'Pan di Spagna', 'Base per torte'),
(8, 'Pasta choux', 'Base per bigné'),
(9, 'Brodo di carne', 'Brodo classico'),
(10, 'Maionese', 'Emulsione base');

DROP TABLE IF EXISTS RecipePreparations;
CREATE TABLE RecipePreparations (
    recipe_id INTEGER NOT NULL,
    preparation_id INTEGER NOT NULL
);
INSERT INTO RecipePreparations (recipe_id, preparation_id) VALUES
(1, 1), (5, 5), (7, 6), (13, 2), (15, 8), (16, 3), (19, 4), (19, 7), (19, 8);

DROP TABLE IF EXISTS MenuItems;
CREATE TABLE MenuItems (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    menu_id INTEGER NOT NULL,
    section_id INTEGER,
    description TEXT,
    recipe_id INTEGER NOT NULL,
    position INTEGER
);
INSERT INTO MenuItems (id, menu_id, section_id, description, recipe_id, position) VALUES
(121, 86, 41, 'Voce 1', 1, 0), (122, 86, 41, 'Voce 2', 2, 1),
(123, 86, 42, 'Voce 3', 5, 0), (124, 86, 43, 'Voce 4', 8, 0),
(125, 86, 44, 'Voce 5', 19, 0), (126, 80, 0, 'Brioches', 9, 0),
(127, 80, 0, 'Biscotti', 13, 1);

DROP TABLE IF EXISTS Events;
CREATE TABLE Events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    date_start DATE,
    date_end DATE,
    chef_id INTEGER DEFAULT 0,
    location TEXT,
    state TEXT DEFAULT 'in lavorazione',
    notes TEXT,
    documentation TEXT,
    is_recurring INTEGER DEFAULT 0,
    recurrence_frequency TEXT,
    recurrence_end DATE
);
INSERT INTO Events (id, name, date_start, date_end, chef_id, location, state) VALUES
(1, 'Convegno Agile Community', '2025-09-25', '2025-09-25', 2, 'Centro Congressi Torino', 'programmato'),
(2, 'Compleanno di Manuela', '2025-08-13', '2025-08-13', 3, 'Villa sulla Collina', 'programmato'),
(3, 'Fiera del Sedano Rapa', '2025-10-02', '2025-10-04', 2, 'Piazza Mercato', 'programmato');

DROP TABLE IF EXISTS Services;
CREATE TABLE Services (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    event_id INTEGER NOT NULL,
    name TEXT,
    proposed_menu_id INTEGER DEFAULT 0,
    approved_menu_id INTEGER DEFAULT 0,
    service_date DATE,
    time_start TIME,
    time_end TIME,
    expected_participants INTEGER,
    location TEXT
);
INSERT INTO Services (id, event_id, name, proposed_menu_id, approved_menu_id, service_date, time_start, time_end, expected_participants, location) VALUES
(1, 2, 'Cena', 86, 86, '2025-08-13', '20:00:00', '23:30:00', 25, 'Salone Principale'),
(2, 1, 'Coffee break mattino', 0, 80, '2025-09-25', '10:30:00', '11:30:00', 100, 'Sala Caffè'),
(3, 1, 'Colazione di lavoro', 0, 86, '2025-09-25', '13:00:00', '14:30:00', 80, 'Sala Ristorante'),
(4, 1, 'Coffee break pomeriggio', 0, 80, '2025-09-25', '16:00:00', '16:30:00', 100, 'Sala Caffè'),
(5, 3, 'Pranzo giorno 1', 0, 86, '2025-10-02', '12:00:00', '15:00:00', 200, 'Padiglione A');

DROP TABLE IF EXISTS Shifts;
CREATE TABLE Shifts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date DATE,
    start_time TIME,
    end_time TIME,
    deadline DATE
);
INSERT INTO Shifts (id, date, start_time, end_time, deadline) VALUES
(1, '2025-09-25', '08:00:00', '14:00:00', '2025-09-20'),
(2, '2025-09-25', '14:00:00', '20:00:00', '2025-09-20'),
(3, '2025-08-13', '15:00:00', '23:00:00', '2025-08-10');

DROP TABLE IF EXISTS ShiftBookings;
CREATE TABLE ShiftBookings (
    shift_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL
);
INSERT INTO ShiftBookings (shift_id, user_id) VALUES
(1, 3), (1, 5), (1, 6), (2, 3), (3, 5), (3, 6);

DROP TABLE IF EXISTS SummarySheets;
CREATE TABLE SummarySheets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_id INTEGER NOT NULL,
    owner_id INTEGER NOT NULL
);

DROP TABLE IF EXISTS Tasks;
CREATE TABLE Tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sumsheet_id INTEGER NOT NULL,
    kitchenproc_id INTEGER NOT NULL,
    type INTEGER DEFAULT 1,
    position INTEGER DEFAULT 0,
    ready INTEGER DEFAULT 0,
    quantity TEXT DEFAULT '',
    portions INTEGER DEFAULT 0,
    minutes INTEGER DEFAULT 0
);

DROP TABLE IF EXISTS Assignments;
CREATE TABLE Assignments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sumsheet_id INTEGER NOT NULL,
    task_id INTEGER NOT NULL,
    shift_id INTEGER NOT NULL,
    cook_id INTEGER DEFAULT 0
);

DROP TABLE IF EXISTS EventInstances;
CREATE TABLE EventInstances (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    event_id INTEGER NOT NULL,
    date DATE,
    state TEXT DEFAULT 'programmato',
    is_modified INTEGER DEFAULT 0,
    custom_location TEXT
);

DROP TABLE IF EXISTS StaffRequests;
CREATE TABLE StaffRequests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_id INTEGER NOT NULL,
    shift_id INTEGER NOT NULL,
    role TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    assigned_user_id INTEGER DEFAULT 0
);

DROP TABLE IF EXISTS MenuProposals;
CREATE TABLE MenuProposals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_id INTEGER NOT NULL,
    menu_id INTEGER NOT NULL,
    approved INTEGER DEFAULT 0,
    proposed_modifications TEXT
);