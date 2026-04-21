import os
import subprocess
import re

file_path = "src/main/resources/data.sql"

def commit(msg):
    subprocess.run(["git", "add", file_path])
    subprocess.run(["git", "commit", "-m", msg])

with open(file_path, 'r', encoding='utf-8') as f:
    original_sql = f.read()

# COMMIT 1: "feat(data): limpiar vehículos de baja gama del catálogo"
# Eliminar id=9 a 25.
# En el archivo estaban así: "(9, 1, 5, 1, 'Seat', 'Ibiza'..."
sql = re.sub(r'\(9, .*?\(25, .*?\);\n', '', original_sql, flags=re.DOTALL)
sql = sql.replace(",\n\n-- Insertar Reservas", ";\n\n-- Insertar Reservas") # Fix trailing comma if any
# Reemplazar la coma del Aston Martin (id 8) por ;
sql = re.sub(r"('Elegancia inglesa con furia', TRUE),", r"\1;", sql)
sql = sql.replace(",;", ";")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(sql)
commit("feat(data): limpiar vehículos de baja gama del catálogo")

# COMMIT 2: "feat(data): actualizar categorías y descripciones de vehicle_categories"
sql = sql.replace(
    "(5, 'COMPACT', 'Vehículos utilitarios y compactos urbanos'),\n(6, 'SUV', 'Todoterrenos y SUVs familiares');",
    "(6, 'SUV', 'SUVs premium y todoterrenos de ultra-lujo');"
)
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(sql)
commit("feat(data): actualizar categorías y descripciones de vehicle_categories")

# Preparar base de vehicles
# Vamos a actualizar los coches 1-8 y dejarlos ya con precios ajustados.
base_vehicles_pre = """INSERT IGNORE INTO vehicles (id, owner_id, category_id, location_id, brand, model, production_year, price_per_day, engine_type, horse_power, torque_nm, transmission, drivetrain, fuel_type, zero_to_hundred, description, available) VALUES
(1, 1, 1, 1, 'Porsche', '911 Carrera S', 2022, 350.00, 'Boxer 6', 450, 530, 'PDK', 'RWD', 'Gasolina', 3.7, 'El icono indiscutible de la estética old money.', TRUE),
(2, 1, 4, 2, 'BMW', 'M4 Competition', 2023, 280.00, 'I6 Twin-Turbo', 510, 650, 'M Steptronic', 'xDrive', 'Gasolina', 3.5, 'Agresividad alemana en estado puro.', TRUE),
(3, 2, 2, 3, 'Mercedes-AMG', 'GT', 2021, 450.00, 'V8 BiTurbo', 530, 670, 'Speedshift DCT', 'RWD', 'Gasolina', 3.8, 'Un V8 que impone respeto en cada semáforo.', TRUE),
(4, 2, 4, 2, 'Audi', 'RS e-tron GT', 2024, 300.00, 'Eléctrico', 598, 830, 'Direct Drive', 'quattro', 'Eléctrico', 3.3, 'Silencio abismal, aceleración que parte el cuello.', TRUE),
(5, 3, 2, 4, 'Ferrari', 'F8 Tributo', 2020, 800.00, 'V8 Twin-Turbo', 720, 770, 'F1 DCT', 'RWD', 'Gasolina', 2.9, 'La obra cumbre del cavallino rampante.', TRUE),
(6, 3, 2, 4, 'Lamborghini', 'Huracán EVO', 2022, 750.00, 'V10 Atmosférico', 640, 600, 'LDF', 'AWD', 'Gasolina', 2.9, 'El sonido V10 más puro que existe.', TRUE),
(7, 4, 2, 3, 'McLaren', '720S', 2019, 650.00, 'V8 Twin-Turbo', 720, 770, 'SSG', 'RWD', 'Gasolina', 2.9, 'Ingeniería espacial para la carretera.', TRUE),
(8, 4, 1, 1, 'Aston Martin', 'Vantage', 2021, 400.00, 'V8 BiTurbo', 510, 685, 'ZF', 'RWD', 'Gasolina', 3.6, 'La elegancia británica elevada a su máxima potencia.', TRUE),
"""

vehicles_supercar = """(9, 1, 2, 4, 'Lamborghini', 'Urus Performante', 2023, 900.00, 'V8 BiTurbo', 666, 850, 'Automática', 'AWD', 'Gasolina', 3.3, 'Brutalidad y poder absoluto.', TRUE),
(10, 2, 2, 3, 'Ferrari', '296 GTB', 2023, 850.00, 'V6 Hybrid', 830, 740, 'F1 DCT', 'RWD', 'Híbrido', 2.9, 'El renacer híbrido del cavallino.', TRUE),
(11, 3, 2, 4, 'Ferrari', 'Roma Spider', 2024, 750.00, 'V8 Twin-Turbo', 620, 760, 'F1 DCT', 'RWD', 'Gasolina', 3.4, 'La dolce vita al descubierto.', TRUE),
(12, 4, 2, 2, 'McLaren', 'Artura', 2023, 600.00, 'V6 Hybrid', 680, 720, 'SSG', 'RWD', 'Híbrido', 3.0, 'El futuro de Woking hecho realidad.', TRUE),
(13, 1, 2, 1, 'Porsche', '911 GT3 RS', 2023, 900.00, 'Boxer 6', 525, 465, 'PDK', 'RWD', 'Gasolina', 3.2, 'Nacido en el circuito, legal en la calle.', TRUE),
"""

