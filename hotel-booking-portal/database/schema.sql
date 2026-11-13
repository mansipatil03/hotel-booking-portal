CREATE TABLE IF NOT EXISTS hotels (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  location VARCHAR(80) NOT NULL,
  description TEXT NOT NULL,
  amenities VARCHAR(255) NOT NULL,
  price_per_night DECIMAL(10,2) NOT NULL,
  total_rooms INT NOT NULL,
  CONSTRAINT chk_price CHECK (price_per_night > 0),
  CONSTRAINT chk_rooms CHECK (total_rooms > 0),
  INDEX idx_hotels_location (location)
);

CREATE TABLE IF NOT EXISTS bookings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reference VARCHAR(20) NOT NULL,
  hotel_id BIGINT NOT NULL,
  guest_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  check_in DATE NOT NULL,
  check_out DATE NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_booking_reference UNIQUE (reference),
  CONSTRAINT fk_booking_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
  CONSTRAINT chk_dates CHECK (check_out > check_in),
  INDEX idx_booking_lookup (hotel_id, status, check_in, check_out)
);
