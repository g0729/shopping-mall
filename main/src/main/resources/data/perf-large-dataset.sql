-- 대용량 성능 검증용 데이터셋 시드
-- 실행 예시:
-- docker exec -i shopping-mysql mysql -uroot -p${DB_ROOT_PASSWORD} shopping_mall < src/main/resources/data/perf-large-dataset.sql
--
-- 기본 목표 건수(필요 시 아래 변수만 수정):
--   site_user: 50,000
--   product:   300,000
--   orders:  4,000,000

SET @target_site_user = 50000;
SET @target_product = 300000;
SET @target_orders = 4000000;

DROP PROCEDURE IF EXISTS seed_site_users;
DROP PROCEDURE IF EXISTS seed_products_with_rep_image;
DROP PROCEDURE IF EXISTS seed_orders_spread_users;

DELIMITER //

CREATE PROCEDURE seed_site_users(IN p_target BIGINT)
BEGIN
    DECLARE v_current BIGINT DEFAULT 0;
    DECLARE v_remaining BIGINT DEFAULT 0;
    DECLARE v_seed_start BIGINT DEFAULT 0;
    DECLARE v_batch INT DEFAULT 0;

    SELECT COUNT(*) INTO v_current FROM site_user;

    IF v_current < p_target THEN
        SET v_remaining = p_target - v_current;

        WHILE v_remaining > 0 DO
            SET v_batch = IF(v_remaining > 10000, 10000, v_remaining);
            SET v_seed_start = v_current;

            INSERT INTO site_user (email, nickname, password, provider, provider_id, role)
            SELECT CONCAT('perf_user_', seq_num, '@example.com'),
                   CONCAT('PerfUser', seq_num),
                   '{noop}perf-password',
                   NULL,
                   NULL,
                   'USER'
            FROM (
                SELECT @n := @n + 1 AS seq_num
                FROM information_schema.columns c1
                CROSS JOIN information_schema.columns c2
                CROSS JOIN (SELECT @n := v_seed_start) seed
                LIMIT v_batch
            ) seq;

            SET v_current = v_current + v_batch;
            SET v_remaining = v_remaining - v_batch;
        END WHILE;
    END IF;
END //

CREATE PROCEDURE seed_products_with_rep_image(IN p_target BIGINT)
BEGIN
    DECLARE v_current BIGINT DEFAULT 0;
    DECLARE v_remaining BIGINT DEFAULT 0;
    DECLARE v_seed_start BIGINT DEFAULT 0;
    DECLARE v_first_id BIGINT DEFAULT 0;
    DECLARE v_batch INT DEFAULT 0;

    SELECT COUNT(*) INTO v_current FROM product;

    IF v_current < p_target THEN
        SET v_remaining = p_target - v_current;

        WHILE v_remaining > 0 DO
            SET v_batch = IF(v_remaining > 20000, 20000, v_remaining);
            SET v_seed_start = v_current;

            INSERT INTO product (
                name,
                price,
                stock_quantity,
                description,
                product_sell_status,
                reg_time,
                update_time,
                created_by,
                modified_by
            )
            SELECT CONCAT(
                       ELT(1 + MOD(seq_num, 6), '상의', '하의', '아우터', '신발', '가방', '액세서리'),
                       ' PERF ',
                       LPAD(seq_num, 7, '0')
                   ),
                   10000 + MOD(seq_num * 13, 190000),
                   1 + MOD(seq_num, 300),
                   CONCAT('대용량 성능 테스트 상품 ', seq_num),
                   IF(MOD(seq_num, 20) = 0, 'SOLD_OUT', 'SELL'),
                   DATE_SUB(NOW(6), INTERVAL MOD(seq_num, 365) DAY),
                   NOW(6),
                   CONCAT('perf_admin_', MOD(seq_num, 100), '@seed.local'),
                   CONCAT('perf_admin_', MOD(seq_num, 100), '@seed.local')
            FROM (
                SELECT @n := @n + 1 AS seq_num
                FROM information_schema.columns c1
                CROSS JOIN information_schema.columns c2
                CROSS JOIN (SELECT @n := v_seed_start) seed
                LIMIT v_batch
            ) seq;

            SET v_first_id = LAST_INSERT_ID();

            INSERT INTO product_image (
                img_name,
                ori_img_name,
                img_url,
                rep_img_yn,
                product_id,
                reg_time,
                update_time,
                created_by,
                modified_by
            )
            SELECT 'no-image.jpg',
                   CONCAT(p.name, '_rep.jpg'),
                   '/images/no-image.jpg',
                   'Y',
                   p.product_id,
                   NOW(6),
                   NOW(6),
                   'perf-seed@local',
                   'perf-seed@local'
            FROM product p
            WHERE p.product_id BETWEEN v_first_id AND (v_first_id + v_batch - 1);

            SET v_current = v_current + v_batch;
            SET v_remaining = v_remaining - v_batch;
        END WHILE;
    END IF;
