package com.myblob.module.interactions.service;

import com.myblob.common.BusinessException;
import com.myblob.common.PageResponse;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.interactions.dto.BoardMessageDTO;
import com.myblob.module.interactions.dto.CreateBoardMessageRequest;
import com.myblob.module.interactions.dto.NotificationDTO;
import com.myblob.module.interactions.entity.BoardMessage;
import com.myblob.module.interactions.entity.Notification;
import com.myblob.module.interactions.repository.BoardMessageRepository;
import com.myblob.module.interactions.repository.FavoriteRepository;
import com.myblob.module.interactions.repository.NotificationRepository;
import com.myblob.module.interactions.repository.PostLikeRepository;
import com.myblob.module.blog.dto.PostDTOAssembler;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final PostLikeRepository postLikeRepository;
    private final FavoriteRepository favoriteRepository;
    private final BoardMessageRepository boardMessageRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<BoardMessageDTO> getBoardMessages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BoardMessageDTO> messages = boardMessageRepository.findByIsPublicTrueOrderByIdDesc(pageable)
                .map(this::toBoardMessageDTO);
        return PageResponse.of(messages);
    }

    @Transactional
    public BoardMessageDTO createBoardMessage(CreateBoardMessageRequest request) {
        Long userId = SecurityUtil.getCurrentUserIdOrNull();
        BoardMessage.BoardMessageBuilder builder = BoardMessage.builder()
                .nickname(request.getNickname())
                .email(request.getEmail() != null ? request.getEmail() : "")
                .content(request.getContent())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .deleted(false);

        if (userId != null) {
            userRepository.findById(userId).ifPresent(builder::user);
        }

        BoardMessage message = boardMessageRepository.save(builder.build());
        return toBoardMessageDTO(message);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationDTO> getNotifications(int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDTO> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toNotificationDTO);
        return PageResponse.of(notifications);
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount() {
        Long userId = SecurityUtil.getCurrentUserId();
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllNotificationsRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationRepository.markAllRead(userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<com.myblob.module.blog.dto.PostDTO> getMyFavorites(int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<com.myblob.module.blog.dto.PostDTO> favorites = favoriteRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(f -> PostDTOAssembler.toBasicDTO(f.getPost()));
        return PageResponse.of(favorites);
    }

    private BoardMessageDTO toBoardMessageDTO(BoardMessage message) {
        return BoardMessageDTO.builder()
                .id(message.getId())
                .userId(message.getUser() != null ? message.getUser().getId() : null)
                .nickname(message.getNickname())
                .email(message.getEmail())
                .content(message.getContent())
                .isPublic(message.getIsPublic())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private NotificationDTO toNotificationDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType().name().toLowerCase())
                .content(notification.getContent())
                .isRead(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
