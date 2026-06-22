package com.myblob.seed;

import com.myblob.module.accounts.entity.User;
import com.myblob.module.blog.entity.Category;
import com.myblob.module.blog.entity.Post;
import com.myblob.module.blog.entity.Tag;
import com.myblob.module.blog.repository.CategoryRepository;
import com.myblob.module.blog.repository.PostRepository;
import com.myblob.module.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 博客种子数据（分类、标签、文章）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlogSeeder {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    private static final ThreadLocalRandom R = ThreadLocalRandom.current();

    /**
     * 初始化博客数据
     */
    @Transactional
    public void seedBlogData(User author) {
        // 创建默认分类
        Category category = ensureCategory("分享", "share");
        ensureCategory("技术", "tech");
        ensureCategory("生活", "life");

        // 创建常用标签
        List<Tag> allTags = new ArrayList<>();
        allTags.add(ensureTag("前端", "frontend"));
        allTags.add(ensureTag("后端", "backend"));
        allTags.add(ensureTag("设计", "design"));
        allTags.add(ensureTag("AI", "ai"));
        allTags.add(ensureTag("效率", "efficiency"));
        allTags.add(ensureTag("美食", "food"));
        allTags.add(ensureTag("旅行", "travel"));
        allTags.add(ensureTag("摄影", "photography"));
        allTags.add(ensureTag("阅读", "reading"));
        allTags.add(ensureTag("健身", "fitness"));

        // 创建 50 篇笔记
        for (int i = 1; i <= 50; i++) {
            Post post = buildPost(i, author, category, allTags);
            postRepository.save(post);
            if (i % 10 == 0) {
                postRepository.flush();
                log.info("已创建 {}/50 篇笔记", i);
            }
        }
    }

    private Category ensureCategory(String name, String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name).slug(slug).active(true).sort(0).build()));
    }

    private Tag ensureTag(String name, String slug) {
        return tagRepository.findBySlug(slug)
                .orElseGet(() -> tagRepository.save(Tag.builder()
                        .name(name).slug(slug).build()));
    }

    private Post buildPost(int index, User author, Category category, List<Tag> allTags) {
        String title = SeedData.TITLES[(index - 1) % SeedData.TITLES.length];
        String content = SeedData.CONTENTS[(index - 1) % SeedData.CONTENTS.length];
        String summary = title.length() > 60 ? title.substring(0, 57) + "..." : title + "——阅读全文了解更多。";

        Set<Tag> tags = new HashSet<>();
        int tagCount = R.nextInt(1, 4);
        List<Tag> shuffled = new ArrayList<>(allTags);
        Collections.shuffle(shuffled);
        for (int i = 0; i < tagCount; i++) {
            tags.add(shuffled.get(i));
        }

        long ts = System.currentTimeMillis() + index;
        String slug = "post-seed-" + index + "-" + ts;

        Post post = new Post();
        post.setTitle(title);
        post.setSlug(slug);
        post.setSummary(summary);
        post.setContent(content);
        post.setAuthor(author);
        post.setCategory(category);
        post.setTags(tags);
        post.setPostType(Post.PostType.NOTE);
        post.setStatus(Post.Status.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now().minusDays(R.nextInt(0, 90)));
        post.setViewCount(R.nextInt(100, 8000));
        post.setLikeCount(R.nextInt(5, 600));
        post.setCommentCount(R.nextInt(0, 120));
        post.setAllowComment(true);
        post.setOriginal(true);
        post.setTop(index <= 3);
        return post;
    }
}
