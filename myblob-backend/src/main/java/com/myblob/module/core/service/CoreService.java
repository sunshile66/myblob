package com.myblob.module.core.service;

import com.myblob.module.core.entity.Ad;
import com.myblob.module.core.entity.Announcement;
import com.myblob.module.core.entity.SiteConfig;
import com.myblob.module.core.repository.AdRepository;
import com.myblob.module.core.repository.AnnouncementRepository;
import com.myblob.module.core.repository.SiteConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CoreService {

    private final AnnouncementRepository announcementRepository;
    private final AdRepository adRepository;
    private final SiteConfigRepository siteConfigRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActiveAnnouncements() {
        return announcementRepository
                .findByActiveTrueAndPublishTimeBeforeOrderByPinnedDescSortAscPublishTimeDesc(LocalDateTime.now())
                .stream().map(this::toAnnouncementMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActiveAds(String position) {
        return adRepository.findByActiveTrueOrderByPositionAscSortAscCreatedAtDesc()
                .stream()
                .filter(ad -> position == null || ad.getPosition().equals(position))
                .filter(ad -> {
                    LocalDateTime now = LocalDateTime.now();
                    if (ad.getStartTime() != null && now.isBefore(ad.getStartTime())) return false;
                    if (ad.getEndTime() != null && now.isAfter(ad.getEndTime())) return false;
                    return true;
                })
                .map(this::toAdMap)
                .toList();
    }

    @Transactional
    public void recordAdClick(Long adId) {
        adRepository.findById(adId).ifPresent(ad -> {
            ad.setClickCount(ad.getClickCount() + 1);
            adRepository.save(ad);
        });
    }

    @Transactional(readOnly = true)
    public Map<String, String> getSiteConfig() {
        Map<String, String> config = new LinkedHashMap<>();
        siteConfigRepository.findAll().forEach(c -> config.put(c.getKey(), c.getValue()));
        return config;
    }

    private Map<String, Object> toAnnouncementMap(Announcement a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.getId());
        map.put("title", a.getTitle());
        map.put("content", a.getContent());
        map.put("announcement_type", a.getAnnouncementType().name().toLowerCase());
        map.put("is_active", a.getActive());
        map.put("is_pinned", a.getPinned());
        map.put("sort", a.getSort());
        map.put("publish_time", a.getPublishTime());
        map.put("show_delay", a.getShowDelay());
        map.put("auto_close", a.getAutoClose());
        map.put("auto_close_time", a.getAutoCloseTime());
        map.put("created_at", a.getCreatedAt());
        map.put("updated_at", a.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toAdMap(Ad ad) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", ad.getId());
        map.put("title", ad.getTitle());
        map.put("image", ad.getImage());
        map.put("link", ad.getLink());
        map.put("position", ad.getPosition());
        map.put("is_active", ad.getActive());
        map.put("sort", ad.getSort());
        map.put("start_time", ad.getStartTime());
        map.put("end_time", ad.getEndTime());
        map.put("click_count", ad.getClickCount());
        map.put("created_at", ad.getCreatedAt());
        map.put("updated_at", ad.getUpdatedAt());
        return map;
    }
}
