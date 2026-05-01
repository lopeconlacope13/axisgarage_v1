-- Insertar datos de ejemplo para 'roles'
INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_MANAGER'), (3, 'ROLE_USER');

-- Insertar datos de ejemplo para 'users'
INSERT IGNORE INTO users (id, username, email, password, enabled, first_name, last_name, image, created_date, last_modified_date)
VALUES (1, 'admin', 'admin@axisgarage.com', '$2b$12$FVRijCavVZ7Qt15.CQssHe9m/6eLAdjAv0PiOKFIjMU161wApxzye', true, 'Admin', 'User', '/images/admin.jpg', NOW(), NOW()),
       (2, 'manager', 'manager@axisgarage.com', '$2b$12$FVRijCavVZ7Qt15.CQssHe9m/6eLAdjAv0PiOKFIjMU161wApxzye', true, 'Manager', 'User', '/images/manager.jpg', NOW(), NOW()),
       (3, 'normal', 'normal@axisgarage.com', '$2b$12$FVRijCavVZ7Qt15.CQssHe9m/6eLAdjAv0PiOKFIjMU161wApxzye', true, 'Regular', 'User', '/images/user.jpg', NOW(), NOW());

-- Usuarios owner (MANAGER) — contraseña: password
INSERT IGNORE INTO users (id, username, email, password, enabled, first_name, last_name, image, created_date, last_modified_date)
VALUES (4, 'manolo',  'manolo@axisgarage.com',   '$2b$12$FVRijCavVZ7Qt15.CQssHe9m/6eLAdjAv0PiOKFIjMU161wApxzye', true, 'Manolo',   'López',      null, NOW(), NOW()),
       (5, 'lucia',   'lucia@axisgarage.com',    '$2b$12$FVRijCavVZ7Qt15.CQssHe9m/6eLAdjAv0PiOKFIjMU161wApxzye', true, 'Lucía',    'García',     null, NOW(), NOW()),
       (6, 'antonio', 'antonio@axisgarage.com',  '$2b$12$FVRijCavVZ7Qt15.CQssHe9m/6eLAdjAv0PiOKFIjMU161wApxzye', true, 'Antonio',  'Recio',      null, NOW(), NOW()),
       (7, 'elena',   'elena@axisgarage.com',    '$2b$12$FVRijCavVZ7Qt15.CQssHe9m/6eLAdjAv0PiOKFIjMU161wApxzye', true, 'Elena',    'Nito',       null, NOW(), NOW());

-- Asignar roles a usuarios (1=ADMIN, 2=MANAGER, 3=USER)
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1, 1), (2, 2), (3, 3), (4, 2), (5, 2), (6, 2), (7, 2);

-- Insertar Sedes (Locations)
INSERT IGNORE INTO locations (id, name, city, address, postal_code, country, phone, email) VALUES
(1, 'Axis Garage Sevilla', 'Sevilla', 'Calle Sierpes 42', '41004', 'España', '954111222', 'sevilla@axisgarage.com'),
(2, 'Axis Garage Madrid', 'Madrid', 'Paseo de la Castellana 100', '28046', 'España', '911333444', 'madrid@axisgarage.com'),
(3, 'Axis Garage Barcelona', 'Barcelona', 'Passeig de Gràcia 55', '08007', 'España', '933555666', 'barcelona@axisgarage.com'),
(4, 'Axis Garage Puerto Banús', 'Marbella', 'Puerto Banús s/n', '29660', 'España', '952777888', 'puertobanus@axisgarage.com');

-- Insertar Categorías de Vehículos
INSERT IGNORE INTO vehicle_categories (id, name, description) VALUES
(1, 'GRAN_TURISMO', 'Gran turismo y coupés de lujo'),
(2, 'SUPERCAR', 'Superdeportivos y hypercar'),
(3, 'HERITAGE', 'Clásicos y vehículos de colección'),
(4, 'EXECUTIVE', 'Berlinas deportivas de alto rendimiento'),
(6, 'SUV', 'SUVs premium y todoterrenos de ultra-lujo');