END //

CREATE PROCEDURE seed_orders_spread_users(IN p_target BIGINT)
BEGIN
    DECLARE v_current BIGINT DEFAULT 0;
    DECLARE v_remaining BIGINT DEFAULT 0;
    DECLARE v_seed_start BIGINT DEFAULT 0;
    DECLARE v_user_span BIGINT DEFAULT 0;
    DECLARE v_batch INT DEFAULT 0;

    SELECT COUNT(*) INTO v_current FROM orders;

    CREATE TEMPORARY TABLE IF NOT EXISTS tmp_seed_user_ids (
        seq BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL
    ) ENGINE=InnoDB;

    TRUNCATE TABLE tmp_seed_user_ids;

    INSERT INTO tmp_seed_user_ids (user_id)
    SELECT id
    FROM site_user
    ORDER BY id;

    SELECT COUNT(*) INTO v_user_span FROM tmp_seed_user_ids;

    IF v_user_span = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'site_user 테이블이 비어 있어 orders 시드를 진행할 수 없습니다.';
    END IF;

    IF v_current < p_target THEN
        SET v_remaining = p_target - v_current;

        WHILE v_remaining > 0 DO
            SET v_batch = IF(v_remaining > 50000, 50000, v_remaining);
            SET v_seed_start = v_current;

            INSERT INTO orders (
                created_by,
                modified_by,
                reg_time,
                update_time,
                order_date,
                status,
                user_id
            )
            SELECT 'perf-seed@local',
                   'perf-seed@local',
                   DATE_SUB(NOW(6), INTERVAL MOD(seq_num, 3650) DAY),
                   DATE_SUB(NOW(6), INTERVAL MOD(seq_num, 3650) DAY),
                   DATE_SUB(NOW(6), INTERVAL MOD(seq_num, 3650) DAY),
                   IF(MOD(seq_num, 9) = 0, 'CANCEL', 'ORDER'),
                   picked.user_id
            FROM (
                SELECT @n := @n + 1 AS seq_num
                FROM information_schema.columns c1
                CROSS JOIN information_schema.columns c2
                CROSS JOIN (SELECT @n := v_seed_start) seed
                LIMIT v_batch
            ) seq
            JOIN tmp_seed_user_ids picked
                ON picked.seq = 1 + MOD(seq.seq_num - 1, v_user_span);

            SET v_current = v_current + v_batch;
            SET v_remaining = v_remaining - v_batch;
        END WHILE;
    END IF;

    DROP TEMPORARY TABLE IF EXISTS tmp_seed_user_ids;
END //

DELIMITER ;

CALL seed_site_users(@target_site_user);
CALL seed_products_with_rep_image(@target_product);
CALL seed_orders_spread_users(@target_orders);

DROP PROCEDURE seed_site_users;
DROP PROCEDURE seed_products_with_rep_image;
DROP PROCEDURE seed_orders_spread_users;

SELECT 'site_user' AS table_name, COUNT(*) AS rows_count FROM site_user
UNION ALL
SELECT 'product', COUNT(*) FROM product
UNION ALL
SELECT 'product_image', COUNT(*) FROM product_image
UNION ALL
SELECT 'orders', COUNT(*) FROM orders;
