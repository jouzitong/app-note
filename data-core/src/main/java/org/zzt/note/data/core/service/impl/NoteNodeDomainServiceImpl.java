package org.zzt.note.data.core.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zzt.note.data.core.convert.NoteNodeConvert;
import org.zzt.note.data.core.dto.NoteNodeAddDTO;
import org.zzt.note.data.core.entity.NoteNode;
import org.zzt.note.data.core.entity.NoteNodeContent;
import org.zzt.note.data.core.entity.NoteNodeMeta;
import org.zzt.note.data.core.entity.NoteTag;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.data.core.entity.dto.NoteNodeMetaDTO;
import org.zzt.note.data.core.entity.dto.NoteTagDTO;
import org.zzt.note.data.core.repository.INoteNodeContentRepository;
import org.zzt.note.data.core.repository.INoteNodeRepository;
import org.zzt.note.data.core.repository.INoteTagRepository;
import org.zzt.note.data.core.request.NoteNodeRequest;
import org.zzt.note.data.core.service.INoteNodeDomainService;
import org.zzt.note.data.core.vo.NoteNodePathVO;
import org.zzt.note.data.core.vo.NoteNodeVO;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Service
@Slf4j
@AllArgsConstructor
public class NoteNodeDomainServiceImpl implements INoteNodeDomainService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_TAG_BIZ_TYPE = "NOTE_NODE";
    private static final int DEFAULT_SEARCH_LIMIT = 20;

    private final INoteNodeRepository noteNodeRepository;

    private final INoteNodeContentRepository noteNodeContentRepository;

    private final INoteTagRepository noteTagRepository;

    private final NoteNodeConvert noteNodeConvert;

    /**
     * 启动后为历史数据补全 pathIds：
     * pathIds 为空时，按 parentId 追溯，生成「根 -> 当前节点」的路径ID列表。
     */
