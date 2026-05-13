    SET FOREIGN_KEY_CHECKS = 0;

    -- BORRADO DE TABLAS
    DROP TABLE IF EXISTS invoices;
    DROP TABLE IF EXISTS damage_reports;
    DROP TABLE IF EXISTS coverages;
    DROP TABLE IF EXISTS reviews;
    DROP TABLE IF EXISTS reservations;
    DROP TABLE IF EXISTS vehicle_images;
    DROP TABLE IF EXISTS vehicles;
    DROP TABLE IF EXISTS renters;
    DROP TABLE IF EXISTS owners;
    DROP TABLE IF EXISTS vehicle_categories;
    DROP TABLE IF EXISTS locations;
    DROP TABLE IF EXISTS user_roles;
    DROP TABLE IF EXISTS roles;
    DROP TABLE IF EXISTS users;

    -- 1. TABLA LOCATIONS (Sedes) --
    CREATE TABLE locations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        city VARCHAR(100) NOT NULL,
        address VARCHAR(255) NOT NULL,
        postal_code VARCHAR(10),
        country VARCHAR(50) NOT NULL DEFAULT 'España',
        phone VARCHAR(20),
        email VARCHAR(100)
    );

    -- 2. TABLA VEHICLE_CATEGORIES (Categorías de vehículos) --
    CREATE TABLE vehicle_categories (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(50) NOT NULL UNIQUE,
        description VARCHAR(255)
    );

    -- 3. TABLA OWNERS (Propietarios) --
    CREATE TABLE owners (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        last_name VARCHAR(100) NOT NULL,
        email VARCHAR(100) NOT NULL UNIQUE,
        phone VARCHAR(20) NOT NULL UNIQUE
    );

    -- 4. TABLA RENTERS (Pilotos / Clientes) --
    CREATE TABLE renters (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        last_name VARCHAR(100) NOT NULL,
        email VARCHAR(100) NOT NULL UNIQUE,
        dni VARCHAR(20) NOT NULL UNIQUE,
        phone VARCHAR(20) NOT NULL UNIQUE,
        address VARCHAR(255)
    );

    -- 5. TABLA VEHICLES --
    CREATE TABLE vehicles (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        owner_id BIGINT NOT NULL,
        category_id BIGINT NOT NULL,
        location_id BIGINT NOT NULL,

        brand VARCHAR(50) NOT NULL,
        model VARCHAR(100) NOT NULL,
        production_year INT NOT NULL,
        price_per_day DECIMAL(10,2) NOT NULL,

        engine_type VARCHAR(50) NOT NULL,
        horse_power INT NOT NULL,
        torque_nm INT NOT NULL,
        transmission VARCHAR(50) NOT NULL,
        drivetrain VARCHAR(20) NOT NULL,
        fuel_type VARCHAR(20) NOT NULL,
        zero_to_hundred DECIMAL(3,1),

        description TEXT,
        available BOOLEAN DEFAULT TRUE,

        CONSTRAINT fk_vehicle_owner FOREIGN KEY (owner_id) REFERENCES owners(id),
        CONSTRAINT fk_vehicle_category FOREIGN KEY (category_id) REFERENCES vehicle_categories(id),
        CONSTRAINT fk_vehicle_location FOREIGN KEY (location_id) REFERENCES locations(id)
    );

    -- 6. TABLA VEHICLE_IMAGES (colección de imágenes por vehículo) --
    CREATE TABLE vehicle_images (
        vehicle_id BIGINT NOT NULL,
        image_url VARCHAR(255),
        FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
    );

    -- 7. TABLA RESERVATIONS --
    CREATE TABLE reservations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        vehicle_id BIGINT NOT NULL,
        renter_id BIGINT NOT NULL,

        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        total_price DECIMAL(10,2) NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

        CONSTRAINT fk_res_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
        CONSTRAINT fk_res_renter FOREIGN KEY (renter_id) REFERENCES renters(id)
    );

    -- 8. TABLA COVERAGES (Coberturas de seguro) --
    CREATE TABLE coverages (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        reservation_id BIGINT NOT NULL,
        type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
        total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
        CONSTRAINT fk_coverage_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
    );

    -- 9. TABLA DAMAGE_REPORTS (Informes de daños PRE/POST alquiler) --
    CREATE TABLE damage_reports (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        reservation_id BIGINT NOT NULL,
        type VARCHAR(10) NOT NULL,
        description TEXT NOT NULL,
        reported_date DATE NOT NULL,
        image_url VARCHAR(255),
        CONSTRAINT fk_damage_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
    );

    -- 10. TABLA REVIEWS --
    CREATE TABLE reviews (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        reservation_id BIGINT NOT NULL,
        renter_id BIGINT NOT NULL,

        rating INT NOT NULL,
        comment TEXT,

        CONSTRAINT fk_review_res FOREIGN KEY (reservation_id) REFERENCES reservations(id),
        CONSTRAINT fk_review_renter FOREIGN KEY (renter_id) REFERENCES renters(id)
    );

    -- 11. TABLA INVOICES (Facturas de reservas confirmadas) --
    CREATE TABLE invoices (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        reservation_id BIGINT NOT NULL UNIQUE,
        invoice_number VARCHAR(20) NOT NULL UNIQUE,
        issue_date DATE NOT NULL,
        base_amount DECIMAL(10,2) NOT NULL,
        tax_rate DECIMAL(5,4) NOT NULL DEFAULT 0.2100,
        tax_amount DECIMAL(10,2) NOT NULL,
        total_amount DECIMAL(10,2) NOT NULL,
        payment_method VARCHAR(20),
        notes TEXT,
        CONSTRAINT fk_invoice_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
    );

    -- 12. TABLAS DE SEGURIDAD --
    CREATE TABLE IF NOT EXISTS users (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(50) UNIQUE NOT NULL,
        email VARCHAR(100) UNIQUE NOT NULL,
        password VARCHAR(100),
        auth_provider VARCHAR(20) DEFAULT 'LOCAL',
        enabled BOOLEAN NOT NULL,
        first_name VARCHAR(50) NOT NULL,
        last_name VARCHAR(50) NOT NULL,
        image VARCHAR(255),
        created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        reset_token VARCHAR(255),
        reset_token_expiry DATETIME
    );

    CREATE TABLE IF NOT EXISTS roles (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(50) UNIQUE NOT NULL
    );

    CREATE TABLE IF NOT EXISTS user_roles (
        user_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        PRIMARY KEY (user_id, role_id),
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );

    SET FOREIGN_KEY_CHECKS = 1;