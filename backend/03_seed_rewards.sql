UPDATE rewards
SET is_active = FALSE
WHERE LOWER(title) = LOWER('Free Coffee')
  AND points_cost = 50;

UPDATE rewards
SET is_featured = FALSE
WHERE is_featured IS NULL;

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Free Coffee Voucher', 'Redeem for a free coffee at participating local cafes.', 500, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Free Coffee Voucher')
);

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Eco-friendly Tote Bag', 'Durable reusable tote bag made from organic cotton.', 1200, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Eco-friendly Tote Bag')
);

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Public Transit Pass', 'One-day public transport pass for city travel.', 2000, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Public Transit Pass')
);

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Grocery Discount Voucher', 'Voucher for savings at partner supermarkets.', 3500, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Grocery Discount Voucher')
);

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Reusable Water Bottle', 'Insulated reusable bottle for everyday use.', 900, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Reusable Water Bottle')
);

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Cinema Ticket Voucher', 'Redeem for one standard movie ticket.', 1800, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Cinema Ticket Voucher')
);

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Tree Planting Donation', 'Sponsor a tree planting contribution in your name.', 700, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Tree Planting Donation')
);

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Mobile Data Top-Up', 'Small mobile data recharge from a partner provider.', 2500, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Mobile Data Top-Up')
);

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Campus Cafeteria Meal', 'Redeem for one meal at the campus cafeteria.', 1100, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Campus Cafeteria Meal')
);

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Bookstore Discount Coupon', 'Discount coupon for books and stationery.', 1600, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Bookstore Discount Coupon')
);

INSERT INTO rewards (title, description, points_cost, is_active, is_featured)
SELECT 'Laundry Service Voucher', 'Voucher toward a partner laundry service.', 1400, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM rewards WHERE LOWER(title) = LOWER('Laundry Service Voucher')
);
