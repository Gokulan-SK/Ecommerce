-- ========================================================
-- SEED DATA FOR E-COMMERCE LOG GENERATOR
-- ========================================================

-- ========================================================
-- USERS DATA
-- Password: 'admin' and 'user' (BCrypt hashed)
-- BCrypt rounds: 10
-- ========================================================
INSERT INTO users (username, password, role) VALUES
('admin', '$2a$10$srPiHRjQdxfqGOtwhBGsTeWKRiOCVLyRs9B2FpPsdQZs9GVult8Ci', 'ADMIN'),
('user', '$2a$10$KfsytE8rBYbb558ubErYVOuG.g2SAMu2wz5VIu13i7KpjUEJ09ekS', 'USER');

-- ========================================================
-- PRODUCTS DATA (50+ Products)
-- Realistic e-commerce product catalog with varied pricing
-- ========================================================

-- Electronics
INSERT INTO products (name, description, price, stock) VALUES
('Wireless Bluetooth Headphones', 'Premium noise-cancelling over-ear headphones with 30-hour battery life', 89.99, 50),
('Gaming Mouse RGB', 'Ergonomic gaming mouse with 16000 DPI and customizable RGB lighting', 49.99, 100),
('Mechanical Keyboard', 'Cherry MX Blue switches, RGB backlight, full-size gaming keyboard', 129.99, 75),
('4K Webcam', 'Ultra HD webcam with auto-focus and built-in microphone', 79.99, 60),
('USB-C Hub 7-in-1', 'Multi-port adapter with HDMI, USB 3.0, SD card reader', 34.99, 150),
('Portable SSD 1TB', 'External solid state drive with 550MB/s transfer speed', 119.99, 40),
('Wireless Charger Pad', 'Fast charging pad compatible with Qi-enabled devices', 24.99, 200),
('Smart Watch Fitness Tracker', 'Heart rate monitor, GPS, waterproof fitness smartwatch', 199.99, 80),
('Laptop Stand Aluminum', 'Adjustable ergonomic laptop stand with cooling design', 39.99, 120),
('Desktop Monitor 27"', 'QHD 2560x1440 IPS display with 144Hz refresh rate', 299.99, 30);

-- Home & Office
INSERT INTO products (name, description, price, stock) VALUES
('Ergonomic Office Chair', 'Mesh back executive chair with lumbar support and adjustable armrests', 249.99, 25),
('Standing Desk Electric', 'Height-adjustable standing desk with memory preset', 399.99, 15),
('LED Desk Lamp', 'Touch control dimmable LED lamp with USB charging port', 29.99, 100),
('Wireless Printer Scanner', 'All-in-one color inkjet printer with WiFi connectivity', 149.99, 35),
('Paper Shredder', 'Cross-cut shredder for personal and office use', 59.99, 45),
('Desk Organizer Set', 'Bamboo desktop organizer with multiple compartments', 19.99, 150),
('Whiteboard 48x36', 'Magnetic dry-erase board with aluminum frame', 44.99, 50),
('Cable Management Box', 'Large cable organizer box to hide power strips and wires', 16.99, 200),
('Bookshelf 5-Tier', 'Modern ladder-style bookshelf with metal frame', 79.99, 40),
('Floor Mat Anti-Fatigue', 'Cushioned standing desk mat for comfort', 34.99, 75);

-- Books & Stationery
INSERT INTO products (name, description, price, stock) VALUES
('Notebook Hardcover A5', 'Premium leather-bound journal with 200 pages', 14.99, 300),
('Fountain Pen Set', 'Professional calligraphy pen set with 6 nibs', 49.99, 60),
('Sticky Notes Assorted', 'Colorful sticky note pack - 12 pads, 100 sheets each', 8.99, 500),
('Planner 2026 Weekly', 'Productivity planner with goal-setting templates', 24.99, 150),
('Highlighter Markers', 'Pack of 12 vibrant chisel-tip highlighters', 11.99, 250),
('Binder Clips Assorted', 'Office binder clips in various sizes - 120 pack', 7.99, 400),
('Index Cards 500ct', 'Ruled white index cards 3x5 inches', 9.99, 200),
('Stapler Heavy Duty', 'Metal construction stapler for up to 100 sheets', 21.99, 80),
('Pencil Case Canvas', 'Large zippered pencil pouch with compartments', 12.99, 180),
('Desk Calendar 2026', 'Monthly desk pad calendar with note space', 13.99, 120);

-- Kitchen & Dining
INSERT INTO products (name, description, price, stock) VALUES
('Stainless Steel Water Bottle', 'Insulated 32oz bottle keeps drinks cold for 24 hours', 27.99, 250),
('Coffee Maker Programmable', '12-cup drip coffee maker with auto-brew feature', 69.99, 55),
('Blender High-Speed', '1500W professional blender with multiple settings', 89.99, 45),
('Air Fryer 6-Quart', 'Digital air fryer with 8 preset cooking functions', 109.99, 60),
('Non-Stick Cookware Set', '10-piece ceramic non-stick pots and pans set', 149.99, 35),
('Kitchen Knife Set', 'Professional 15-piece knife block set with sharpener', 79.99, 50),
('Food Storage Containers', 'Glass meal prep containers with locking lids - 10 pack', 34.99, 100),
('Electric Kettle', 'Rapid boil stainless steel kettle with auto shut-off', 39.99, 80),
('Cutting Board Set', 'Bamboo cutting boards - 3 sizes with juice grooves', 29.99, 120),
('Spice Rack Organizer', 'Rotating spice rack with 20 glass jars included', 44.99, 70);

-- Sports & Fitness
INSERT INTO products (name, description, price, stock) VALUES
('Yoga Mat Extra Thick', '6mm eco-friendly TPE yoga mat with carrying strap', 34.99, 150),
('Resistance Bands Set', '5 resistance levels with handles and door anchor', 24.99, 200),
('Dumbbell Set Adjustable', 'Quick-adjust dumbbell pair 5-52.5 lbs each', 299.99, 25),
('Jump Rope Speed', 'Professional tangle-free speed rope with ball bearings', 14.99, 180),
('Gym Bag Duffel', 'Large sports duffel with shoe compartment and wet pocket', 39.99, 90),
('Foam Roller', 'High-density foam roller for muscle recovery', 19.99, 140),
('Water Resistant Running Belt', 'Adjustable waist pack for phone and essentials', 16.99, 160),
('Workout Gloves', 'Padded weight lifting gloves with wrist support', 21.99, 110),
('Protein Shaker Bottle', 'BPA-free shaker with mixing ball - 28oz', 11.99, 300),
('Fitness Tracker Band', 'Activity tracker with sleep monitoring and notifications', 49.99, 100);

-- Personal Care
INSERT INTO products (name, description, price, stock) VALUES
('Electric Toothbrush', 'Rechargeable sonic toothbrush with 5 modes and timer', 59.99, 70),
('Hair Dryer Ionic', '1875W professional hair dryer with diffuser attachment', 44.99, 85),
('Massage Gun Deep Tissue', 'Percussion massager with 6 heads and adjustable speeds', 89.99, 50),
('Humidifier Ultrasonic', 'Cool mist humidifier for bedroom with essential oil tray', 39.99, 95),
('Bathroom Scale Digital', 'High-precision body weight scale with LCD display', 24.99, 120);

-- Special Chaos Testing Product
INSERT INTO products (name, description, price, stock) VALUES
('heavy-query', 'Special product that triggers DB latency simulation for testing', 999.99, 1);
