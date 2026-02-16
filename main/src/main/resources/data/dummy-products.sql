-- 상품 1000개 + 상품당 대표이미지 1개, 서브이미지 2개 더미 데이터
-- Docker MySQL에서 실행: docker exec -i shopping-mysql mysql -uroot -p${DB_ROOT_PASSWORD} shopping_mall < dummy-products.sql

DROP PROCEDURE IF EXISTS insert_dummy_products;

DELIMITER //

CREATE PROCEDURE insert_dummy_products()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE category VARCHAR(20);
    DECLARE product_name VARCHAR(50);
    DECLARE price INT;
    DECLARE stock INT;
    DECLARE product_id BIGINT;

    WHILE i <= 1000 DO
        -- 카테고리 랜덤 배정
        SET category = ELT(FLOOR(1 + RAND() * 5), '상의', '하의', '아우터', '신발', '액세서리');

        SET product_name = CONCAT(category, ' 상품 #', i);
        SET price = FLOOR(10000 + RAND() * 190000);  -- 10,000 ~ 200,000원
        SET stock = FLOOR(10 + RAND() * 490);         -- 10 ~ 500개

        -- 상품 INSERT
        INSERT INTO product (name, price, stock_quantity, description, product_sell_status, reg_time, update_time, created_by, modified_by)
        VALUES (
            product_name,
            price,
            stock,
            CONCAT(product_name, ' 상세 설명'),
            'SELL',
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
            NOW(),
            'admin@test.com',
            'admin@test.com'
        );

        SET product_id = LAST_INSERT_ID();

        -- 대표 이미지
        INSERT INTO product_image (img_name, ori_img_name, img_url, rep_img_yn, product_id, reg_time, update_time, created_by, modified_by)
        VALUES (
            'no-image.jpg',
            CONCAT(product_name, '_대표.jpg'),
            '/images/no-image.jpg',
            'Y',
            product_id,
            NOW(), NOW(), 'admin@test.com', 'admin@test.com'
        );

        -- 서브 이미지 1
        INSERT INTO product_image (img_name, ori_img_name, img_url, rep_img_yn, product_id, reg_time, update_time, created_by, modified_by)
        VALUES (
            'no-image.jpg',
            CONCAT(product_name, '_상세1.jpg'),
            '/images/no-image.jpg',
            'N',
            product_id,
            NOW(), NOW(), 'admin@test.com', 'admin@test.com'
        );

        -- 서브 이미지 2
        INSERT INTO product_image (img_name, ori_img_name, img_url, rep_img_yn, product_id, reg_time, update_time, created_by, modified_by)
        VALUES (
            'no-image.jpg',
            CONCAT(product_name, '_상세2.jpg'),
            '/images/no-image.jpg',
            'N',
            product_id,
            NOW(), NOW(), 'admin@test.com', 'admin@test.com'
        );

        SET i = i + 1;
    END WHILE;
END //

DELIMITER ;

CALL insert_dummy_products();
DROP PROCEDURE insert_dummy_products;
