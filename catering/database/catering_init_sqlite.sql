-- TABELLE ORIGINALI DEL PROGETTO (Menù, Ricette, Utenti)

DROP TABLE IF EXISTS `Roles`;
CREATE TABLE `Roles` (
  `id` char(1) NOT NULL,
  `role` varchar(128) NOT NULL DEFAULT 's',
  PRIMARY KEY (`id`)
);
INSERT INTO `Roles` VALUES ('c','cuoco'),('s','chef'),('o','organizzatore');

DROP TABLE IF EXISTS `Users`;
CREATE TABLE `Users` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `username` varchar(128) NOT NULL DEFAULT ''
);
INSERT INTO `Users` VALUES (1,'Carlin'),(2,'Lidia'),(3,'Tony'),(4,'Marinella'),(5,'Guido'),(6,'Antonietta'),(7,'Paola'),(8,'Silvia'),(9,'Maria Pia'),(10,'Piergiorgio');

DROP TABLE IF EXISTS `UserRoles`;
CREATE TABLE `UserRoles` (
  `user_id` int(11) NOT NULL,
  `role_id` char(1) NOT NULL DEFAULT 's'
);
-- Impostiamo Carlin (1) come organizzatore e Lidia (2) come chef per far funzionare i test
INSERT INTO `UserRoles` VALUES (1,'o'),(2,'s'),(3,'c'),(4,'s'),(4,'c'),(5,'c'),(6,'c'),(7,'c'),(8,'s'),(9,'c'),(10,'c');

DROP TABLE IF EXISTS `Menus`;
CREATE TABLE `Menus` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `title` TEXT,
  `owner_id` INTEGER,
  `published` INTEGER DEFAULT 0
);

DROP TABLE IF EXISTS `MenuSections`;
CREATE TABLE `MenuSections` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `menu_id` INTEGER NOT NULL,
  `name` TEXT,
  `position` INTEGER
);

DROP TABLE IF EXISTS `MenuItems`;
CREATE TABLE `MenuItems` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `menu_id` INTEGER NOT NULL,
  `section_id` INTEGER,
  `description` TEXT,
  `recipe_id` INTEGER NOT NULL,
  `position` INTEGER
);

DROP TABLE IF EXISTS `Recipes`;
CREATE TABLE `Recipes` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT
);
INSERT INTO `Recipes` VALUES (1,'Vitello tonnato'),(2,'Carpaccio di spada'),(3,'Alici marinate'),(4,'Penne al sugo'),(5,'Risotto ai funghi');

DROP TABLE IF EXISTS `Shifts`;
CREATE TABLE `Shifts` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `shift_date` DATE,
  `time_start` TIME,
  `time_end` TIME,
  `location` TEXT
);


-- ==========================================================
-- NUOVE TABELLE E TABELLE AGGIORNATE (CASO D'USO EVENTI)
-- ==========================================================

DROP TABLE IF EXISTS `Events`;
CREATE TABLE `Events` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` TEXT,
    `date_start` DATE,
    `date_end` DATE,
    `chef_id` INTEGER DEFAULT 0,
    `location` TEXT,
    `state` TEXT DEFAULT 'in lavorazione',
    `notes` TEXT,
    `documentation` TEXT,
    `is_recurring` INTEGER DEFAULT 0,
    `recurrence_frequency` TEXT,
    `recurrence_end` DATE
);

DROP TABLE IF EXISTS `Services`;
CREATE TABLE `Services` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `event_id` INTEGER NOT NULL,
    `name` TEXT,
    `proposed_menu_id` INTEGER DEFAULT 0,
    `approved_menu_id` INTEGER DEFAULT 0,
    `service_date` DATE,
    `time_start` TIME,
    `time_end` TIME,
    `expected_participants` INTEGER,
    `location` TEXT
);

DROP TABLE IF EXISTS `EventInstances`;
CREATE TABLE `EventInstances` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `event_id` INTEGER NOT NULL,
    `date` DATE,
    `state` TEXT DEFAULT 'programmato',
    `is_modified` INTEGER DEFAULT 0,
    `custom_location` TEXT
);

DROP TABLE IF EXISTS `StaffRequests`;
CREATE TABLE `StaffRequests` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `service_id` INTEGER NOT NULL,
    `shift_id` INTEGER NOT NULL,
    `role` TEXT NOT NULL,
    `quantity` INTEGER NOT NULL,
    `assigned_user_id` INTEGER DEFAULT 0
);

DROP TABLE IF EXISTS `MenuProposals`;
CREATE TABLE `MenuProposals` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `service_id` INTEGER NOT NULL,
    `menu_id` INTEGER NOT NULL,
    `approved` INTEGER DEFAULT 0,
    `proposed_modifications` TEXT
);