-- Insertar Propietarios (Owners)
INSERT IGNORE INTO owners (id, name, last_name, email, phone) VALUES
(1, 'Manolo', 'López', 'manolo@axisgarage.com', '600111222'),
(2, 'Lucía', 'García', 'lucia@axisgarage.com', '600333444'),
(3, 'Antonio', 'Recio', 'antonio@axisgarage.com', '600555666'),
(4, 'Elena', 'Nito', 'elena@axisgarage.com', '600777888');

-- Insertar Pilotos / Clientes (Renters)
INSERT IGNORE INTO renters (id, name, last_name, email, dni, phone, address) VALUES
(1, 'Carlos', 'Viajero', 'carlos@gmail.com', '12345678A', '611000111', 'Calle Sierpes 15, Sevilla'),
(2, 'Ana', 'Turista', 'ana@gmail.es', '87654321B', '611000222', 'Paseo de la Castellana 50, Madrid'),
(3, 'David', 'Mochilero', 'david@gmail.com', '11223344C', '611000333', 'Passeig de Gràcia 12, Barcelona'),
(4, 'Sofía', 'Exploradora', 'sofia@gmail.com', '55667788D', '611000444', 'Puerto Banús s/n, Marbella'),
(5, 'Miguel', 'Aventurero', 'miguel@gmail.com', '99887766E', '611000555', 'Calle Alcalá 88, Madrid');

