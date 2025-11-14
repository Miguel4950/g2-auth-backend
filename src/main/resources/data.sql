-- Script de inicialización de datos para la base de datos de biblioteca
-- Grupo 2: Autenticación y Gestión de Usuarios

-- Crear tabla de tipos de usuario si no existe
CREATE TABLE IF NOT EXISTS tipo_usuario (
    id_tipo_usuario INTEGER PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

-- Insertar tipos de usuario
INSERT INTO tipo_usuario (id_tipo_usuario, nombre) VALUES 
(1, 'Estudiante'),
(2, 'Bibliotecario'),
(3, 'Admin');

-- Crear tabla de estados de usuario si no existe
CREATE TABLE IF NOT EXISTS estado_usuario (
    id_estado_usuario INTEGER PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

-- Insertar estados de usuario
INSERT INTO estado_usuario (id_estado_usuario, nombre) VALUES 
(0, 'Inactivo'),
(1, 'Activo'),
(2, 'Bloqueado');

-- Insertar usuarios de prueba
-- Contraseña por defecto para todos: "contrasena123" (BCrypt hash)
INSERT INTO usuario (nombre, username, email, contrasena, id_tipo_usuario, id_estado_usuario, intentos_fallidos) VALUES 
-- Administradores
('Admin Sistema', 'admin', 'admin@biblioteca.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 3, 1, 0),

-- Bibliotecarios
('Juan Pérez', 'jperez', 'juan.perez@biblioteca.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 2, 1, 0),
('María García', 'mgarcia', 'maria.garcia@biblioteca.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 2, 1, 0),
('Carlos Rodríguez', 'crodriguez', 'carlos.rodriguez@biblioteca.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 2, 1, 0),

-- Estudiantes
('Ana Martínez', 'amartinez', 'ana.martinez@estudiante.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 1, 1, 0),
('Luis Hernández', 'lhernandez', 'luis.hernandez@estudiante.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 1, 1, 0),
('Sofia López', 'slopez', 'sofia.lopez@estudiante.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 1, 1, 0),
('Diego Torres', 'dtorres', 'diego.torres@estudiante.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 1, 1, 0),
('Laura Ramírez', 'lramirez', 'laura.ramirez@estudiante.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 1, 1, 0),
('Pedro Sánchez', 'psanchez', 'pedro.sanchez@estudiante.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 1, 1, 0),

-- Estudiantes con diferentes estados
('Usuario Bloqueado', 'bloqueado', 'bloqueado@estudiante.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 1, 1, 5),
('Usuario Inactivo', 'inactivo', 'inactivo@estudiante.edu.co', '$2a$10$DowJBJJRh8YaE5qr6JvgKO8Ck3F7xOL5sOVNmBBdGKdHFz0L8n6Oy', 1, 0, 0);
