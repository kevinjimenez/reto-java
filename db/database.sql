-- ============================================================================
-- BASE DE DATOS: cliente
-- ============================================================================

CREATE TABLE IF NOT EXISTS personas (
                                        id              BIGSERIAL PRIMARY KEY,
                                        nombre          VARCHAR(150) NOT NULL,
    genero          VARCHAR(20)  NOT NULL,
    edad            INTEGER      NOT NULL CHECK (edad > 0),
    identificacion  VARCHAR(30)  NOT NULL UNIQUE,
    direccion       VARCHAR(200),
    telefono        VARCHAR(20)
    );

CREATE TABLE IF NOT EXISTS clientes (
                                        persona_id  BIGINT PRIMARY KEY REFERENCES personas(id) ON DELETE CASCADE,
    cliente_id  VARCHAR(30)  NOT NULL UNIQUE,
    contrasena  VARCHAR(100) NOT NULL,
    estado      BOOLEAN      NOT NULL DEFAULT TRUE
    );

-- Carga inicial: Creacion de Usuarios (caso de uso 1)
INSERT INTO personas (nombre, genero, edad, identificacion, direccion, telefono) VALUES
                                                                                     ('Jose Lema',           'Masculino', 35, '1000000001', 'Otavalo sn y principal',   '098254785'),
                                                                                     ('Marianela Montalvo',  'Femenino',  40, '1000000002', 'Amazonas y NNUU',          '097548965'),
                                                                                     ('Juan Osorio',         'Masculino', 30, '1000000003', '13 junio y Equinoccial',   '098874587');

INSERT INTO clientes (persona_id, cliente_id, contrasena, estado) VALUES
                                                                      ((SELECT id FROM personas WHERE identificacion = '1000000001'), 'jlema',      '1234', TRUE),
                                                                      ((SELECT id FROM personas WHERE identificacion = '1000000002'), 'mmontalvo',  '5678', TRUE),
                                                                      ((SELECT id FROM personas WHERE identificacion = '1000000003'), 'josorio',    '1245', TRUE);


-- ============================================================================
-- BASE DE DATOS: cuenta
-- ============================================================================

-- Replica local (solo lectura) de clientes, poblada de forma asincrona por
-- ms-cliente a traves de RabbitMQ. Se precarga aqui para que el esquema sea
CREATE TABLE IF NOT EXISTS clientes_replica (
                                                cliente_persona_id  BIGINT PRIMARY KEY,
                                                cliente_id          VARCHAR(30)  NOT NULL UNIQUE,
    nombre              VARCHAR(150) NOT NULL,
    estado              BOOLEAN      NOT NULL DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS cuentas (
                                       id             BIGSERIAL PRIMARY KEY,
                                       numero_cuenta  VARCHAR(20)    NOT NULL UNIQUE,
    tipo_cuenta    VARCHAR(20)    NOT NULL,
    saldo_inicial  NUMERIC(15,2)  NOT NULL,
    saldo_actual   NUMERIC(15,2)  NOT NULL,
    estado         BOOLEAN        NOT NULL DEFAULT TRUE,
    cliente_id     VARCHAR(30)    NOT NULL
    );

CREATE TABLE IF NOT EXISTS movimientos (
                                           id               BIGSERIAL PRIMARY KEY,
                                           fecha            TIMESTAMP      NOT NULL DEFAULT NOW(),
    tipo_movimiento  VARCHAR(20)    NOT NULL,
    valor            NUMERIC(15,2)  NOT NULL,
    saldo            NUMERIC(15,2)  NOT NULL,
    cuenta_id        BIGINT         NOT NULL REFERENCES cuentas(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_movimientos_cuenta_fecha ON movimientos (cuenta_id, fecha);

-- Replica de clientes (equivalente a lo que llegaria via evento asincrono)
INSERT INTO clientes_replica (cliente_persona_id, cliente_id, nombre, estado) VALUES
                                                                                  (1, 'jlema',     'Jose Lema',          TRUE),
                                                                                  (2, 'mmontalvo', 'Marianela Montalvo', TRUE),
                                                                                  (3, 'josorio',   'Juan Osorio',        TRUE);

-- Carga inicial: Creacion de Cuentas de Usuario (caso de uso 2)
INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo_inicial, saldo_actual, estado, cliente_id) VALUES
                                                                                                      ('478758', 'AHORRO',    2000.00, 2000.00, TRUE, 'jlema'),
                                                                                                      ('225487', 'CORRIENTE',  100.00,  100.00, TRUE, 'mmontalvo'),
                                                                                                      ('495878', 'AHORROS',      0.00,    0.00, TRUE, 'josorio'),
                                                                                                      ('496825', 'AHORROS',    540.00,  540.00, TRUE, 'mmontalvo');

-- Caso de uso 3: Nueva cuenta corriente para Jose Lema
INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo_inicial, saldo_actual, estado, cliente_id) VALUES
    ('585545', 'CORRIENTE', 1000.00, 1000.00, TRUE, 'jlema');

-- Caso de uso 4: Movimientos (el saldo ya refleja el resultado de cada movimiento)
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id) VALUES
                                                                              (NOW(), 'RETIRO',   -575.00, 1425.00, (SELECT id FROM cuentas WHERE numero_cuenta = '478758')),
                                                                              (NOW(), 'DEPOSITO',  600.00,  700.00, (SELECT id FROM cuentas WHERE numero_cuenta = '225487')),
                                                                              (NOW(), 'DEPOSITO',  150.00,  150.00, (SELECT id FROM cuentas WHERE numero_cuenta = '495878')),
                                                                              (NOW(), 'RETIRO',   -540.00,    0.00, (SELECT id FROM cuentas WHERE numero_cuenta = '496825'));

-- Sincroniza saldo_actual de las cuentas con el ultimo movimiento registrado
UPDATE cuentas SET saldo_actual = 1425.00 WHERE numero_cuenta = '478758';
UPDATE cuentas SET saldo_actual =  700.00 WHERE numero_cuenta = '225487';
UPDATE cuentas SET saldo_actual =  150.00 WHERE numero_cuenta = '495878';
UPDATE cuentas SET saldo_actual =    0.00 WHERE numero_cuenta = '496825';