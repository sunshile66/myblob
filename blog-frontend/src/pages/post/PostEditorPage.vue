<template>
  <DefaultLayout>
    <div class="post-editor">
      <el-card class="editor-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">{{ isEdit ? "编辑文章" : "写文章" }}</span>
          </div>
        </template>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-form-item label="标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入文章标题"
              size="large"
            />
          </el-form-item>

          <el-form-item label="Slug" prop="slug">
            <el-input
              v-model="form.slug"
              placeholder="请输入文章 Slug（URL友好）"
            />
          </el-form-item>

          <el-form-item label="分类" prop="category">
            <el-select
              v-model="form.category"
              placeholder="请选择分类"
              style="width: 100%"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="标签">
            <el-select
              v-model="form.tags"
              multiple
              placeholder="请选择标签"
              style="width: 100%"
            >
              <el-option
                v-for="tag in tags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="摘要">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="3"
              placeholder="请输入文章摘要"
            />
          </el-form-item>

          <el-form-item label="内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="15"
              placeholder="请输入文章内容（支持 Markdown）"
            />
          </el-form-item>

          <el-form-item label="封面">
            <el-input v-model="form.cover" placeholder="请输入封面图片 URL" />
          </el-form-item>

          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio value="draft">草稿</el-radio>
              <el-radio value="published">发布</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="loading">
              {{ isEdit ? "更新" : "发布" }}
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import DefaultLayout from "@/layout/DefaultLayout.vue";
import {
  getCategories,
  getTags,
  createPost,
  updatePost,
  getPost,
} from "@/api/post";
import type { Post, Category, Tag } from "@/types";

const router = useRouter();
const route = useRoute();
const formRef = ref<FormInstance>();
const loading = ref(false);
const categories = ref<Category[]>([]);
const tags = ref<Tag[]>([]);

const isEdit = computed(() => !!route.params.slug);

const form = ref({
  title: "",
  slug: "",
  category: null as number | null,
  tags: [] as number[],
  summary: "",
  content: "",
  cover: "",
  status: "draft" as "draft" | "published",
});

const rules: FormRules = {
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  slug: [{ required: true, message: "请输入 Slug", trigger: "blur" }],
  content: [{ required: true, message: "请输入内容", trigger: "blur" }],
};

const loadData = async () => {
  try {
    const [catData, tagData] = await Promise.all([getCategories(), getTags()]);
    categories.value = catData;
    tags.value = tagData;

    if (isEdit.value && route.params.slug) {
      const post = await getPost(route.params.slug as string);
      form.value = {
        title: post.title,
        slug: post.slug,
        category: post.category?.id || null,
        tags: post.tags.map((t) => t.id),
        summary: post.summary || "",
        content: post.content,
        cover: post.cover || "",
        status: post.status as "draft" | "published",
      };
    }
  } catch (error) {
    console.error("Failed to load data:", error);
    ElMessage.error("加载数据失败");
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    loading.value = true;
    try {
      if (isEdit.value) {
        ElMessage.success("文章更新成功");
      } else {
        await createPost(form.value);
        ElMessage.success("文章发布成功");
      }
      router.push("/");
    } catch (error) {
      console.error("Failed to save post:", error);
      ElMessage.error("保存失败");
    } finally {
      loading.value = false;
    }
  });
};

const handleReset = () => {
  formRef.value?.resetFields();
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.post-editor {
  max-width: 900px;
  margin: 0 auto;
}

.editor-card {
  border-radius: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}
</style>