-- Insertar Vehículos (con category_id y location_id)
INSERT IGNORE INTO vehicles (id, owner_id, category_id, location_id, brand, model, production_year, price_per_day, engine_type, horse_power, torque_nm, transmission, drivetrain, fuel_type, zero_to_hundred, description, available) VALUES
(1, 1, 1, 1, 'Ferrari', 'Roma Spider', 2024, 1300.00, '3855 L', 620, 760, 'F1 DCT', 'RWD', 'Gasolina', 3.4, 'La elegancia atemporal de la Dolce Vita sin techo. Un diseño impecable y un corazón V8 que transforma cada paseo por la costa en una obra de arte.', TRUE),
(2, 2, 2, 2, 'Ferrari', 'F8 Tributo', 2023, 1400.00, '4.0 L', 720, 770, 'F1 DCT', 'RWD', 'Gasolina', 2.9, 'El homenaje definitivo al V8 de Maranello. Una aerodinámica esculpida por el viento y una potencia devastadora que exprime las leyes de la física.', TRUE),
(3, 3, 2, 3, 'Ferrari', '296 GTB', 2024, 2800.00, '3 L', 830, 740, 'F1 DCT', 'RWD', 'Híbrido', 2.9, 'El renacer híbrido del Cavallino. Empuje constante y salvaje gracias a la combinación perfecta de electrificación y la pureza de un V6 a 8.500 rpm.', TRUE),
(4, 4, 1, 4, 'Ferrari', '812 Superfast', 2022, 1700.00, '6.5 L', 800, 718, 'F1 DCT', 'RWD', 'Gasolina', 2.9, 'La brutalidad de un V12 delantero que aúlla y desgarra el asfalto. El Gran Turismo definitivo para devorar continentes a velocidades inconfesables.', TRUE),
(5, 1, 6, 1, 'Ferrari', 'Purosangue', 2024, 3450.00, '6.5 L', 725, 716, 'F1 DCT', 'AWD', 'Gasolina', 3.3, 'El primer Ferrari de cuatro puertas. Dinámica de superdeportivo y la majestuosidad de un V12, redefiniendo el concepto de todoterreno de ultralujo.', TRUE),
(6, 2, 2, 2, 'Ferrari', 'SF90 Spider', 2023, 1900.00, '4 L', 1000, 800, 'F1 DCT', 'AWD', 'Híbrido', 2.5, 'Mil caballos de fuerza híbrida a cielo abierto. El hypercar que pulveriza cronómetros y te permite sentir el futuro rozando la barrera del sonido.', TRUE),
(7, 3, 1, 3, 'Ferrari', 'Portofino', 2022, 1100.00, '3.8 L', 600, 760, 'F1 DCT', 'RWD', 'Gasolina', 3.5, 'El descapotable más versátil de Maranello. Combina una agresividad contenida con un confort absoluto para el día a día bajo el sol.', TRUE),
(8, 4, 2, 4, 'McLaren', '720S Spider', 2023, 1200.00, '4 L', 720, 770, 'SSG', 'RWD', 'Gasolina', 2.9, 'Ingeniería alienígena al servicio del conductor. Un peso pluma que desafía constantemente el tiempo y la física con su techo de cristal retráctil.', TRUE),
(9, 1, 2, 1, 'Lamborghini', 'Aventador', 2021, 1400.00, '6.5 L', 700, 690, 'ISR', 'AWD', 'Gasolina', 2.9, 'La definición del coche póster. Un V12 brutal, líneas cortantes y unas puertas de tijera que garantizan ser el centro de todas las miradas.', TRUE),
(10, 2, 2, 2, 'Lamborghini', 'Aventador SVJ Coupe', 2021, 1400.00, '6.5 L', 770, 720, 'ISR', 'AWD', 'Gasolina', 2.8, 'El rey del Nürburgring. Aerodinámica activa ALA y un rugido ensordecedor para una experiencia de circuito llevada a las calles.', TRUE),
(11, 3, 2, 3, 'Lamborghini', 'Huracan EVO', 2023, 1300.00, '5.2 L', 640, 600, 'LDF', 'AWD', 'Gasolina', 2.9, 'Dinamismo puro e instintivo. Un motor V10 que corta la respiración, unido a un sistema predictivo que lee tus intenciones en cada curva.', TRUE),
(12, 4, 2, 4, 'Lamborghini', 'Huracan STO', 2023, 1400.00, '5.2 L', 640, 565, 'LDF', 'RWD', 'Gasolina', 3.0, 'Nacido en el circuito. Homologado por pura cortesía, este tracción trasera es lo más cercano a conducir un coche del Super Trofeo.', TRUE),
(13, 1, 2, 1, 'Lamborghini', 'Revuelto', 2024, 2700.00, '6.5 L', 1015, 725, 'DCT', 'AWD', 'Híbrido', 2.5, 'El trueno eléctrico de Sant Agata. Más de mil caballos de un V12 híbrido enchufable que te catapulta hacia la nueva era de los hypercars.', TRUE),
(14, 2, 6, 2, 'Lamborghini', 'Urus S', 2024, 1400.00, '4 L', 666, 850, 'Automática', 'AWD', 'Gasolina', 3.5, 'El demonio disfrazado de todoterreno. Espacio de lujo para cinco y dinámica de superdeportivo para cruzar el continente de forma inconfesable.', TRUE),
(15, 3, 2, 3, 'Audi', 'R8', 2023, 700.00, '5.2 L', 540, 540, 'S tronic', 'quattro', 'Gasolina', 3.5, 'La usabilidad diaria unida a un corazón V10 de competición. Precisión alemana y tracción quattro para dominar cualquier carretera.', TRUE),
(16, 4, 4, 4, 'Audi', 'RS7', 2023, 550.00, '4 L', 600, 800, 'Tiptronic', 'quattro', 'Gasolina', 3.6, 'La berlina coupé definitiva. Diseño agresivo, espacio ejecutivo y una aceleración balística para viajes de negocios de alta velocidad.', TRUE),
(17, 1, 4, 1, 'Audi', 'RS3', 2023, 450.00, '2.5 L', 401, 500, 'S tronic', 'quattro', 'Gasolina', 3.6, 'El hot-hatch que avergüenza a superdeportivos. Un icónico motor de cinco cilindros con un sonido que emula a los Grupo B de rally.', TRUE),
(18, 2, 4, 2, 'Audi', 'RS3 ABT', 2023, 550.00, '2480 L', 367, 540, 'S tronic', 'quattro', 'Gasolina', 4.1, 'Una inyección de esteroides de ABT Sportsline. Más potencia, escape radical y estética ensanchada para los inconformistas de la carretera.', TRUE),
(19, 3, 4, 3, 'Audi', 'e-tron GT', 2024, 650.00, 'Electric', 476, 630, 'Direct Drive', 'quattro', 'Eléctrico', 4.1, 'Una nave espacial en absoluto silencio. Líneas esculturales y empuje eléctrico inmediato, demostrando que el futuro ya nos ha arrollado.', TRUE),
(20, 4, 1, 4, 'Porsche', '911 Carrera 4S Coupé', 2023, 450.00, '3.6 L', 320, 530, 'PDK', 'AWD', 'Gasolina', 5.1, 'El deportivo perfecto para los 365 días del año. Su tracción integral garantiza que cada uno de sus caballos se agarre al asfalto como un imán.', TRUE),
(21, 1, 1, 1, 'Porsche', '911 Carrera S Cabrio', 2023, 500.00, '3.0 L', 420, 500, 'PDK', 'RWD', 'Gasolina', 4.5, 'La magia del nueveonce bajo las estrellas. Siente el viento y el aullido del motor bóxer a tu espalda en la máxima expresión de libertad.', TRUE),
(22, 2, 2, 2, 'Porsche', '992 GT3', 2023, 750.00, '4 L', 510, 470, 'PDK', 'RWD', 'Gasolina', 3.4, 'Nacido en la pista. Aerodinámica avanzada y un motor atmosférico que te exige la perfección en cada vértice y sube de vueltas sin fin.', TRUE),
(23, 3, 2, 3, 'Nissan', 'GT-R', 2022, 850.00, '3799 L', 573, 633, 'Automática', 'AWD', 'Gasolina', 2.7, 'Godzilla. El cazagigantes por excelencia que somete la carretera con una tracción brutal, destrozando superdeportivos europeos sin inmutarse.', TRUE),
(24, 4, 1, 4, 'BMW', 'M4 Cabrio', 2023, 450.00, '4.0 L', 430, 650, 'M Steptronic', 'RWD', 'Gasolina', 4.3, 'Fuerza bruta bávara con el cielo como techo. Un chasis concebido para deslizar y un motor que entrega la potencia sin piedad.', TRUE),
(25, 1, 1, 1, 'BMW', 'M8 Cabrio', 2023, 600.00, '4.4 L', 600, 750, 'M Steptronic', 'AWD', 'Gasolina', 3.4, 'El culmen del lujo descapotable de BMW. Un misil balístico disfrazado de yate de lujo para surcar las carreteras a cielo abierto.', TRUE),
(26, 2, 4, 2, 'BMW', 'i7', 2024, 750.00, 'Electric', 544, 745, 'Direct Drive', 'AWD', 'Eléctrico', 4.7, 'La sala de juntas más tecnológica del planeta. Lujo extremo, pantalla de cine trasera y la suavidad inmensa de su silencioso corazón eléctrico.', TRUE),
(27, 3, 4, 3, 'BMW', '5', 2023, 250.00, '3498 L', 249, 350, 'Steptronic', 'RWD', 'Gasolina', 5.7, 'La berlina ejecutiva por antonomasia. Un equilibrio majestuoso entre confort para el día a día y la agilidad intrínseca del ADN bávaro.', TRUE),
(28, 4, 1, 4, 'Aston Martin', 'DBS', 2022, 1100.00, '5.2 L', 770, 900, 'ZF', 'RWD', 'Gasolina', 3.4, 'El traje a medida más rápido del mundo. La brutalidad británica encarnada en un motor que entrega un par inagotable bajo un diseño de museo.', TRUE),
(29, 1, 1, 1, 'Aston Martin', 'DB12', 2024, 900.00, '4 L', 680, 800, 'ZF', 'RWD', 'Gasolina', 3.6, 'El primer Super Tourer. Interior rediseñado al lujo supremo y un motor que ruge elegantemente para conquistar la Riviera Francesa.', TRUE),
(30, 2, 1, 2, 'Bentley', 'Continental GT Speed', 2023, 1200.00, '6 L', 659, 900, 'DCT', 'AWD', 'Gasolina', 3.6, 'Opulencia rodante a 335 km/h. Materiales nobles trabajados a mano y un inagotable bloque para cruzar Europa con una clase inigualable.', TRUE),
(31, 3, 4, 3, 'Rolls-Royce', 'Phantom', 2023, 1700.00, '6.75 L', 571, 900, 'Automática', 'RWD', 'Gasolina', 5.3, 'Deslizándose sobre la calzada en mutismo absoluto. La máxima expresión de riqueza y estatus, diseñada para aislarte del mundo terrenal.', TRUE),
(32, 4, 4, 4, 'Rolls-Royce', 'Spectre', 2024, 2950.00, 'Electric', 585, 900, 'Direct Drive', 'AWD', 'Eléctrico', 4.5, 'El Rolls-Royce definitivo. La propulsión eléctrica eleva a la perfección el legendario silencio de la marca en un coupé de dimensiones épicas.', TRUE),
(33, 1, 6, 1, 'Mercedes', 'GLE', 2023, 450.00, '2.9 L', 367, 500, '9G-Tronic', 'AWD', 'Gasolina', 5.7, 'El SUV premium que domina cualquier terreno sin perder la compostura. Altura dominante y un interior inmersivo cargado de tecnología.', TRUE),
(34, 2, 4, 2, 'Mercedes', 'V', 2023, 380.00, '2.5 L', 190, 440, '9G-Tronic', 'RWD', 'Diesel', 9.1, 'El salón VIP en movimiento. Transporta a siete afortunados con la majestuosidad y el confort digno de la estrella de Stuttgart.', TRUE),
(35, 3, 4, 3, 'Mercedes', 'Vito', 2023, 350.00, '1987 L', 160, 380, '9G-Tronic', 'RWD', 'Diesel', 9.3, 'Versatilidad ejecutiva. Ideal para el transporte premium de equipos con una fiabilidad inquebrantable y confort inmejorable.', TRUE),
(36, 4, 4, 4, 'Mercedes', 'CLASS S 350 LONG', 2023, 450.00, '3.0 L', 258, 620, '9G-Tronic', 'RWD', 'Diesel', 6.8, 'El estándar oro de las berlinas de representación. Espacio infinito en las plazas traseras para cerrar los negocios más importantes del mundo.', TRUE),
(37, 1, 4, 1, 'Audi', 'A8', 2023, 350.00, '3993 L', 450, 660, 'Tiptronic', 'quattro', 'Gasolina', 4.7, 'La sobriedad alemana llevada a la excelencia. Un buque insignia que oculta un poderío inmenso bajo una carrocería de elegancia innegable.', TRUE),
(38, 2, 6, 2, 'Range Rover', 'Sport', 2023, 370.00, '5.0 L', 550, 680, 'Automática', 'AWD', 'Gasolina', 4.4, 'Estilo británico imponente. Un todoterreno deportivo que no se achanta ante un puerto de montaña ni desentona en el casino de Montecarlo.', TRUE),
(39, 3, 6, 3, 'Range Rover', 'Sport SVR', 2023, 850.00, '4999 L', 550, 680, 'Automática', 'AWD', 'Gasolina', 4.7, 'Un tanque de asalto vestido de etiqueta. Presencia implacable y un aullido sobrealimentado que derriba cualquier complejo de la vía.', TRUE),
(40, 4, 6, 4, 'Range Rover', 'Vogue Supercharged', 2023, 750.00, '5000 L', 510, 625, 'Automática', 'AWD', 'Gasolina', 5.4, 'La realeza del off-road. El vehículo preferido de la aristocracia que aísla de las imperfecciones del camino con un poderío colosal.', TRUE),
(41, 1, 6, 1, 'Jeep', 'Wrangler Hybrid Rubicon', 2023, 400.00, '2 L', 380, 637, 'Automática', 'AWD', 'Híbrido', 6.0, 'La leyenda americana electrificada. Desmonta puertas y techo para un contacto total con la naturaleza mientras conquistas montañas en silencio.', TRUE),
(42, 2, 2, 2, 'Bugatti', 'Chiron', 2022, 25000.00, '8 L', 1500, 1600, 'DSG', 'AWD', 'Gasolina', 2.4, 'El ápice de la ingeniería automotriz. 1.500 caballos que reescriben las leyes de la física. No es un coche, es una deidad sobre el asfalto.', TRUE);


