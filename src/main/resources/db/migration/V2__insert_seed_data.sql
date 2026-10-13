-- Extensión de PostgreSQL que sabe calcular hashes BCrypt
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_USER');

-- Usuarios de demostración: cambia estas contraseñas antes de publicar la API
INSERT INTO users (username, password_hash, full_name, email) VALUES
    ('admin', crypt('admin123', gen_salt('bf', 10)), 'Administrador', 'admin@tickets.pe'),
    ('ana',   crypt('ana123',   gen_salt('bf', 10)), 'Ana Torres',    'ana@tickets.pe');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r
  ON (u.username = 'admin' AND r.name = 'ROLE_ADMIN')
  OR (u.username = 'ana'   AND r.name = 'ROLE_USER');

INSERT INTO categories (name, description) VALUES
    ('Hardware', 'Equipos, monitores, teclados e impresoras'),
    ('Software', 'Programas, licencias e instalaciones'),
    ('Redes',    'Internet, wifi y acceso a servidores');