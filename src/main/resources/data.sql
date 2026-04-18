-- Insertar datos de ejemplo para 'roles'
INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_MANAGER'), (3, 'ROLE_USER');

-- Insertar datos de ejemplo para 'users'
INSERT IGNORE INTO users (id, username, email, password, enabled, first_name, last_name, image, created_date, last_modified_date, last_password_change_date)
VALUES (1, 'admin', 'admin@axisgarage.com', '$2b$12$FVRijCavVZ7Qt15.CQssHe9m/6eLAdjAv0PiOKFIjMU161wApxzye', true, 'Admin', 'User', '/images/admin.jpg', NOW(), NOW(), NOW()),
       (2, 'manager', 'manager@axisgarage.com', '$2b$12$FVRijCavVZ7Qt15.CQssHe9m/6eLAdjAv0PiOKFIjMU161wApxzye', true, 'Manager', 'User', '/images/manager.jpg', NOW(), NOW(), NOW()),
       (3, 'normal', 'normal@axisgarage.com', '$2b$12$FVRijCavVZ7Qt15.CQssHe9m/6eLAdjAv0PiOKFIjMU161wApxzye', true, 'Regular', 'User', '/images/user.jpg', NOW(), NOW(), NOW());

-- Asignar roles a usuarios
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1, 1), (2, 2), (3, 3);

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
INSERT IGNORE INTO renters (id, name, last_name, email, dni, phone) VALUES
(1, 'Carlos', 'Viajero', 'carlos@gmail.com', '12345678A', '611000111'),
(2, 'Ana', 'Turista', 'ana@gmail.es', '87654321B', '611000222'),
(3, 'David', 'Mochilero', 'david@gmail.com', '11223344C', '611000333'),
(4, 'Sofía', 'Exploradora', 'sofia@gmail.com', '55667788D', '611000444'),
(5, 'Miguel', 'Aventurero', 'miguel@gmail.com', '99887766E', '611000555');

