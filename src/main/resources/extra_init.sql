-- extra_init.sql
-- Adds refresh_token and contrasena_reset_token tables and inserts additional test users (to reach 30 users)

CREATE TABLE IF NOT EXISTS refresh_token (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(512) NOT NULL,
  id_usuario INTEGER NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  revoked BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS contrasena_reset_token (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(255) NOT NULL,
  id_usuario INTEGER NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  used BOOLEAN DEFAULT FALSE
);

-- Insert additional users (examples). Adjust contrasenas to match encoded contrasena used in project (e.g., same hash).
-- If existing users use plain text, these will be plain too. Replace contrasena hashes if necessary.

INSERT INTO usuario (username, nombre, id_tipo_usuario, id_estado_usuario, contrasena, intentos_fallidos) VALUES
('estudiante11@example.com','Estudiante 11',1,1,'contrasena',0),
('estudiante12@example.com','Estudiante 12',1,1,'contrasena',0),
('estudiante13@example.com','Estudiante 13',1,1,'contrasena',0),
('estudiante14@example.com','Estudiante 14',1,1,'contrasena',0),
('estudiante15@example.com','Estudiante 15',1,1,'contrasena',0),
('bibliotecario11@example.com','Bibliotecario 11',2,1,'contrasena',0),
('bibliotecario12@example.com','Bibliotecario 12',2,1,'contrasena',0),
('bibliotecario13@example.com','Bibliotecario 13',2,1,'contrasena',0),
('usuario11@example.com','Usuario 11',3,1,'contrasena',0),
('usuario12@example.com','Usuario 12',3,1,'contrasena',0),
('usuario13@example.com','Usuario 13',3,1,'contrasena',0),
('usuario14@example.com','Usuario 14',3,1,'contrasena',0),
('usuario15@example.com','Usuario 15',3,1,'contrasena',0),
('usuario16@example.com','Usuario 16',3,1,'contrasena',0),
('usuario17@example.com','Usuario 17',3,1,'contrasena',0),
('usuario18@example.com','Usuario 18',3,1,'contrasena',0),
('usuario19@example.com','Usuario 19',3,1,'contrasena',0),
('usuario20@example.com','Usuario 20',3,1,'contrasena',0);
