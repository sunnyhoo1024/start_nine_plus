CREATE DATABASE IF NOT EXISTS start_nine_plus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE start_nine_plus;
SET NAMES utf8mb4;

-- Admin RBAC
CREATE TABLE IF NOT EXISTS admin_user (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    nickname      VARCHAR(64)  NULL,
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS admin_role (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(64)  NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS admin_permission (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    code        VARCHAR(128) NOT NULL UNIQUE,
    name        VARCHAR(64)  NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS admin_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS admin_role_permission (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS admin_op_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id    BIGINT       NULL,
    action      VARCHAR(128) NOT NULL,
    detail      VARCHAR(1024) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- App users
CREATE TABLE IF NOT EXISTS app_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid      VARCHAR(64)  NOT NULL UNIQUE,
    unionid     VARCHAR(64)  NULL,
    nickname    VARCHAR(64)  NULL,
    avatar_url  VARCHAR(512) NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- CMS
CREATE TABLE IF NOT EXISTS cms_banner (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(128) NOT NULL,
    image_url   VARCHAR(512) NOT NULL,
    link_url    VARCHAR(512) NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0草稿 1上线',
    start_at    DATETIME     NULL,
    end_at      DATETIME     NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cms_block (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    title       VARCHAR(128) NOT NULL,
    content_json JSON        NULL,
    status      TINYINT      NOT NULL DEFAULT 0,
    start_at    DATETIME     NULL,
    end_at      DATETIME     NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Points ledger
CREATE TABLE IF NOT EXISTS points_account (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL UNIQUE,
    balance     BIGINT       NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS points_ledger (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    change_amount BIGINT       NOT NULL COMMENT '正加负减',
    balance_after BIGINT       NOT NULL,
    biz_type      VARCHAR(32)  NOT NULL COMMENT 'ADMIN_ADJUST/LOTTERY_COST/LOTTERY_REWARD/REDEEM/...',
    biz_id        VARCHAR(64)  NULL,
    remark        VARCHAR(255) NULL,
    operator_id   BIGINT       NULL COMMENT '后台调分操作人',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB;

-- Lottery
CREATE TABLE IF NOT EXISTS lottery_activity (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    name             VARCHAR(128) NOT NULL,
    description      VARCHAR(512) NULL,
    cost_points      BIGINT       NOT NULL DEFAULT 0,
    daily_limit      INT          NOT NULL DEFAULT 0 COMMENT '0不限',
    total_limit      INT          NOT NULL DEFAULT 0 COMMENT '0不限',
    status           TINYINT      NOT NULL DEFAULT 0,
    start_at         DATETIME     NULL,
    end_at           DATETIME     NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS lottery_prize (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id  BIGINT       NOT NULL,
    name         VARCHAR(128) NOT NULL,
    prize_type   VARCHAR(32)  NOT NULL COMMENT 'POINTS/THANKS/PHYSICAL',
    points_value BIGINT       NOT NULL DEFAULT 0,
    weight       INT          NOT NULL DEFAULT 1,
    stock        INT          NOT NULL DEFAULT -1 COMMENT '-1无限',
    image_url    VARCHAR(512) NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    status       TINYINT      NOT NULL DEFAULT 1,
    INDEX idx_activity (activity_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS lottery_record (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id  BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    prize_id     BIGINT       NULL,
    prize_name   VARCHAR(128) NULL,
    prize_type   VARCHAR(32)  NULL,
    points_value BIGINT       NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_act (user_id, activity_id, created_at)
) ENGINE=InnoDB;

-- Redeem
CREATE TABLE IF NOT EXISTS redeem_sku (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(128) NOT NULL,
    description  VARCHAR(512) NULL,
    image_url    VARCHAR(512) NULL,
    points_price BIGINT       NOT NULL,
    stock        INT          NOT NULL DEFAULT 0,
    status       TINYINT      NOT NULL DEFAULT 0,
    start_at     DATETIME     NULL,
    end_at       DATETIME     NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS redeem_order (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no     VARCHAR(64)  NOT NULL UNIQUE,
    user_id      BIGINT       NOT NULL,
    sku_id       BIGINT       NOT NULL,
    sku_name     VARCHAR(128) NOT NULL,
    points_cost  BIGINT       NOT NULL,
    quantity     INT          NOT NULL DEFAULT 1,
    status       VARCHAR(32)  NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/FULFILLED/CANCELLED',
    remark       VARCHAR(255) NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

-- Admin user/role/permission are seeded by AdminBootstrap on API startup (admin / admin123).

INSERT INTO cms_banner (title, image_url, link_url, sort_order, status)
SELECT '欢迎来到 Start Nine Plus', 'https://via.placeholder.com/750x300?text=Banner', '/pages/lottery/index', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cms_banner LIMIT 1);

INSERT INTO cms_block (code, title, content_json, status)
SELECT 'home_intro', '首页介绍', CAST('{"text":"用积分抽奖、兑换好礼"}' AS CHAR CHARACTER SET utf8mb4), 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cms_block WHERE code = 'home_intro');
