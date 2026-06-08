package com.myblob.module.knowledge.service;

import com.myblob.module.knowledge.entity.KnowledgeItem;
import com.myblob.module.knowledge.repository.KnowledgeItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeItemRepository knowledgeItemRepository;

    @Cacheable(value = "knowledge", key = "#id")
    public KnowledgeItem getItemById(Long id) {
        return knowledgeItemRepository.findById(id).orElse(null);
    }

    @Cacheable(value = "knowledge-list", key = "#category + ':' + #page + ':' + #size")
    public Page<KnowledgeItem> getByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewCount"));
        return knowledgeItemRepository.findByCategory(category, pageable);
    }

    @Cacheable(value = "knowledge-list", key = "'hot:' + #category + ':' + #page")
    public Page<KnowledgeItem> getHotByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return knowledgeItemRepository.findByCategoryOrderByViewCountDesc(category, pageable);
    }

    @Cacheable(value = "knowledge-list", key = "'all:' + #page + ':' + #size")
    public Page<KnowledgeItem> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewCount"));
        return knowledgeItemRepository.findAll(pageable);
    }

    @Cacheable(value = "knowledge-list", key = "'search:' + #query + ':' + #page")
    public Page<KnowledgeItem> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewCount"));
        return knowledgeItemRepository.fullTextSearch(query.trim(), pageable);
    }

    @CacheEvict(value = {"knowledge", "knowledge-list"}, allEntries = true)
    @Transactional
    public void incrementViewCount(Long id) {
        knowledgeItemRepository.incrementViewCount(id);
    }

    @CacheEvict(value = {"knowledge", "knowledge-list"}, allEntries = true)
    @Transactional
    public KnowledgeItem save(KnowledgeItem item) {
        return knowledgeItemRepository.save(item);
    }
}