-- Insertar Reservas
INSERT IGNORE INTO reservations (id, vehicle_id, renter_id, start_date, end_date, total_price, status) VALUES
(1, 1, 1, '2023-12-01', '2023-12-05', 480.00, 'CONFIRMED'),
(2, 3, 2, '2024-01-10', '2024-01-15', 1000.00, 'CONFIRMED'),
(3, 2, 3, '2024-02-14', '2024-02-16', 170.00, 'CONFIRMED'),
(4, 4, 4, '2024-03-20', '2024-03-25', 750.00, 'CONFIRMED'),
(5, 5, 1, '2025-06-01', '2025-06-10', 855.00, 'CONFIRMED'),
(6, 6, 2, '2025-07-15', '2025-07-20', 550.00, 'CONFIRMED'),
(7, 7, 5, '2025-08-01', '2025-08-15', 2520.00, 'CONFIRMED'),
(8, 8, 3, '2025-09-10', '2025-09-12', 500.00, 'CONFIRMED'),
(9, 1, 4, '2025-10-05', '2025-10-10', 600.00, 'CONFIRMED'),
(10, 3, 1, '2025-12-24', '2025-12-31', 1400.00, 'CONFIRMED');

-- Insertar Coberturas (una por reserva, STANDARD por defecto)
INSERT IGNORE INTO coverages (id, reservation_id, type, total_price) VALUES
(1, 1, 'STANDARD', 0.00),
(2, 2, 'STANDARD', 0.00),
(3, 3, 'STANDARD', 0.00),
(4, 4, 'PREMIUM', 45.00),
(5, 5, 'STANDARD', 0.00),
(6, 6, 'TOTAL', 120.00),
(7, 7, 'STANDARD', 0.00),
(8, 8, 'STANDARD', 0.00),
(9, 9, 'PREMIUM', 45.00),
(10, 10, 'STANDARD', 0.00);

