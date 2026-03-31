-- =====================================================
-- E. path_ids 质量检查（MySQL 8+）
-- =====================================================

-- E1. path_ids 非 JSON 数组
SELECT id, path_ids
FROM note_node
WHERE path_ids IS NOT NULL
  AND JSON_VALID(path_ids) = 1
  AND JSON_TYPE(path_ids) <> 'ARRAY';

-- E2. path_ids 中包含不存在的节点ID
SELECT n.id AS note_id, jt.path_id
FROM note_node n
         JOIN JSON_TABLE(n.path_ids, '$[*]' COLUMNS(path_id BIGINT PATH '$')) jt
         LEFT JOIN note_node p ON p.id = jt.path_id
WHERE p.id IS NULL;

-- E3. path_ids 最后一个ID不是自己（按你的设计应是根->自己）
SELECT n.id, jt.last_id
FROM note_node n
         JOIN (
    SELECT x.id,
           CAST(JSON_UNQUOTE(JSON_EXTRACT(x.path_ids, CONCAT('$[', JSON_LENGTH(x.path_ids)-1, ']'))) AS UNSIGNED) AS last_id
    FROM note_node x
    WHERE x.path_ids IS NOT NULL
      AND JSON_VALID(x.path_ids) = 1
      AND JSON_TYPE(x.path_ids) = 'ARRAY'
      AND JSON_LENGTH(x.path_ids) > 0
) jt ON jt.id = n.id
WHERE jt.last_id <> n.id;
