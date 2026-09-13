-- Fix garbled Chinese in CMS seed data (UTF-8 mis-read as Latin-1).
-- Example:
--   docker exec -i snp-mysql mysql -usnp -psnp_change_me --default-character-set=utf8mb4 start_nine_plus < sql/fix-cms-utf8.sql

SET NAMES utf8mb4;

UPDATE cms_block
SET title = '首页介绍',
    content_json = '{"text":"用积分抽奖、兑换好礼"}',
    updated_at = NOW()
WHERE code = 'home_intro';

UPDATE cms_banner
SET title = '欢迎来到 Start Nine Plus',
    updated_at = NOW()
WHERE sort_order = 1;
