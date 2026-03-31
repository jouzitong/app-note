-- =====================================================
-- D. 节点树结构异常
-- =====================================================

-- D1. parent_id 指向不存在节点
SELECT n.*
FROM note_node n
         LEFT JOIN note_node p ON p.id = n.parent_id
WHERE n.parent_id IS NOT NULL AND p.id IS NULL;

-- D2. 自己指向自己
SELECT *
FROM note_node
WHERE parent_id = id;
