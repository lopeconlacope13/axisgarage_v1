
INSERT INTO propietario (id, nombre, apellidos, email, telefono) VALUES
(1, 'Manolo', 'López', 'manolo@lopebnb.com', '600111222'),
(2, 'Lucía', 'García', 'lucia@lopebnb.com', '600333444'),
(3, 'Antonio', 'Recio', 'antonio@lopebnb.com', '600555666'),
(4, 'Elena', 'Nito', 'elena@lopebnb.com', '600777888');


INSERT INTO huesped (id, dni, nombre, apellidos, email, telefono) VALUES
(1, '12345678A', 'Carlos', 'Viajero', 'carlos@gmail.com', '611000111'),
(2, '87654321B', 'Ana', 'Turista', 'ana@gmail.es', '611000222'),
(3, '11223344C', 'David', 'Mochilero', 'david@gmail.com', '611000333'),
(4, '55667788D', 'Sofía', 'Exploradora', 'sofia@gmail.com', '611000444'),
(5, '99887766E', 'Miguel', 'Aventurero', 'miguel@gmail.com', '611000555');


INSERT INTO casa_rural (id, nombre, direccion, precio_noche, capacidad_personas, propietario_id) VALUES
(1, 'Cortijo El Olivo', 'Ctra. Sierra Nevada km 4', 120.00, 6, 1),
(2, 'Casa La Encina', 'Plaza del Pueblo 1, Cazorla', 85.00, 4, 1),
(3, 'Villa Relax', 'Camino del Río s/n, Grazalema', 200.00, 10, 2),
(4, 'Cabaña del Bosque', 'Sendero Perdido 7, Pirineos', 150.00, 5, 2),
(5, 'Finca Los Almendros', 'Calle Mayor 23, Toledo', 95.00, 8, 3),
(6, 'Apartamento Rural Sol', 'Costa de la Luz 12, Cádiz', 110.00, 3, 3),
(7, 'El Mirador de Gredos', 'Alto de la Montaña, Ávila', 180.00, 12, 4),
(8, 'Caserío Vasco', 'Barrio Verde 4, San Sebastián', 250.00, 15, 4);

-- Reservas Pasadas ¡
INSERT INTO reserva (fecha_entrada, fecha_salida, importe_total, casa_rural_id, huesped_id) VALUES
('2023-12-01', '2023-12-05', 480.00, 1, 1),   -- 4 días * 120€
('2024-01-10', '2024-01-15', 1000.00, 3, 2),  -- 5 días * 200€
('2024-02-14', '2024-02-16', 170.00, 2, 3),   -- 2 días * 85€
('2024-03-20', '2024-03-25', 750.00, 4, 4);   -- 5 días * 150€

-- Reservas Futuras (Próximas)
INSERT INTO reserva (fecha_entrada, fecha_salida, importe_total, casa_rural_id, huesped_id) VALUES
('2025-06-01', '2025-06-10', 855.00, 5, 1),   -- 9 días * 95€
('2025-07-15', '2025-07-20', 550.00, 6, 2),   -- 5 días * 110€
('2025-08-01', '2025-08-15', 2520.00, 7, 5),  -- 14 días * 180€
('2025-09-10', '2025-09-12', 500.00, 8, 3),   -- 2 días * 250€
('2025-10-05', '2025-10-10', 600.00, 1, 4),   -- 5 días * 120€
('2025-12-24', '2025-12-31', 1400.00, 3, 1);  -- 7 días * 200€

INSERT INTO opinion (puntuacion, comentario, casa_rural_id, huesped_id) VALUES
(5, 'Una villa espectacular, la piscina es increíble. Repetiré seguro.', 3, 2),
(4, 'El sitio es muy bonito, pero hacía un poco de frío por la noche.', 1, 1),
(5, 'Perfecto para desconectar. Silencio absoluto y vistas preciosas.', 4, 4),
(3, 'La casa está bien, pero el wifi no funcionaba muy rápido.', 2, 3),
(5, 'El dueño, Antonio, es majísimo. Nos regaló una botella de vino.', 5, 1),
(4, 'Muy cerca de la playa, ubicación inmejorable.', 6, 2);