//    @PostConstruct
    @Transactional
    public void initPathIdsOnStartup() {
        List<NoteNode> allNodes = noteNodeRepository.findAll();
        if (CollectionUtils.isEmpty(allNodes)) {
            return;
        }

        Map<Long, NoteNode> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(NoteNode::getId, node -> node));
        Map<Long, List<Long>> pathCache = new HashMap<>();
        List<NoteNode> needUpdateNodes = new ArrayList<>();

        for (NoteNode node : allNodes) {
            if (!CollectionUtils.isEmpty(node.getPathIds())) {
                continue;
            }
            List<Long> computedPathIds = buildPathIds(node.getId(), nodeMap, pathCache, new HashSet<>());
            node.setPathIds(computedPathIds);
            needUpdateNodes.add(node);
        }

        if (!needUpdateNodes.isEmpty()) {
            noteNodeRepository.saveAll(needUpdateNodes);
            log.info("Initialized pathIds for {} note nodes.", needUpdateNodes.size());
        }
    }

    @Override
    @Transactional
    public void add(NoteNodeAddDTO noteNodeAdd) {
        // 1) 基础参数校验：创建节点时必须携带 noteNode 主体
        if (noteNodeAdd == null || noteNodeAdd.getNoteNode() == null) {
            throw new IllegalArgumentException("noteNodeAdd.noteNode cannot be null");
        }

        NoteNodeDTO noteNodeDTO = noteNodeAdd.getNoteNode();
        // 2) pathIds 由后端根据 parentId 计算，不信任前端传入值
        noteNodeDTO.setPathIds(Collections.emptyList());

        // 3) 先保存节点主表（note_node）
        NoteNode noteNode = noteNodeConvert.toEntity(noteNodeDTO);
        normalizeMetaTags(noteNode);
        NoteNode saved = noteNodeRepository.save(noteNode);

        // 4) 基于 parentId 计算当前节点 pathIds 并回写
        List<Long> computedPathIds = buildPathIdsForNewNode(saved);
        saved.setPathIds(computedPathIds);
        noteNodeRepository.save(saved);

        // 5) 若有正文内容，再写入独立内容表（note_node_content）
        if (noteNodeAdd.getContent() != null) {
            NoteNodeContent nodeContent = new NoteNodeContent();
            nodeContent.setNode(saved);
            nodeContent.setContent(toContentString(noteNodeAdd.getContent()));
            noteNodeContentRepository.save(nodeContent);
        }
    }

    @Override
    @Transactional
    public void update(NoteNodeAddDTO noteNodeAdd) {
        // 1) 更新必须携带节点ID
        if (noteNodeAdd == null || noteNodeAdd.getNoteNode() == null || noteNodeAdd.getNoteNode().getId() == null) {
            throw new IllegalArgumentException("noteNodeAdd.noteNode.id cannot be null");
        }

        Long nodeId = noteNodeAdd.getNoteNode().getId();
        NoteNode existingNode = noteNodeRepository.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("NoteNode not found, id=" + nodeId));

        // 2) 主表字段更新（pathIds 优先取外层参数）
        NoteNodeDTO dto = noteNodeAdd.getNoteNode();
        if (noteNodeAdd.getPathIds() != null) {
            dto.setPathIds(noteNodeAdd.getPathIds());
        }
        NoteNodeMetaDTO incomingMeta = dto.getMeta();
        dto.setMeta(null);
        noteNodeConvert.updateEntityFromDto(dto, existingNode);
        applyMetaFromDto(existingNode, incomingMeta);
        normalizeMetaTags(existingNode);
        noteNodeRepository.save(existingNode);

        // 3) 内容表做 upsert/清空
        NoteNodeContent existingContent = noteNodeContentRepository.findByNodeId(nodeId).orElse(null);
        if (noteNodeAdd.getContent() == null) {
            if (existingContent != null) {
                noteNodeContentRepository.delete(existingContent);
            }
        } else {
            if (existingContent == null) {
                existingContent = new NoteNodeContent();
                existingContent.setNode(existingNode);
            }
            existingContent.setContent(toContentString(noteNodeAdd.getContent()));
            noteNodeContentRepository.save(existingContent);
        }

        // 4) 当前节点及其子树路径重算，避免 parent 变化后 pathIds 脏数据
        List<NoteNode> allNodes = noteNodeRepository.findAll();
        Map<Long, NoteNode> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(NoteNode::getId, node -> node));
        Set<Long> subtreeIds = collectSubtreeIds(nodeId, allNodes);
        Map<Long, List<Long>> pathCache = new HashMap<>();
        List<NoteNode> needUpdatePathNodes = new ArrayList<>();
        for (Long subtreeId : subtreeIds) {
            NoteNode subtreeNode = nodeMap.get(subtreeId);
            if (subtreeNode == null) {
                continue;
            }
            List<Long> newPathIds = buildPathIds(subtreeId, nodeMap, pathCache, new HashSet<>());
            if (!newPathIds.equals(subtreeNode.getPathIds())) {
                subtreeNode.setPathIds(newPathIds);
                needUpdatePathNodes.add(subtreeNode);
            }
        }
        if (!needUpdatePathNodes.isEmpty()) {
            noteNodeRepository.saveAll(needUpdatePathNodes);
        }
    }

    @Override
    @Transactional
    public void delete(NoteNodeRequest request) {
        // 1) 删除必须指定节点ID
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("request.id cannot be null");
        }
        Long rootId = request.getId();

        // 2) 先加载当前可见节点，构造待删除子树
        List<NoteNode> allNodes = noteNodeRepository.findAll();
        if (CollectionUtils.isEmpty(allNodes)) {
            return;
        }
        Map<Long, NoteNode> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(NoteNode::getId, node -> node));
        if (!nodeMap.containsKey(rootId)) {
            throw new IllegalArgumentException("NoteNode not found, id=" + rootId);
        }
        Set<Long> deleteIds = collectSubtreeIds(rootId, allNodes);

        // 3) 先清理内容表，避免留下孤儿内容数据
        noteNodeContentRepository.deleteByNodeIdIn(new ArrayList<>(deleteIds));

        // 4) 删除主节点子树（逻辑删除）
        List<NoteNode> toDeleteNodes = deleteIds.stream()
                .map(nodeMap::get)
                .filter(node -> node != null)
                .collect(Collectors.toList());
        noteNodeRepository.deleteAll(toDeleteNodes);

        // 5) 清理其他节点 pathIds 中指向已删除节点的引用，避免路径脏数据
        List<NoteNode> needCleanNodes = new ArrayList<>();
        for (NoteNode node : allNodes) {
            if (deleteIds.contains(node.getId())) {
                continue;
            }
            List<Long> currentPathIds = node.getPathIds();
            if (CollectionUtils.isEmpty(currentPathIds)) {
                continue;
            }
            List<Long> cleanedPathIds = currentPathIds.stream()
                    .filter(id -> !deleteIds.contains(id))
                    .collect(Collectors.toList());
            if (!cleanedPathIds.equals(currentPathIds)) {
                node.setPathIds(cleanedPathIds);
                needCleanNodes.add(node);
            }
        }
        if (!needCleanNodes.isEmpty()) {
            noteNodeRepository.saveAll(needCleanNodes);
        }
    }

    @Override
    @Transactional
    public NoteNodeVO get(NoteNodeRequest request) {
        // 1) 读取节点详情必须指定节点 ID
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("request.id cannot be null");
        }

        Long id = request.getId();
        // 2) 查询主节点，不存在直接抛错
        NoteNode noteNode = noteNodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NoteNode not found, id=" + id));

        // 3) 构建返回 VO：主节点 DTO + 路径 + 子节点 + 正文内容
        NoteNodeDTO noteNodeDTO = noteNodeConvert.toDTO(noteNode);
        List<NoteNodePathVO> paths = buildPaths(noteNode.getPathIds());
        List<NoteNodePathVO> childNoteNodes = noteNodeRepository.findByParentIdOrderBySortAsc(noteNode.getId())
                .stream()
                .map(child -> new NoteNodePathVO(child.getId(), child.getTitle(), child.getNoteType()))
                .collect(Collectors.toList());

        // 4) 内容优先按 JSON 解析，解析失败时保留原始字符串
        Object content = noteNodeContentRepository.findByNodeId(noteNode.getId())
                .map(NoteNodeContent::getContent)
                .map(this::toContentObject)
                .orElse(null);

        return new NoteNodeVO(noteNodeDTO, paths, childNoteNodes, content);
    }

    @Override
    @Transactional
    public List<NoteNodePathVO> searchParentNodes(String keyword, Long excludeId, Integer limit) {
        int normalizedLimit = normalizeLimit(limit);

        List<NoteNode> allNodes = noteNodeRepository.findAll();
        if (CollectionUtils.isEmpty(allNodes)) {
            return Collections.emptyList();
        }

        Set<Long> blockedIds = Collections.emptySet();
        if (excludeId != null) {
            blockedIds = collectSubtreeIds(excludeId, allNodes);
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        List<NoteNode> candidates;
        if (normalizedKeyword == null) {
            candidates = allNodes.stream()
                    .sorted((a, b) -> {
                        int sortCompare = Integer.compare(safeSort(a.getSort()), safeSort(b.getSort()));
                        if (sortCompare != 0) {
                            return sortCompare;
                        }
                        return Long.compare(safeId(a.getId()), safeId(b.getId()));
                    })
                    .collect(Collectors.toList());
        } else {
            candidates = noteNodeRepository.findByTitleContainingIgnoreCaseOrderBySortAscIdAsc(normalizedKeyword);
        }

        Map<Long, NoteNode> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(NoteNode::getId, node -> node));
        Map<Long, List<Long>> pathCache = new HashMap<>();

        List<NoteNodePathVO> results = new ArrayList<>();
        for (NoteNode node : candidates) {
            if (node == null || node.getId() == null) {
                continue;
            }
            if (blockedIds.contains(node.getId())) {
                continue;
            }
            List<Long> pathIds = node.getPathIds();
            if (CollectionUtils.isEmpty(pathIds)) {
                pathIds = buildPathIds(node.getId(), nodeMap, pathCache, new HashSet<>());
            }
            String pathTitle = toPathTitle(pathIds, nodeMap);
            String title = pathTitle.isBlank() ? node.getTitle() : pathTitle;
            results.add(new NoteNodePathVO(node.getId(), title, node.getNoteType()));
            if (results.size() >= normalizedLimit) {
                break;
            }
        }

        return results;
    }

    @Override
    @Transactional
    public List<NoteTagDTO> searchTags(String keyword, String bizType, Integer limit) {
        int normalizedLimit = normalizeLimit(limit);
        String normalizedBizType = normalizeBizType(bizType);
        String normalizedKeyword = normalizeKeyword(keyword);

        List<NoteTag> tags = normalizedKeyword == null
                ? noteTagRepository.findByBizTypeOrderByLabelAsc(normalizedBizType)
                : noteTagRepository.findByBizTypeAndLabelContainingIgnoreCaseOrderByLabelAsc(normalizedBizType, normalizedKeyword);

        if (CollectionUtils.isEmpty(tags)) {
            return Collections.emptyList();
        }

        return tags.stream()
                .filter(tag -> tag != null && tag.getId() != null)
                .limit(normalizedLimit)
                .map(this::toTagDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteTagDTO createTag(NoteTagDTO tagDTO) {
        if (tagDTO == null) {
            throw new IllegalArgumentException("tagDTO cannot be null");
        }
        String label = normalizeKeyword(tagDTO.getLabel());
        if (label == null) {
            throw new IllegalArgumentException("tagDTO.label cannot be blank");
        }

        String normalizedBizType = normalizeBizType(tagDTO.getBizType());
        NoteTag tag = noteTagRepository.findByBizTypeAndLabel(normalizedBizType, label)
                .orElseGet(() -> {
                    NoteTag created = new NoteTag();
                    created.setBizType(normalizedBizType);
                    created.setLabel(label);
                    created.setClassName(tagDTO.getClassName());
                    return noteTagRepository.save(created);
                });
        return toTagDTO(tag);
    }

    /**
     * 新增节点时，根据 parentId 计算 pathIds（根 -> 当前节点）。
     */
    private List<Long> buildPathIdsForNewNode(NoteNode node) {
        if (node == null || node.getId() == null) {
            return Collections.emptyList();
        }

        Long parentId = node.getParentId();
        if (parentId == null) {
            return new ArrayList<>(Collections.singletonList(node.getId()));
        }

        NoteNode parent = noteNodeRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent NoteNode not found, id=" + parentId));

        List<Long> parentPathIds = parent.getPathIds();
        if (CollectionUtils.isEmpty(parentPathIds)) {
            // 兜底：历史数据父节点 pathIds 缺失时，现场重算父节点路径。
            List<NoteNode> allNodes = noteNodeRepository.findAll();
            Map<Long, NoteNode> nodeMap = allNodes.stream()
                    .collect(Collectors.toMap(NoteNode::getId, n -> n));
            parentPathIds = buildPathIds(parentId, nodeMap, new HashMap<>(), new HashSet<>());
        }

        List<Long> result = new ArrayList<>(parentPathIds);
        result.add(node.getId());
        return result;
    }

    /**
     * 计算指定根节点的整棵子树ID（包含根节点自身）。
     */
    private Set<Long> collectSubtreeIds(Long rootId, List<NoteNode> allNodes) {
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (NoteNode node : allNodes) {
            if (node.getParentId() != null) {
                childrenMap.computeIfAbsent(node.getParentId(), k -> new ArrayList<>()).add(node.getId());
            }
        }

        Set<Long> subtreeIds = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (!subtreeIds.add(current)) {
                continue;
            }
            List<Long> children = childrenMap.getOrDefault(current, Collections.emptyList());
            queue.addAll(children);
        }
        return subtreeIds;
    }

    /**
     * 递归构建节点路径（根 -> 当前节点），并通过缓存减少重复计算。
     */
    private List<Long> buildPathIds(Long nodeId,
                                    Map<Long, NoteNode> nodeMap,
                                    Map<Long, List<Long>> pathCache,
                                    Set<Long> visiting) {
        if (nodeId == null) {
            return Collections.emptyList();
        }
        if (pathCache.containsKey(nodeId)) {
            return pathCache.get(nodeId);
        }
        if (!visiting.add(nodeId)) {
            // 保护：若出现循环父子关系，降级为仅当前节点，避免递归死循环。
            log.warn("Detected parent cycle while initializing pathIds, nodeId={}", nodeId);
            List<Long> cyclePath = Collections.singletonList(nodeId);
            pathCache.put(nodeId, cyclePath);
            return cyclePath;
        }

        NoteNode current = nodeMap.get(nodeId);
        if (current == null) {
            return Collections.emptyList();
        }

        List<Long> result = new ArrayList<>();
        Long parentId = current.getParentId();
        if (parentId != null && !parentId.equals(nodeId) && nodeMap.containsKey(parentId)) {
            result.addAll(buildPathIds(parentId, nodeMap, pathCache, visiting));
        }
        result.add(nodeId);

        visiting.remove(nodeId);
        pathCache.put(nodeId, result);
        return result;
    }

    private List<NoteNodePathVO> buildPaths(List<Long> pathIds) {
        // pathIds 为空时，路径直接为空集合
        if (CollectionUtils.isEmpty(pathIds)) {
            return Collections.emptyList();
        }

        // 先批量查出路径节点，再按 pathIds 原始顺序重组
        List<NoteNode> pathNodes = noteNodeRepository.findByIdIn(pathIds);
        Map<Long, NoteNode> pathNodeMap = new LinkedHashMap<>();
        for (NoteNode pathNode : pathNodes) {
            pathNodeMap.put(pathNode.getId(), pathNode);
        }

        List<NoteNodePathVO> paths = new ArrayList<>();
        for (Long pathId : pathIds) {
            NoteNode pathNode = pathNodeMap.get(pathId);
            if (pathNode != null) {
                paths.add(new NoteNodePathVO(pathNode.getId(), pathNode.getTitle(), pathNode.getNoteType()));
            }
        }
        return paths;
    }

    private String toPathTitle(List<Long> pathIds, Map<Long, NoteNode> nodeMap) {
        if (CollectionUtils.isEmpty(pathIds) || nodeMap == null || nodeMap.isEmpty()) {
            return "";
        }
        return pathIds.stream()
                .map(nodeMap::get)
                .filter(node -> node != null && node.getTitle() != null && !node.getTitle().isBlank())
                .map(NoteNode::getTitle)
                .collect(Collectors.joining(" / "));
    }

    /**
     * update 场景下手动合并 meta，避免 DTO 映射直接改写 NoteNodeMeta 主键。
     */
    private void applyMetaFromDto(NoteNode node, NoteNodeMetaDTO metaDTO) {
        if (node == null) {
            return;
        }
        if (metaDTO == null) {
            node.setMeta(null);
            return;
        }

        NoteNodeMeta meta = node.getMeta();
        if (meta == null) {
            meta = new NoteNodeMeta();
            node.setMeta(meta);
        }

        meta.setIcon(metaDTO.getIcon());
        meta.setSubject(metaDTO.getSubject());
        if (CollectionUtils.isEmpty(metaDTO.getTags())) {
            meta.setTags(new ArrayList<>());
            return;
        }

        List<NoteTag> tags = metaDTO.getTags().stream()
                .map(dto -> {
                    NoteTag tag = new NoteTag();
                    tag.setId(dto.getId());
                    tag.setBizType(dto.getBizType());
                    tag.setLabel(dto.getLabel());
                    tag.setClassName(dto.getClassName());
                    return tag;
                })
                .collect(Collectors.toList());
        meta.setTags(tags);
    }

    /**
     * 将 meta.tags 归一化为受管实体，避免仅有 id 的脱管 Tag 触发 JPA persist 异常。
     */
    private void normalizeMetaTags(NoteNode node) {
        if (node == null || node.getMeta() == null) {
            return;
        }
        List<NoteTag> incomingTags = node.getMeta().getTags();
        if (CollectionUtils.isEmpty(incomingTags)) {
            node.getMeta().setTags(new ArrayList<>());
            return;
        }

        List<NoteTag> managedTags = new ArrayList<>();
        Set<String> dedupeKeys = new HashSet<>();
        for (NoteTag tag : incomingTags) {
            NoteTag managed = resolveManagedTag(tag);
            if (managed == null) {
                continue;
            }
            String dedupeKey = managed.getId() != null
                    ? "ID:" + managed.getId()
                    : "K:" + normalizeBizType(managed.getBizType()) + ":" + normalizeKeyword(managed.getLabel());
            if (!dedupeKeys.add(dedupeKey)) {
                continue;
            }
            managedTags.add(managed);
        }
        node.getMeta().setTags(managedTags);
    }

    private NoteTag resolveManagedTag(NoteTag tag) {
        if (tag == null) {
            return null;
        }
        String incomingClassName = normalizeKeyword(tag.getClassName());
        if (tag.getId() != null) {
            NoteTag persisted = noteTagRepository.findById(tag.getId())
                    .orElseThrow(() -> new IllegalArgumentException("NoteTag not found, id=" + tag.getId()));
            if (incomingClassName != null && !incomingClassName.equals(persisted.getClassName())) {
                persisted.setClassName(incomingClassName);
                noteTagRepository.save(persisted);
            }
            return persisted;
        }

        String label = normalizeKeyword(tag.getLabel());
        if (label == null) {
            return null;
        }
        String bizType = normalizeBizType(tag.getBizType());
        NoteTag persisted = noteTagRepository.findByBizTypeAndLabel(bizType, label)
                .orElseGet(() -> {
                    NoteTag created = new NoteTag();
                    created.setBizType(bizType);
                    created.setLabel(label);
                    created.setClassName(incomingClassName);
                    return noteTagRepository.save(created);
                });
        if (incomingClassName != null && !incomingClassName.equals(persisted.getClassName())) {
            persisted.setClassName(incomingClassName);
            noteTagRepository.save(persisted);
        }
        return persisted;
    }

    private NoteTagDTO toTagDTO(NoteTag tag) {
        NoteTagDTO dto = new NoteTagDTO();
        dto.setId(tag.getId());
        dto.setBizType(tag.getBizType());
        dto.setLabel(tag.getLabel());
        dto.setClassName(tag.getClassName());
        return dto;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeBizType(String bizType) {
        String normalized = normalizeKeyword(bizType);
        return normalized == null ? DEFAULT_TAG_BIZ_TYPE : normalized;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_SEARCH_LIMIT;
        }
        return Math.min(limit, 100);
    }

    private int safeSort(Integer sort) {
        return sort == null ? 0 : sort;
    }

    private long safeId(Long id) {
        return id == null ? Long.MAX_VALUE : id;
    }

    private String toContentString(Object content) {
        // 内容已经是字符串时直接透传，避免重复序列化
        if (content == null) {
            return null;
        }
        if (content instanceof String contentString) {
            return contentString;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize note node content.", e);
        }
    }

    private Object toContentObject(String content) {
        // 空内容直接返回 null；非空优先解析成 JSON 对象
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(content, Object.class);
        } catch (JsonProcessingException e) {
            return content;
        }
    }
}
