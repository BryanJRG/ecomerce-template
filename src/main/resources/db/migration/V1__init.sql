-- Baseline schema, generated from the Hibernate-managed dev schema (ddl-auto: update) at the point
-- Flyway took over. Every table here mirrors an @Entity in the codebase — see each entity's
-- class-level Javadoc for the reasoning behind its shape (soft-delete vs hard-delete, no
-- @OneToMany navigation, ON DELETE RESTRICT on historical FKs, etc.).

SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE `admin_invitations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `invited_email` varchar(255) NOT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `role_to_grant` varchar(50) NOT NULL,
  `status` enum('ACCEPTED','CANCELLED','EXPIRED','PENDING','REJECTED') NOT NULL,
  `token_hash` varchar(255) NOT NULL,
  `accepted_by_user_id` bigint DEFAULT NULL,
  `created_by_user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_invitation_token` (`token_hash`),
  KEY `idx_invitation_email` (`invited_email`),
  KEY `FKe9ariulnkcesqlsa9ak4u6488` (`accepted_by_user_id`),
  KEY `FKqmgpdlav0xm39ta2qvff8afk8` (`created_by_user_id`),
  CONSTRAINT `FKe9ariulnkcesqlsa9ak4u6488` FOREIGN KEY (`accepted_by_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqmgpdlav0xm39ta2qvff8afk8` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `new_values` json DEFAULT NULL,
  `old_values` json DEFAULT NULL,
  `record_id` bigint DEFAULT NULL,
  `table_name` varchar(100) NOT NULL,
  `user_agent` text,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_table` (`table_name`),
  KEY `idx_action` (`action`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `FKjs4iimve3y0xssbtve5ysyef0` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `brands` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `logo_url` varchar(500) DEFAULT NULL,
  `name` varchar(150) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `cart_lines` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price_at_addition` decimal(12,2) NOT NULL,
  `quantity` int NOT NULL,
  `cart_id` bigint NOT NULL,
  `variant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_variant` (`cart_id`,`variant_id`),
  KEY `idx_cart_line_cart` (`cart_id`),
  KEY `idx_cart_line_variant` (`variant_id`),
  CONSTRAINT `FKl9x8x2b1f8rksdrjfimvwbw6m` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`),
  CONSTRAINT `FKo89iiikek2ox9cgm1cakk4xvt` FOREIGN KEY (`variant_id`) REFERENCES `item_variants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `carts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `status` enum('ABANDONED','ACTIVE','CONVERTED') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `client_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cart_client` (`client_id`),
  KEY `idx_cart_status_updated` (`status`,`updated_at`),
  CONSTRAINT `FKifii0wg46vi0dhrtvailay156` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `max_price` decimal(12,2) DEFAULT NULL,
  `min_price` decimal(12,2) DEFAULT NULL,
  `name` varchar(150) NOT NULL,
  `slug` varchar(160) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_category_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `clients` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_client_email` (`email`),
  UNIQUE KEY `UKsmrp6gi0tckq1w5rnd7boyowu` (`user_id`),
  CONSTRAINT `FKtiuqdledq2lybrds2k3rfqrv4` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `daily_sales_summaries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `gross_revenue` decimal(14,2) NOT NULL,
  `order_count` int NOT NULL,
  `sales_date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_daily_sales_summary_date` (`sales_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `discount_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL,
  `discount_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_discount_category` (`discount_id`,`category_id`),
  KEY `idx_discount_category_category` (`category_id`),
  CONSTRAINT `FKh48vixodwy7gu9hqq959lk7r1` FOREIGN KEY (`discount_id`) REFERENCES `discounts` (`id`),
  CONSTRAINT `FKogf2yx855ibb0fiuckjlktplg` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `discount_excluded_variants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `discount_id` bigint NOT NULL,
  `variant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_discount_excluded_variant` (`discount_id`,`variant_id`),
  KEY `idx_discount_excluded_variant_variant` (`variant_id`),
  CONSTRAINT `FK9vyhh688ti48wr0q4fqfh1v7q` FOREIGN KEY (`discount_id`) REFERENCES `discounts` (`id`),
  CONSTRAINT `FKmablengwyucfb2fu13ip0q4c9` FOREIGN KEY (`variant_id`) REFERENCES `item_variants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `discount_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `discount_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_discount_item` (`discount_id`,`item_id`),
  KEY `idx_discount_item_item` (`item_id`),
  CONSTRAINT `FK5o861c8kho3n3hbdfmdqepdjn` FOREIGN KEY (`discount_id`) REFERENCES `discounts` (`id`),
  CONSTRAINT `FKm7m5hnci8tvuahtfjcy762px4` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `discount_redemptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `redeemed_at` datetime(6) NOT NULL,
  `client_id` bigint NOT NULL,
  `discount_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_discount_redemption_order` (`discount_id`,`order_id`),
  KEY `idx_discount_redemption_discount_client` (`discount_id`,`client_id`),
  KEY `FKp00kux13gdijsfdt0vr3r7ada` (`client_id`),
  KEY `FKk9trqj9ktdycp25ugjht8vqhd` (`order_id`),
  CONSTRAINT `FKk9trqj9ktdycp25ugjht8vqhd` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKkmhhs114g5814vy6dvgpotw23` FOREIGN KEY (`discount_id`) REFERENCES `discounts` (`id`),
  CONSTRAINT `FKp00kux13gdijsfdt0vr3r7ada` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `discount_variants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `discount_id` bigint NOT NULL,
  `variant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_discount_variant` (`discount_id`,`variant_id`),
  KEY `idx_discount_variant_variant` (`variant_id`),
  CONSTRAINT `FKixbictdy22wjiyfpufir2xrnk` FOREIGN KEY (`variant_id`) REFERENCES `item_variants` (`id`),
  CONSTRAINT `FKmcghunmlyfetmgk6umqkrcddm` FOREIGN KEY (`discount_id`) REFERENCES `discounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `discounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `code` varchar(50) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `end_date` datetime(6) NOT NULL,
  `max_uses_per_client` int DEFAULT NULL,
  `max_uses_total` int DEFAULT NULL,
  `min_purchase_amount` decimal(12,2) DEFAULT NULL,
  `start_date` datetime(6) NOT NULL,
  `target_type` enum('ALL','CATEGORY','ITEM','VARIANT') NOT NULL,
  `title` varchar(200) NOT NULL,
  `type` enum('FIXED_AMOUNT','PERCENTAGE') NOT NULL,
  `value` decimal(12,2) NOT NULL,
  `used_count` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_discount_code` (`code`),
  KEY `idx_discount_active_dates` (`active`,`start_date`,`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `email_verification_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `token_hash` varchar(255) NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKah12jdrm74nfnmu11x9xxlbex` (`token_hash`),
  KEY `FKi1c4mmamlb8keqt74k4lrtwhc` (`user_id`),
  CONSTRAINT `FKi1c4mmamlb8keqt74k4lrtwhc` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `item_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `display_order` int NOT NULL,
  `is_primary` bit(1) NOT NULL,
  `url` varchar(500) NOT NULL,
  `item_id` bigint NOT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_image_item` (`item_id`),
  KEY `idx_image_variant` (`variant_id`),
  CONSTRAINT `FK31vykiuqi6nfw2rmvw37qlydy` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`),
  CONSTRAINT `FK8515xs38d2s54bxmx8qn4jdmy` FOREIGN KEY (`variant_id`) REFERENCES `item_variants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `item_variants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `low_stock_threshold` int NOT NULL,
  `name` varchar(150) NOT NULL,
  `price` decimal(12,2) NOT NULL,
  `sku` varchar(64) NOT NULL,
  `stock` int NOT NULL,
  `version` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_variant_sku` (`sku`),
  KEY `idx_variant_item` (`item_id`),
  CONSTRAINT `FKjvwpj86cp0scxwm4va1yav3i` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text,
  `name` varchar(200) NOT NULL,
  `total_stock` int NOT NULL,
  `brand_id` bigint DEFAULT NULL,
  `category_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_item_category` (`category_id`),
  KEY `idx_item_brand` (`brand_id`),
  CONSTRAINT `FKi0gnxi21mo1gmbl3q2cqvpx69` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`id`),
  CONSTRAINT `FKjcdcde7htb3tyjgouo4g9xbmr` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `login_method` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `provider` enum('GOOGLE','LOCAL') NOT NULL,
  `provider_user_id` varchar(255) DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmyseunkkv020piyyl1gho3dgp` (`usuario_id`),
  CONSTRAINT `FKmyseunkkv020piyyl1gho3dgp` FOREIGN KEY (`usuario_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `data` json DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `message` text,
  `read_at` datetime(6) DEFAULT NULL,
  `related_id` bigint DEFAULT NULL,
  `related_table` varchar(100) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `type` varchar(100) NOT NULL,
  `actor_id` bigint DEFAULT NULL,
  `recipient_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_recipient` (`recipient_id`),
  KEY `idx_notification_recipient_read` (`recipient_id`,`is_read`),
  KEY `idx_notification_type` (`type`),
  KEY `idx_notification_created_at` (`created_at`),
  KEY `FK4sd9fik0uthbk6d9rsxco4uja` (`actor_id`),
  CONSTRAINT `FK4sd9fik0uthbk6d9rsxco4uja` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqqnsjxlwleyjbxlmm213jaj3f` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `order_lines` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_name_snapshot` varchar(200) NOT NULL,
  `price_at_purchase` decimal(12,2) NOT NULL,
  `quantity` int NOT NULL,
  `order_id` bigint NOT NULL,
  `variant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_line_order` (`order_id`),
  KEY `idx_order_line_variant` (`variant_id`),
  CONSTRAINT `FK1smc0s578t2oih21yn9hw6usr` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKl17gseex5mfpkf9t0mojtn4rt` FOREIGN KEY (`variant_id`) REFERENCES `item_variants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `order_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `changed_at` datetime(6) NOT NULL,
  `note` varchar(500) DEFAULT NULL,
  `status` enum('CANCELLED','DELIVERED','PAID','PENDING','PROCESSING','REFUNDED','SHIPPED') NOT NULL,
  `order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_status_history_order` (`order_id`),
  CONSTRAINT `FKnmcbg3mmbt8wfva97ra40nmp3` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `sales_channel` enum('IN_STORE','ONLINE','PHONE') NOT NULL,
  `status` enum('CANCELLED','DELIVERED','PAID','PENDING','PROCESSING','REFUNDED','SHIPPED') NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `tracking_number` varchar(100) DEFAULT NULL,
  `client_id` bigint NOT NULL,
  `shipping_address_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_client` (`client_id`),
  KEY `idx_order_status` (`status`),
  KEY `FKstjxbn0162q6csb4f7ejx3fwe` (`shipping_address_id`),
  CONSTRAINT `FKm2dep9derpoaehshbkkatam3v` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`),
  CONSTRAINT `FKstjxbn0162q6csb4f7ejx3fwe` FOREIGN KEY (`shipping_address_id`) REFERENCES `shipping_addresses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `password_reset_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `token_hash` varchar(255) NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKajre85ybxavf1tt4omkrs5p6g` (`token_hash`),
  KEY `FKtc1ilxtb129hunxr7juosig9r` (`usuario_id`),
  CONSTRAINT `FKtc1ilxtb129hunxr7juosig9r` FOREIGN KEY (`usuario_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `provider` varchar(100) NOT NULL,
  `provider_transaction_id` varchar(150) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','FAILED','PENDING') NOT NULL,
  `type` enum('CARD','GOOGLE_PAY') NOT NULL,
  `order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_payment_order` (`order_id`),
  KEY `idx_payment_provider_tx` (`provider_transaction_id`),
  CONSTRAINT `FK81gagumt0r8y3rmudcgpbk42l` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `pending_admin_actions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `confirmed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `payload` varchar(255) NOT NULL,
  `status` enum('CANCELLED','CONFIRMED','PENDING') NOT NULL,
  `token_hash` varchar(255) NOT NULL,
  `type` enum('CREATE_ADMIN_INVITATION') NOT NULL,
  `requested_by_user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_pending_action_token` (`token_hash`),
  KEY `FKq0lqk0lolxq32rly24ear99q4` (`requested_by_user_id`),
  CONSTRAINT `FKq0lqk0lolxq32rly24ear99q4` FOREIGN KEY (`requested_by_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(200) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpnvtwliis6p05pn6i3ndjrqt2` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `refund_lines` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) NOT NULL,
  `quantity` int NOT NULL,
  `order_line_id` bigint NOT NULL,
  `refund_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_refund_line_refund` (`refund_id`),
  KEY `idx_refund_line_order_line` (`order_line_id`),
  CONSTRAINT `FKfvmn7t1omqcr7x3f1hkrlkxyw` FOREIGN KEY (`refund_id`) REFERENCES `refunds` (`id`),
  CONSTRAINT `FKrvx4hlux2hmrvrptb2yt7m3r2` FOREIGN KEY (`order_line_id`) REFERENCES `order_lines` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `refund_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `changed_at` datetime(6) NOT NULL,
  `status` enum('APPROVED','COMPLETED','REJECTED','REQUESTED') NOT NULL,
  `refund_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_refund_status_history_refund` (`refund_id`),
  CONSTRAINT `FK3bew96t1i3obu62u6rssj07di` FOREIGN KEY (`refund_id`) REFERENCES `refunds` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `refunds` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `reason` varchar(500) NOT NULL,
  `status` enum('APPROVED','COMPLETED','REJECTED','REQUESTED') NOT NULL,
  `total_amount` decimal(12,2) NOT NULL,
  `order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_refund_order` (`order_id`),
  CONSTRAINT `FKsk9rqm7f6y8b1g0qob018hdm7` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `review_votes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `helpful` bit(1) NOT NULL,
  `review_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_vote_review_user` (`review_id`,`user_id`),
  KEY `idx_review_vote_review` (`review_id`),
  KEY `FKf48a6afo7lolirjks7uorfaah` (`user_id`),
  CONSTRAINT `FK5bf4o4ap2v7g3qpxt8y28a38x` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`),
  CONSTRAINT `FKf48a6afo7lolirjks7uorfaah` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` text,
  `created_at` datetime(6) NOT NULL,
  `rating` int NOT NULL,
  `item_id` bigint NOT NULL,
  `order_line_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_user_item` (`user_id`,`item_id`),
  KEY `idx_review_item` (`item_id`),
  KEY `idx_review_user` (`user_id`),
  KEY `FKmy9dpbq2g4hy5altoj3lpqqnb` (`order_line_id`),
  CONSTRAINT `FKcgy7qjc1r99dp117y9en6lxye` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKd0qivr20lp2u34cfcrr0ibct7` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`),
  CONSTRAINT `FKmy9dpbq2g4hy5altoj3lpqqnb` FOREIGN KEY (`order_line_id`) REFERENCES `order_lines` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `role_permissions` (
  `role_id` int NOT NULL,
  `permission_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `FKegdk29eiy7mdtefy5c7eirr6e` (`permission_id`),
  CONSTRAINT `FKegdk29eiy7mdtefy5c7eirr6e` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
  CONSTRAINT `FKn5fotdgk8d1xvo8nav9uv3muc` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKofx66keruapi6vyqpv6f2or37` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `shipping_addresses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_line1` varchar(255) NOT NULL,
  `address_line2` varchar(255) DEFAULT NULL,
  `city` varchar(100) NOT NULL,
  `department` varchar(100) NOT NULL,
  `is_default` bit(1) NOT NULL,
  `label` varchar(100) NOT NULL,
  `phone` varchar(30) NOT NULL,
  `recipient_name` varchar(150) NOT NULL,
  `client_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_shipping_address_client` (`client_id`),
  CONSTRAINT `FKs0w9b90gihv7klnfj0rsmwv8v` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `stock_reservations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `idempotency_key` varchar(100) DEFAULT NULL,
  `quantity` int NOT NULL,
  `reserved_at` datetime(6) NOT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `status` enum('CONFIRMED','PENDING','RELEASED') NOT NULL,
  `cart_id` bigint NOT NULL,
  `variant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reservation_status_reserved_at` (`status`,`reserved_at`),
  KEY `idx_reservation_variant` (`variant_id`),
  KEY `FK7g4qrt7yrd49u0lcgeqxgmt3c` (`cart_id`),
  CONSTRAINT `FK7g4qrt7yrd49u0lcgeqxgmt3c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`),
  CONSTRAINT `FK7vaqldglggq7g3v24tsgstcf7` FOREIGN KEY (`variant_id`) REFERENCES `item_variants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`),
  CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `first_name` varchar(100) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `is_verified` bit(1) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `locked_until` datetime(6) DEFAULT NULL,
  `mfa_enabled` bit(1) NOT NULL,
  `mfa_secret` varchar(255) DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKk8d0f2n7n88w1a16yhua64onx` (`user_name`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `wishlist_lines` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `added_at` datetime(6) NOT NULL,
  `variant_id` bigint NOT NULL,
  `wishlist_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wishlist_variant` (`wishlist_id`,`variant_id`),
  KEY `idx_wishlist_line_wishlist` (`wishlist_id`),
  KEY `idx_wishlist_line_variant` (`variant_id`),
  CONSTRAINT `FK4cgt4r3ua02ytqsus4oyynmdj` FOREIGN KEY (`wishlist_id`) REFERENCES `wishlists` (`id`),
  CONSTRAINT `FKsityw0af7xcyw9a3ifjyug2gu` FOREIGN KEY (`variant_id`) REFERENCES `item_variants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `wishlists` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_wishlist_user` (`user_id`),
  CONSTRAINT `FK330pyw2el06fn5g28ypyljt16` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS=1;