vehicles_gt = """(14, 2, 1, 1, 'Porsche', '911 Carrera 4S', 2023, 380.00, 'Boxer 6', 450, 530, 'PDK', 'AWD', 'Gasolina', 3.6, 'Tracción total para el clásico de Stuttgart.', TRUE),
(15, 3, 1, 2, 'Porsche', '911 Turbo S', 2024, 600.00, 'Boxer 6', 650, 800, 'PDK', 'AWD', 'Gasolina', 2.7, 'La cúspide de las prestaciones usable a diario.', TRUE),
(16, 4, 1, 3, 'Porsche', 'Taycan Turbo S', 2024, 450.00, 'Eléctrico', 761, 1050, 'Automática', 'AWD', 'Eléctrico', 2.8, 'Poder eléctrico que quita la respiración.', TRUE),
(17, 1, 1, 4, 'Aston Martin', 'DB12', 2024, 650.00, 'V8 BiTurbo', 680, 800, 'ZF', 'RWD', 'Gasolina', 3.6, 'Super tourer sin concesiones.', TRUE),
(18, 2, 1, 2, 'Bentley', 'Continental GT Speed', 2023, 700.00, 'W12', 659, 900, 'DCT', 'AWD', 'Gasolina', 3.6, 'Excelencia y opulencia rodante.', TRUE),
(19, 3, 1, 3, 'Jaguar', 'F-Type R', 2022, 350.00, 'V8 Supercharged', 575, 700, 'ZF', 'AWD', 'Gasolina', 3.7, 'Sonido supercharger que hiela la sangre.', TRUE),
"""

vehicles_exec_her_suv = """(20, 1, 4, 2, 'Rolls-Royce', 'Ghost', 2023, 2500.00, 'V12', 571, 850, 'Automática', 'AWD', 'Gasolina', 4.8, 'Deslizándose por el asfalto en puro silencio.', TRUE),
(21, 2, 4, 4, 'Rolls-Royce', 'Cullinan Black Badge', 2024, 2800.00, 'V12', 600, 900, 'Automática', 'AWD', 'Gasolina', 5.2, 'Dominancia insuperable en cualquier terreno.', TRUE),
(22, 3, 4, 2, 'BMW', 'Serie 7 760i xDrive', 2024, 800.00, 'V8', 544, 750, 'Steptronic', 'AWD', 'Gasolina', 4.2, 'La sala VIP más rápida de Munich.', TRUE),
(23, 4, 4, 3, 'Mercedes-Maybach', 'S 680', 2023, 1200.00, 'V12', 612, 900, '9G-Tronic', 'AWD', 'Gasolina', 4.5, 'El máximo lujo terrenal.', TRUE),
(24, 1, 3, 1, 'Porsche', '911 Classic 964 RS', 1992, 400.00, 'Boxer 6', 260, 310, 'Manual', 'RWD', 'Gasolina', 5.3, 'Purismo analógico de otro siglo.', TRUE),
(25, 2, 3, 3, 'Jaguar', 'E-Type Serie 1', 1968, 500.00, 'I6', 269, 353, 'Manual', 'RWD', 'Gasolina', 7.1, 'El coche más hermoso jamás fabricado según Enzo.', TRUE),
(26, 3, 6, 2, 'Range Rover', 'Sport SVR', 2023, 450.00, 'V8 Supercharged', 575, 700, 'Automática', 'AWD', 'Gasolina', 4.5, 'Bramido inconfundible y capacidades absolutas.', TRUE),
(27, 4, 6, 3, 'BMW', 'X6 M Competition', 2024, 430.00, 'V8 BiTurbo', 625, 750, 'M Steptronic', 'AWD', 'Gasolina', 3.8, 'Un mastodonte desafiando las leyes de la física.', TRUE);
"""

# Re-generar regex para limpiar todo el insert actual de vehicles
# Encuentra INSERT INTO vehicles hasta el punto y coma final
sql_without_vehicles = re.sub(r'INSERT IGNORE INTO vehicles \[^\;\]*\;', '', sql, flags=re.DOTALL)
# Pero como lo eliminé, lo vuelvo a reconstruir limpiamente a mano con splits:

parts = sql.split('-- Insertar Vehículos (con category_id y location_id)')
part1 = parts[0]
part2 = parts[1].split('-- Insertar Reservas')[1]

# COMMIT 3: "feat(data): añadir flota SUPERCAR a data.sql"
current_insert = base_vehicles_pre + vehicles_supercar
current_sql = part1 + "-- Insertar Vehículos (con category_id y location_id)\n" + current_insert.rstrip(",\n") + ";\n\n-- Insertar Reservas" + part2

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(current_sql)
commit("feat(data): añadir flota SUPERCAR a data.sql")

# COMMIT 4: "feat(data): añadir flota GRAN_TURISMO a data.sql"
current_insert += vehicles_gt
current_sql = part1 + "-- Insertar Vehículos (con category_id y location_id)\n" + current_insert.rstrip(",\n") + ";\n\n-- Insertar Reservas" + part2

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(current_sql)
commit("feat(data): añadir flota GRAN_TURISMO a data.sql")

# COMMIT 5: "feat(data): añadir flota EXECUTIVE y HERITAGE a data.sql"
current_insert += vehicles_exec_her_suv
current_sql = part1 + "-- Insertar Vehículos (con category_id y location_id)\n" + current_insert.rstrip(",\n") + ";\n\n-- Insertar Reservas" + part2

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(current_sql)
commit("feat(data): añadir flota EXECUTIVE y HERITAGE a data.sql")