-- Insertar Informes de Daños
INSERT IGNORE INTO damage_reports (id, reservation_id, type, description, reported_date, image_url) VALUES
(1, 1, 'PRE', 'Arañazo leve en parachoques delantero derecho', '2023-12-01', NULL),
(2, 1, 'POST', 'Ningún daño adicional detectado tras la devolución', '2023-12-05', NULL),
(3, 2, 'PRE', 'Vehículo en perfecto estado, sin daños previos', '2024-01-10', NULL);

-- Insertar Opiniones (Reviews)
INSERT IGNORE INTO reviews (id, reservation_id, renter_id, rating, comment) VALUES
(1, 2, 2, 5, 'Un coche espectacular, el motor V8 es increíble. Repetiré seguro.'),
(2, 1, 1, 4, 'El 911 es muy bonito, pero hacía un poco de ruido por la noche en carretera.'),
(3, 4, 4, 5, 'Perfecto para disfrutar. Aceleración absoluta y agarre precioso.'),
(4, 3, 3, 3, 'El coche está bien, pero el infotainment no funcionaba muy rápido.'),
(5, 5, 1, 5, 'El dueño, Antonio, es majísimo. Nos dejó un depósito lleno gratis.'),
(6, 6, 2, 4, 'Muy rápido y deportivo, prestaciones inmejorables.');
-- Insertar Facturas (Invoices) de ejemplo para reservas confirmadas
INSERT IGNORE INTO invoices (id, reservation_id, invoice_number, issue_date, base_amount, tax_rate, tax_amount, total_amount, payment_method, notes) VALUES
(1, 1, 'AG-2023-0001', '2023-12-05', 480.00, 0.2100, 100.80, 580.80, 'CARD', NULL),
(2, 2, 'AG-2024-0001', '2024-01-15', 1000.00, 0.2100, 210.00, 1210.00, 'CARD', NULL),
(3, 3, 'AG-2024-0002', '2024-02-16', 170.00, 0.2100, 35.70, 205.70, 'TRANSFER', NULL);