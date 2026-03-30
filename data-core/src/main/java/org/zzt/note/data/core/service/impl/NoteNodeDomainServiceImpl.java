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
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.data.core.repository.INoteNodeContentRepository;
import org.zzt.note.data.core.repository.INoteNodeRepository;
import org.zzt.note.data.core.request.NoteNodeRequest;
import org.zzt.note.data.core.service.INoteNodeDomainService;
import org.zzt.note.data.core.vo.NoteNodePathVO;
import org.zzt.note.data.core.vo.NoteNodeVO;

import java.util.ArrayList;
import java.util.ArrayDeque;
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

    private final INoteNodeRepository noteNodeRepository;

    private final INoteNodeContentRepository noteNodeContentRepository;

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
        // 2) pathIds 以外层参数为准，回填到主节点 DTO
        if (noteNodeAdd.getPathIds() != null) {
            noteNodeDTO.setPathIds(noteNodeAdd.getPathIds());
        }

        // 3) 先保存节点主表（note_node）
        NoteNode noteNode = noteNodeConvert.toEntity(noteNodeDTO);
        NoteNode saved = noteNodeRepository.save(noteNode);

        // 4) 若有正文内容，再写入独立内容表（note_node_content）
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
        noteNodeConvert.updateEntityFromDto(dto, existingNode);
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
                .map(child -> new NoteNodePathVO(child.getId(), child.getTitle()))
                .collect(Collectors.toList());

        // 4) 内容优先按 JSON 解析，解析失败时保留原始字符串
        Object content = noteNodeContentRepository.findByNodeId(noteNode.getId())
                .map(NoteNodeContent::getContent)
                .map(this::toContentObject)
                .orElse(null);

        return new NoteNodeVO(noteNodeDTO, paths, childNoteNodes, content);
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
                paths.add(new NoteNodePathVO(pathNode.getId(), pathNode.getTitle()));
            }
        }
        return paths;
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