-- Insertar Vehículos (con category_id y location_id)
INSERT IGNORE INTO vehicles (id, owner_id, category_id, location_id, brand, model, production_year, price_per_day, engine_type, horse_power, torque_nm, transmission, drivetrain, fuel_type, zero_to_hundred, description, available) VALUES
(1, 1, 1, 1, 'Porsche', '911 Carrera S', 2022, 350.00, 'Boxer 6', 450, 530, 'PDK', 'RWD', 'Gasolina', 3.7, 'Precisión quirúrgica de Stuttgart. Su tracción trasera y motor bóxer desnudan el asfalto. La pureza de conducción convertida en arte.', TRUE),
(2, 1, 4, 2, 'BMW', 'M4 Competition', 2023, 280.00, 'I6 Twin-Turbo', 510, 650, 'M Steptronic', 'xDrive', 'Gasolina', 3.5, 'Fuerza bruta bajo control. Un chasis concebido para deslizar y un motor biturbo que entrega la potencia con violencia y precisión alemana.', TRUE),
(3, 2, 2, 3, 'Mercedes-AMG', 'GT', 2021, 450.00, 'V8 BiTurbo', 530, 670, 'Speedshift DCT', 'RWD', 'Gasolina', 3.8, 'El rugido de un V8 artesanal que despierta pasiones. Capó infinito y proporciones clásicas para devorar kilómetros con aplomo y contundencia.', TRUE),
(4, 2, 4, 2, 'Audi', 'RS e-tron GT', 2024, 300.00, 'Eléctrico', 598, 830, 'Direct Drive', 'quattro', 'Eléctrico', 3.3, 'Una nave espacial en silencio. Entrega de par inmediata y tracción total inteligente. Mueve el tiempo y el espacio sin derramar una gota de sudor.', TRUE),
(5, 3, 2, 4, 'Ferrari', 'F8 Tributo', 2020, 800.00, 'V8 Twin-Turbo', 720, 770, 'F1 DCT', 'RWD', 'Gasolina', 2.9, 'El canto de cisne del V8 italiano. Aerodinámica esculpida por el viento y un corazón de Maranello que ruge hasta las 8.000 vueltas por minuto.', TRUE),
(6, 3, 2, 4, 'Lamborghini', 'Huracán EVO', 2022, 750.00, 'V10 Atmosférico', 640, 600, 'LDF', 'AWD', 'Gasolina', 2.9, 'Concebido para desatar el infierno. Un motor V10 atmosférico que corta la respiración y un diseño radical que acapara todas las miradas.', TRUE),
(7, 4, 2, 3, 'McLaren', '720S', 2019, 650.00, 'V8 Twin-Turbo', 720, 770, 'SSG', 'RWD', 'Gasolina', 2.9, 'Ingeniería alienígena al servicio del conductor. Un peso pluma con 720 caballos que desafía constantemente las leyes de la física y el tiempo.', TRUE),
(8, 4, 1, 1, 'Aston Martin', 'Vantage', 2021, 400.00, 'V8 BiTurbo', 510, 685, 'ZF', 'RWD', 'Gasolina', 3.6, 'La brutalidad vestida de esmoquin. Su escultural diseño esconde un V8 que entrega una experiencia de conducción salvajemente refinada.', TRUE),
(9, 1, 2, 4, 'Lamborghini', 'Urus Performante', 2023, 900.00, 'V8 BiTurbo', 666, 850, 'Automática', 'AWD', 'Gasolina', 3.3, 'El demonio disfrazado de todoterreno. Dinámica de superdeportivo puro con espacio para cruzarte el continente a velocidades inconfesables.', TRUE),
(10, 2, 2, 3, 'Ferrari', '296 GTB', 2023, 850.00, 'V6 Hybrid', 830, 740, 'F1 DCT', 'RWD', 'Híbrido', 2.9, 'El renacer eléctrico del Cavallino. Su V6 híbrido ofrece un empuje tan brutal y constante que redefine el concepto de superdeportivo moderno.', TRUE),
(11, 3, 2, 4, 'Ferrari', 'Roma Spider', 2024, 750.00, 'V8 Twin-Turbo', 620, 760, 'F1 DCT', 'RWD', 'Gasolina', 3.4, 'La Dolce Vita sin techo. Líneas limpias, capota de lona tradicional y un corazón V8 que transforma cada paseo por la costa en una película.', TRUE),
(12, 4, 2, 2, 'McLaren', 'Artura', 2023, 600.00, 'V6 Hybrid', 680, 720, 'SSG', 'RWD', 'Híbrido', 3.0, 'Adelantado a su tiempo. Arquitectura ultraligera y electrificación se combinan para ofrecer una respuesta instantánea y letal en carretera.', TRUE),
(13, 1, 2, 1, 'Porsche', '911 GT3 RS', 2023, 900.00, 'Boxer 6', 525, 465, 'PDK', 'RWD', 'Gasolina', 3.2, 'Nacido en la pista, homologado por cortesía. Aerodinámica activa masiva y un motor atmosférico que te exige la perfección en cada vértice.', TRUE),
(14, 2, 1, 1, 'Porsche', '911 Carrera 4S', 2023, 380.00, 'Boxer 6', 450, 530, 'PDK', 'AWD', 'Gasolina', 3.6, 'El deportivo definitivo para cualquier situación. Su tracción integral garantiza que cada uno de sus 450 cv se agarre al asfalto como un imán.', TRUE),
(15, 3, 1, 2, 'Porsche', '911 Turbo S', 2024, 600.00, 'Boxer 6', 650, 800, 'PDK', 'AWD', 'Gasolina', 2.7, 'El cazagigantes por excelencia. Aceleración que distorsiona la vista combinada con la docilidad de un coche utilitario si así lo deseas.', TRUE),
(16, 4, 1, 3, 'Porsche', 'Taycan Turbo S', 2024, 450.00, 'Eléctrico', 761, 1050, 'Automática', 'AWD', 'Eléctrico', 2.8, 'Fuerza G sin piedad. El pináculo del lujo eléctrico que te pega al asiento en absoluto silencio, demostrando que el futuro ya nos ha arrollado.', TRUE),
(17, 1, 1, 4, 'Aston Martin', 'DB12', 2024, 650.00, 'V8 BiTurbo', 680, 800, 'ZF', 'RWD', 'Gasolina', 3.6, 'El primer Super Tourer del mundo. Lujo superlativo en el interior y una entrega de potencia monumental que conquista continentes al amanecer.', TRUE),
(18, 2, 1, 2, 'Bentley', 'Continental GT Speed', 2023, 700.00, 'W12', 659, 900, 'DCT', 'AWD', 'Gasolina', 3.6, 'Opulencia rodante a 330 km/h. Materiales nobles trabajados a mano y un W12 inagotable para cruzar Europa con una clase y porte inigualables.', TRUE),
(19, 3, 1, 3, 'Jaguar', 'F-Type R', 2022, 350.00, 'V8 Supercharged', 575, 700, 'ZF', 'AWD', 'Gasolina', 3.7, 'El último de su estirpe. Un V8 sobrealimentado cuyo bramido gutural rinde homenaje a los años dorados del automovilismo británico purasangre.', TRUE),
(20, 1, 4, 2, 'Rolls-Royce', 'Ghost', 2023, 2500.00, 'V12', 571, 850, 'Automática', 'AWD', 'Gasolina', 4.8, 'Deslizándose sobre la calzada en mutismo absoluto. La máxima expresión de riqueza, minuciosamente diseñada para aislarte del mundo terrenal.', TRUE),
(21, 2, 4, 4, 'Rolls-Royce', 'Cullinan Black Badge', 2024, 2800.00, 'V12', 600, 900, 'Automática', 'AWD', 'Gasolina', 5.2, 'Dominio soberano sin importar la latitud. Porque estar en la cima del mundo requiere un trono inexpugnable e irreverente, vayas donde vayas.', TRUE),
(22, 3, 4, 2, 'BMW', 'Serie 7 760i xDrive', 2024, 800.00, 'V8', 544, 750, 'Steptronic', 'AWD', 'Gasolina', 4.2, 'La sala de juntas más rápida del planeta. Lujo bávaro, tecnología escandalosa y la suavidad inmensa y señorial de un exquisito bloque V8.', TRUE),
(23, 4, 4, 3, 'Mercedes-Maybach', 'S 680', 2023, 1200.00, 'V12', 612, 900, '9G-Tronic', 'AWD', 'Gasolina', 4.5, 'El lujo llevado al delirio. Su inmenso motor V12 mueve esta suite de primera clase con un refinamiento que roza lo espiritual e intocable.', TRUE),
(24, 1, 3, 1, 'Porsche', '911 Classic 964 RS', 1992, 400.00, 'Boxer 6', 260, 310, 'Manual', 'RWD', 'Gasolina', 5.3, 'Analógico, crudo, real. Sin filtros electrónicos que intervengan entre el asfalto, el piloto y su mítico motor refrigerado por aire.', TRUE),
(25, 2, 3, 3, 'Jaguar', 'E-Type Serie 1', 1968, 500.00, 'I6', 269, 353, 'Manual', 'RWD', 'Gasolina', 7.1, 'La leyenda inofensiva al tiempo. El propio Enzo Ferrari cruzó la frontera solo para admitir que este era el coche más hermoso jamás concebido.', TRUE),
(26, 3, 6, 2, 'Range Rover', 'Sport SVR', 2023, 450.00, 'V8 Supercharged', 575, 700, 'Automática', 'AWD', 'Gasolina', 4.5, 'Un tanque de asalto vestido de etiqueta. Presencia implacable, confort real y un aullido de V8 sobrealimentado que derriba cualquier complejo.', TRUE),
(27, 4, 6, 3, 'BMW', 'X6 M Competition', 2024, 430.00, 'V8 BiTurbo', 625, 750, 'M Steptronic', 'AWD', 'Gasolina', 3.8, 'Física desafiada. Un coloso capaz de humillar a deportivos en circuito mientras te envuelve en  cuero, carbono y agresividad desenfrenada.', TRUE);;

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