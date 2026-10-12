-- V6: Seed Categories, Products, Demo Users, Settings, Coupons
-- Demo Users (Password: Password@123)
-- BCrypt hash: $2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.
INSERT INTO users (id, email, password_hash, full_name, role, is_active, created_at, updated_at) VALUES
(X'00000000000000000000000000000001', 'demo@basketly.app', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Aarav Sharma', 'USER', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'00000000000000000000000000000002', 'test@basketly.app', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Diya Patel', 'USER', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'00000000000000000000000000000003', 'admin@basketly.app', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Basketly Admin', 'ADMIN', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP(6);

-- User Settings
INSERT INTO user_settings (user_id, weekly_budget, monthly_budget, week_start_day, timezone, email_notifications, budget_alerts, created_at, updated_at) VALUES
(X'00000000000000000000000000000001', 4500.00, 18000.00, 'MONDAY', 'Asia/Kolkata', TRUE, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'00000000000000000000000000000002', 3500.00, 14000.00, 'MONDAY', 'Asia/Kolkata', TRUE, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'00000000000000000000000000000003', 10000.00, 40000.00, 'MONDAY', 'Asia/Kolkata', TRUE, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP(6);

-- Default Addresses
INSERT INTO addresses (id, user_id, label, recipient_name, phone, street, city, state, postal_code, is_default, created_at, updated_at) VALUES
(X'00000000000000000000000000000011', X'00000000000000000000000000000001', 'Home', 'Aarav Sharma', '+91 9876543210', 'Flat 402, Green Glen Layout, Bellandur', 'Bengaluru', 'Karnataka', '560103', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'00000000000000000000000000000012', X'00000000000000000000000000000001', 'Office', 'Aarav Sharma', '+91 9876543210', 'Prestige Tech Park, Marathahalli Ring Rd', 'Bengaluru', 'Karnataka', '560103', FALSE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'00000000000000000000000000000013', X'00000000000000000000000000000002', 'Home', 'Diya Patel', '+91 9812345678', 'Plot 12, Indiranagar 100ft Road', 'Bengaluru', 'Karnataka', '560038', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP(6);

-- Categories
INSERT INTO categories (id, name, slug, icon, display_order, created_at) VALUES
(X'10000000000000000000000000000001', 'Dairy and Eggs', 'dairy-and-eggs', 'Milk', 1, CURRENT_TIMESTAMP(6)),
(X'10000000000000000000000000000002', 'Fruits', 'fruits', 'Apple', 2, CURRENT_TIMESTAMP(6)),
(X'10000000000000000000000000000003', 'Vegetables', 'vegetables', 'Carrot', 3, CURRENT_TIMESTAMP(6)),
(X'10000000000000000000000000000004', 'Bakery', 'bakery', 'Croissant', 4, CURRENT_TIMESTAMP(6)),
(X'10000000000000000000000000000005', 'Rice, Grains and Pulses', 'rice-grains-and-pulses', 'Wheat', 5, CURRENT_TIMESTAMP(6)),
(X'10000000000000000000000000000006', 'Meat and Seafood', 'meat-and-seafood', 'Fish', 6, CURRENT_TIMESTAMP(6)),
(X'10000000000000000000000000000007', 'Snacks', 'snacks', 'Cookie', 7, CURRENT_TIMESTAMP(6)),
(X'10000000000000000000000000000008', 'Beverages', 'beverages', 'Coffee', 8, CURRENT_TIMESTAMP(6)),
(X'10000000000000000000000000000009', 'Household Essentials', 'household-essentials', 'Sparkles', 9, CURRENT_TIMESTAMP(6)),
(X'10000000000000000000000000000010', 'Frozen Foods', 'frozen-foods', 'Snowflake', 10, CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE display_order = VALUES(display_order);

-- Products (50 items with realistic INR prices)
INSERT INTO products (id, category_id, name, normalized_name, description, price, package_size, unit, image_url, is_available, stock_quantity, version, created_at, updated_at) VALUES
-- Dairy and Eggs
(X'20000000000000000000000000000001', X'10000000000000000000000000000001', 'Amul Taaza Homogenised Toned Milk', 'amul taaza homogenised toned milk', 'Fresh and nutritious toned milk packet', 32.00, '500 ml', 'pack', 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=500&auto=format&fit=crop&q=60', TRUE, 120, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000002', X'10000000000000000000000000000001', 'Farm Fresh White Eggs (Pack of 12)', 'farm fresh white eggs pack of 12', 'Clean, protein-rich farm eggs', 96.00, '12 pcs', 'tray', 'https://images.unsplash.com/photo-1516448620398-c5f44bf9f441?w=500&auto=format&fit=crop&q=60', TRUE, 85, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000003', X'10000000000000000000000000000001', 'Amul Salted Butter', 'amul salted butter', 'Classic pasteurized salted butter block', 58.00, '100 g', 'box', 'https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?w=500&auto=format&fit=crop&q=60', TRUE, 90, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000004', X'10000000000000000000000000000001', 'Milky Mist Fresh Paneer Block', 'milky mist fresh paneer block', 'Soft, tender cottage cheese', 115.00, '200 g', 'pack', 'https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=500&auto=format&fit=crop&q=60', TRUE, 50, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000005', X'10000000000000000000000000000001', 'Mother Dairy Classic Dahi / Curd', 'mother dairy classic dahi curd', 'Thick, creamy, wholesome probiotic curd', 45.00, '400 g', 'cup', 'https://images.unsplash.com/photo-1571212515416-fef01fc43637?w=500&auto=format&fit=crop&q=60', TRUE, 70, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

-- Fruits
(X'20000000000000000000000000000006', X'10000000000000000000000000000002', 'Shimla Royal Delicious Apples', 'shimla royal delicious apples', 'Crisp, sweet, handpicked mountain apples', 180.00, '1 kg', 'kg', 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500&auto=format&fit=crop&q=60', TRUE, 60, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000007', X'10000000000000000000000000000002', 'Robusta Cavendish Bananas', 'robusta cavendish bananas', 'Fresh, energetic yellow bananas', 48.00, '1 kg', 'kg', 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=500&auto=format&fit=crop&q=60', TRUE, 110, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000008', X'10000000000000000000000000000002', 'Nagpur Sweet Oranges', 'nagpur sweet oranges', 'Juicy, tangy citrus oranges rich in Vitamin C', 95.00, '1 kg', 'kg', 'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=500&auto=format&fit=crop&q=60', TRUE, 45, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000009', X'10000000000000000000000000000002', 'Ruby Red Pomegranates', 'ruby red pomegranates', 'Fresh antioxidant-rich pomegranate pearls', 160.00, '800 g', 'pack', 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=500&auto=format&fit=crop&q=60', TRUE, 40, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000010', X'10000000000000000000000000000002', 'Imported Green Kiwi Box', 'imported green kiwi box', 'Sweet-sour fiber rich kiwis', 110.00, '3 pcs', 'box', 'https://images.unsplash.com/photo-1518492104633-130d0cc84637?w=500&auto=format&fit=crop&q=60', TRUE, 30, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

-- Vegetables
(X'20000000000000000000000000000011', X'10000000000000000000000000000003', 'Hybrid Red Tomatoes', 'hybrid red tomatoes', 'Firm and juicy cooking tomatoes', 35.00, '1 kg', 'kg', 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500&auto=format&fit=crop&q=60', TRUE, 150, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000012', X'10000000000000000000000000000003', 'Nashik Red Onions', 'nashik red onions', 'Pungent, essential kitchen onions', 42.00, '1 kg', 'kg', 'https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?w=500&auto=format&fit=crop&q=60', TRUE, 200, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000013', X'10000000000000000000000000000003', 'Fresh Agra Potatoes', 'fresh agra potatoes', 'Versatile, earthy baking & curry potatoes', 32.00, '1 kg', 'kg', 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=500&auto=format&fit=crop&q=60', TRUE, 250, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000014', X'10000000000000000000000000000003', 'Hydroponic English Cucumber', 'hydroponic english cucumber', 'Crisp, seedless refreshing salad cucumbers', 45.00, '500 g', 'pack', 'https://images.unsplash.com/photo-1604977042946-1eecc30f269e?w=500&auto=format&fit=crop&q=60', TRUE, 80, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000015', X'10000000000000000000000000000003', 'Fresh Spinach / Palak Leaves', 'fresh spinach palak leaves', 'Iron-rich organic leafy greens', 28.00, '250 g', 'bunch', 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=500&auto=format&fit=crop&q=60', TRUE, 65, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

-- Bakery
(X'20000000000000000000000000000016', X'10000000000000000000000000000004', 'Modern 100% Whole Wheat Bread', 'modern 100 whole wheat bread', 'High-fiber wholesome brown sliced loaf', 50.00, '400 g', 'loaf', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&auto=format&fit=crop&q=60', TRUE, 55, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000017', X'10000000000000000000000000000004', 'English Oven Garlic & Herb Loaf', 'english oven garlic herb loaf', 'Artisan aromatic sandwich loaf', 65.00, '350 g', 'loaf', 'https://images.unsplash.com/photo-1586444248902-2f64eddc13df?w=500&auto=format&fit=crop&q=60', TRUE, 30, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000018', X'10000000000000000000000000000004', 'Fresh French Butter Croissants (2 Pcs)', 'fresh french butter croissants 2 pcs', 'Flaky, buttery bakery breakfast pastry', 120.00, '2 pcs', 'pack', 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=500&auto=format&fit=crop&q=60', TRUE, 25, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000019', X'10000000000000000000000000000004', 'Burger Buns with White Sesame Seeds', 'burger buns with white sesame seeds', 'Soft, fluffy gourmet bakery buns', 45.00, '4 pcs', 'pack', 'https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=500&auto=format&fit=crop&q=60', TRUE, 40, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

-- Rice, Grains and Pulses
(X'20000000000000000000000000000020', X'10000000000000000000000000000005', 'Daawat Rozana Gold Basmati Rice', 'daawat rozana gold basmati rice', 'Aromatic long-grain fragrant rice', 145.00, '1 kg', 'pouch', 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500&auto=format&fit=crop&q=60', TRUE, 80, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000021', X'10000000000000000000000000000005', 'Aashirvaad Shudh Chakki Atta', 'aashirvaad shudh chakki atta', '100% whole wheat flour with natural dietary fiber', 255.00, '5 kg', 'bag', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&auto=format&fit=crop&q=60', TRUE, 90, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000022', X'10000000000000000000000000000005', 'Tata Sampann Unpolished Toor Dal', 'tata sampann unpolished toor dal', 'High-protein natural yellow pigeon peas', 170.00, '1 kg', 'pouch', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop&q=60', TRUE, 70, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000023', X'10000000000000000000000000000005', 'Organic Moong Dal Washed', 'organic moong dal washed', 'Easily digestible healthy yellow lentils', 150.00, '1 kg', 'pouch', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop&q=60', TRUE, 65, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

-- Meat and Seafood
(X'20000000000000000000000000000024', X'10000000000000000000000000000006', 'Fresh Chicken Curry Cut (Skinless)', 'fresh chicken curry cut skinless', 'Tender, juicy, antibiotic-free chicken pieces', 210.00, '500 g', 'pack', 'https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=500&auto=format&fit=crop&q=60', TRUE, 40, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000025', X'10000000000000000000000000000006', 'Premium Boneless Chicken Breast', 'premium boneless chicken breast', 'Lean, high-protein fresh cut chicken breast', 260.00, '500 g', 'pack', 'https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=500&auto=format&fit=crop&q=60', TRUE, 35, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000026', X'10000000000000000000000000000006', 'Cleaned Fresh Water Prawns / Shrimp', 'cleaned fresh water prawns shrimp', 'Tail-on deveined fresh coastal prawns', 380.00, '350 g', 'tray', 'https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=500&auto=format&fit=crop&q=60', TRUE, 20, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

-- Snacks
(X'20000000000000000000000000000027', X'10000000000000000000000000000007', 'Haldirams Classic Bhujia Sev', 'haldirams classic bhujia sev', 'Crispy traditional spiced gram flour snack', 90.00, '200 g', 'pack', 'https://images.unsplash.com/photo-1599490659213-e2b9527bd087?w=500&auto=format&fit=crop&q=60', TRUE, 100, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000028', X'10000000000000000000000000000007', 'Lays Spanish Tomato Tango Chips', 'lays spanish tomato tango chips', 'Crunchy potato chips bursting with tangy tomato', 20.00, '52 g', 'pouch', 'https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=500&auto=format&fit=crop&q=60', TRUE, 120, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000029', X'10000000000000000000000000000007', 'Happilo California Roasted Almonds', 'happilo california roasted almonds', 'Healthy lightly salted crunch almonds', 299.00, '200 g', 'pouch', 'https://images.unsplash.com/photo-1508061252445-564467e24631?w=500&auto=format&fit=crop&q=60', TRUE, 50, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000030', X'10000000000000000000000000000007', 'Dark Fantasy Choco Fills Cookies', 'dark fantasy choco fills cookies', 'Rich biscuits filled with melted molten chocolate', 40.00, '75 g', 'box', 'https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=500&auto=format&fit=crop&q=60', TRUE, 85, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

-- Beverages
(X'20000000000000000000000000000031', X'10000000000000000000000000000008', 'Nescafe Classic Instant Coffee Jar', 'nescafe classic instant coffee jar', '100% pure robust coffee granules', 195.00, '100 g', 'jar', 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=500&auto=format&fit=crop&q=60', TRUE, 60, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000032', X'10000000000000000000000000000008', 'Brooke Bond Red Label Tea', 'brooke bond red label tea', 'Strong, aromatic CTC black tea blend', 140.00, '250 g', 'box', 'https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=500&auto=format&fit=crop&q=60', TRUE, 90, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000033', X'10000000000000000000000000000008', 'Real Activ 100% Mixed Fruit Juice', 'real activ 100 mixed fruit juice', 'No added sugar natural nutrient fruit juice', 135.00, '1 L', 'tetra', 'https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=500&auto=format&fit=crop&q=60', TRUE, 70, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000034', X'10000000000000000000000000000008', 'Raw Pressery Pure Coconut Water', 'raw pressery pure coconut water', 'Natural electrolyte hydrating tender coconut water', 75.00, '200 ml', 'bottle', 'https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=500&auto=format&fit=crop&q=60', TRUE, 55, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

-- Household Essentials
(X'20000000000000000000000000000035', X'10000000000000000000000000000009', 'Surf Excel Matic Top Load Liquid Detergent', 'surf excel matic top load liquid detergent', 'Deep cleaning washing machine liquid detergent', 230.00, '1 L', 'bottle', 'https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=500&auto=format&fit=crop&q=60', TRUE, 45, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000036', X'10000000000000000000000000000009', 'Vim Lemon Dishwash Gel with Scrub Pad', 'vim lemon dishwash gel with scrub pad', 'Tough grease cutting lemon formula', 105.00, '500 ml', 'bottle', 'https://images.unsplash.com/photo-1585421514738-01798e348b17?w=500&auto=format&fit=crop&q=60', TRUE, 80, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000037', X'10000000000000000000000000000009', 'Dettol Disinfectant Multi-Surface Cleaner', 'dettol disinfectant multi surface cleaner', 'Antibacterial surface cleanser with fresh pine', 185.00, '500 ml', 'bottle', 'https://images.unsplash.com/photo-1584820927498-cfe5211fd8bf?w=500&auto=format&fit=crop&q=60', TRUE, 60, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

-- Frozen Foods
(X'20000000000000000000000000000038', X'10000000000000000000000000000010', 'McCain French Fries Crispy Snack', 'mccain french fries crispy snack', 'Crunchy golden ready-to-fry potato fries', 110.00, '420 g', 'pack', 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=500&auto=format&fit=crop&q=60', TRUE, 40, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000039', X'10000000000000000000000000000010', 'Sumeru Sweet Green Peas / Matar', 'sumeru sweet green peas matar', 'Naturally sweet tender frozen garden green peas', 85.00, '500 g', 'pack', 'https://images.unsplash.com/photo-1587735243615-c03f25aaff15?w=500&auto=format&fit=crop&q=60', TRUE, 70, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
(X'20000000000000000000000000000040', X'10000000000000000000000000000010', 'ITC Master Chef Veg Pizza Pocket', 'itc master chef veg pizza pocket', 'Crispy pockets stuffed with cheese and vegetables', 165.00, '360 g', 'pack', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format&fit=crop&q=60', TRUE, 30, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE price = VALUES(price);

-- Coupons
INSERT INTO coupons (id, code, discount_percent, discount_amount, min_order_amount, max_discount_amount, is_active, expires_at, created_at) VALUES
(X'30000000000000000000000000000001', 'WELCOME50', 0.00, 50.00, 300.00, 50.00, TRUE, '2027-12-31 23:59:59', CURRENT_TIMESTAMP(6)),
(X'30000000000000000000000000000002', 'SAVE10', 10.00, 0.00, 500.00, 150.00, TRUE, '2027-12-31 23:59:59', CURRENT_TIMESTAMP(6)),
(X'30000000000000000000000000000003', 'FREEDEL', 0.00, 40.00, 400.00, 40.00, TRUE, '2027-12-31 23:59:59', CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE is_active = VALUES(is_active);

-- Delivery Slots for next 7 days (Slots: 07:00-09:00, 09:00-11:00, 16:00-18:00, 18:00-20:00)
INSERT INTO delivery_slots (id, slot_date, start_time, end_time, capacity, booked_count, is_active, created_at) VALUES
(X'40000000000000000000000000000001', CURRENT_DATE, '07:00:00', '09:00:00', 15, 2, TRUE, CURRENT_TIMESTAMP(6)),
(X'40000000000000000000000000000002', CURRENT_DATE, '09:00:00', '11:00:00', 15, 5, TRUE, CURRENT_TIMESTAMP(6)),
(X'40000000000000000000000000000003', CURRENT_DATE, '16:00:00', '18:00:00', 15, 1, TRUE, CURRENT_TIMESTAMP(6)),
(X'40000000000000000000000000000004', CURRENT_DATE, '18:00:00', '20:00:00', 15, 3, TRUE, CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE capacity = VALUES(capacity);
