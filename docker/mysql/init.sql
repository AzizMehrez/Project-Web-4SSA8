-- ShopyTn local MySQL databases (one schema per microservice)
CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS payment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS delivery_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS dispute_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS notification_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON user_db.* TO 'shopytn'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'shopytn'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'shopytn'@'%';
GRANT ALL PRIVILEGES ON delivery_db.* TO 'shopytn'@'%';
GRANT ALL PRIVILEGES ON dispute_db.* TO 'shopytn'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'shopytn'@'%';
FLUSH PRIVILEGES;
