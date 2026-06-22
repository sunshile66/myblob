package com.myblob.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 开发/测试数据种子协调器
 * 幂等：已存在该邮箱用户时跳过全部数据创建。
 */
@Slf4j
@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AccountSeeder accountSeeder;
    private final BlogSeeder blogSeeder;

    @Override
    public void run(String... args) {
        // 检查用户是否已存在
        if (accountSeeder.isSeeded()) {
            log.info("种子用户已存在，跳过数据填充。");
            return;
        }

        // 尝试升级旧版用户
        if (accountSeeder.upgradeOldUser()) {
            return;
        }

        log.info("开始填充测试数据……");

        // 创建用户
        var user = accountSeeder.createSeedUser();

        // 创建博客数据
        blogSeeder.seedBlogData(user);

        log.info("测试数据填充完成：1 用户 + 50 篇笔记。");
    }
}
