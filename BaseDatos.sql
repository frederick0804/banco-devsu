-- ============================================================
--  BaseDatos.sql — Banco Devsu
--  Esquema e información inicial de prueba
-- ============================================================

CREATE TABLE IF NOT EXISTS persona (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    genero      VARCHAR(50),
    edad        INTEGER,
    identificacion VARCHAR(255) UNIQUE NOT NULL,
    direccion   VARCHAR(255),
    telefono    VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS cliente (
    id          BIGINT PRIMARY KEY REFERENCES persona(id),
    clienteid   VARCHAR(255) UNIQUE NOT NULL,
    contrasena  VARCHAR(255) NOT NULL,
    estado      BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS cuenta (
    id              BIGSERIAL PRIMARY KEY,
    numero_cuenta   VARCHAR(255) UNIQUE NOT NULL,
    tipo_cuenta     VARCHAR(100) NOT NULL,
    saldo_inicial   DOUBLE PRECISION NOT NULL,
    estado          BOOLEAN NOT NULL,
    cliente_id      BIGINT NOT NULL REFERENCES cliente(id)
);

CREATE TABLE IF NOT EXISTS movimiento (
    id               BIGSERIAL PRIMARY KEY,
    fecha            TIMESTAMP NOT NULL,
    tipo_movimiento  VARCHAR(50) NOT NULL,
    valor            DOUBLE PRECISION NOT NULL,
    saldo            DOUBLE PRECISION NOT NULL,
    cuenta_id        BIGINT NOT NULL REFERENCES cuenta(id)
);

-- ============================================================
--  Datos iniciales — Casos de uso del enunciado
-- ============================================================

-- 1. Clientes
INSERT INTO persona (id, nombre, genero, edad, identificacion, direccion, telefono) VALUES
  (1, 'Jose Lema',          'Masculino', 30, '1234567890', 'Otavalo sn y principal',    '098254785'),
  (2, 'Marianela Montalvo', 'Femenino',  28, '0987654321', 'Amazonas y NNUU',            '097548965'),
  (3, 'Juan Osorio',        'Masculino', 35, '1122334455', '13 junio y Equinoccial',     '098874587')
ON CONFLICT DO NOTHING;

SELECT setval('persona_id_seq', (SELECT MAX(id) FROM persona));

INSERT INTO cliente (id, clienteid, contrasena, estado) VALUES
  (1, 'jose123',       '1234', true),
  (2, 'marianela456',  '5678', true),
  (3, 'juanosorio789', '1245', true)
ON CONFLICT DO NOTHING;

-- 2. Cuentas
INSERT INTO cuenta (id, numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id) VALUES
  (1, '478758', 'Ahorro',    2000, true, 1),
  (2, '225487', 'Corriente',  100, true, 2),
  (3, '495878', 'Ahorros',      0, true, 3),
  (4, '496825', 'Ahorros',    540, true, 2),
  (5, '585545', 'Corriente', 1000, true, 1)
ON CONFLICT DO NOTHING;

SELECT setval('cuenta_id_seq', (SELECT MAX(id) FROM cuenta));

-- 3. Movimientos (saldo calculado sobre saldo inicial de cada cuenta)
--    478758 → retiro  575  → saldo 1425
--    225487 → depósito 600 → saldo 700
--    495878 → depósito 150 → saldo 150
--    496825 → retiro  540  → saldo 0
INSERT INTO movimiento (id, fecha, tipo_movimiento, valor, saldo, cuenta_id) VALUES
  (1, '2022-02-10 10:00:00', 'Débito',  -575,  1425, 1),
  (2, '2022-02-10 10:00:00', 'Crédito',  600,   700, 2),
  (3, '2022-02-08 10:00:00', 'Crédito',  150,   150, 3),
  (4, '2022-02-08 10:00:00', 'Débito',  -540,     0, 4)
ON CONFLICT DO NOTHING;

SELECT setval('movimiento_id_seq', (SELECT MAX(id) FROM movimiento));
