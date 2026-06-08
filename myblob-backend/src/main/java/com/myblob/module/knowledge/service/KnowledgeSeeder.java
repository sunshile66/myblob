package com.myblob.module.knowledge.service;

import com.myblob.module.knowledge.entity.KnowledgeItem;
import com.myblob.module.knowledge.repository.KnowledgeItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeSeeder {
    private final KnowledgeItemRepository repo;

    @PostConstruct
    public void seedKnowledge() {
        List<KnowledgeItem> all = new ArrayList<>();
        addIfCategoryEmpty(all, buildProgramming(), "programming");
        addIfCategoryEmpty(all, buildMath(), "math");
        addIfCategoryEmpty(all, buildHistory(), "history");
        addIfCategoryEmpty(all, buildScience(), "science");
        addIfCategoryEmpty(all, buildLiterature(), "literature");
        addIfCategoryEmpty(all, buildPhilosophy(), "philosophy");
        addIfCategoryEmpty(all, buildBusiness(), "business");
        // 新增高级分类
        addIfCategoryEmpty(all, buildAI(), "ai");
        addIfCategoryEmpty(all, buildCybersecurity(), "cybersecurity");
        addIfCategoryEmpty(all, buildAlgorithms(), "algorithms");
        addIfCategoryEmpty(all, buildSystemDesign(), "system-design");
        if (!all.isEmpty()) {
            repo.saveAll(all);
            log.info("Seeded {} new knowledge items", all.size());
        }
    }

    private void addIfCategoryEmpty(List<KnowledgeItem> target, List<KnowledgeItem> items, String category) {
        long count = repo.countByCategory(category);
        if (count > 0) { log.info("Knowledge category {} already has {} items, skipping", category, count); return; }
        target.addAll(items);
    }

    private List<KnowledgeItem> buildProgramming() { return List.of(
        k("CAP 定理（分布式系统基础）","CAP定理是分布式系统的基石。\n\n**三个核心属性：**\n- **C (Consistency) 一致性**：所有节点看到相同数据\n- **A (Availability) 可用性**：每个请求都能收到响应\n- **P (Partition Tolerance) 分区容错**：网络分区时仍能运行\n\n**选型**：ZooKeeper/etcd→CP | Cassandra/DynamoDB→AP","programming","分布式系统,CAP,架构","计算机科学导论",3),
        k("SOLID 设计原则","面向对象设计五大原则：\n\n**S - 单一职责 (SRP)**\n**O - 开闭原则 (OCP)**：对扩展开放，对修改关闭\n**L - 里氏替换 (LSP)**\n**I - 接口隔离 (ISP)**\n**D - 依赖倒置 (DIP)**：依赖抽象而非细节","programming","设计模式,OOP,SOLID","Clean Architecture",2),
        k("HTTP 状态码速查","**2xx**：200 OK, 201 Created, 204 No Content\n**3xx**：301 永久, 302 临时, 304 缓存\n**4xx**：400 参数错误, 401 未认证, 403 无权限, 404 不存在, 429 频率过高\n**5xx**：500 内部错误, 502 网关错误, 503 服务不可用","programming","HTTP,状态码","RFC 7231",1),
        k("Git 分支模型","**核心**：main(生产) + develop(开发)\n**辅助**：feature/* + release/* + hotfix/*\n**替代**：GitHub Flow, Trunk-Based Development","programming","Git,版本控制","Vincent Driessen",2),
        k("常见算法复杂度","| 复杂度 | 场景 |\n|--------|------|\n| O(1) | 哈希表 |\n| O(log n) | 二分搜索 |\n| O(n) | 遍历 |\n| O(n log n) | 归并排序 |\n| O(n²) | 冒泡排序 |\n| O(2ⁿ) | 递归斐波那契 |","programming","算法,时间复杂度","CLRS",3),
        k("RESTful API 设计最佳实践","**URL设计**：名词复数`/users`，嵌套`/users/1/posts`\n**方法语义**：GET查询 | POST创建 | PUT全量更新 | PATCH部分更新 | DELETE删除\n**分页**：`?page=1&size=20`\n**版本控制**：URL前缀`/api/v1/`或Header","programming","REST,API设计,HTTP","RESTful Web APIs",2),
        k("Docker 核心概念","**三大核心**：Image(模板) → Container(运行实例) → Registry(仓库)\n**常用命令**：`docker build -t app .` / `docker run -d -p 8080:80 app` / `docker compose up -d`\n**最佳实践**：多阶段构建、合并RUN、利用缓存","programming","Docker,容器化,DevOps","Docker文档",2),
        k("OAuth 2.0 四种授权模式","**1. 授权码模式**：最安全，适用Web应用\n**2. 隐式模式**：纯前端，已不推荐\n**3. 密码模式**：仅高信任应用\n**4. 客户端凭证**：服务间通信\n\nToken：Access Token(短期) + Refresh Token(长期)","programming","OAuth,认证,安全","RFC 6749",3),
        k("观察者模式","**意图**：一对多依赖，状态变化时通知所有观察者\n**结构**：Subject维护列表 + Observer定义update接口\n**应用**：Vue/React响应式、事件监听、消息队列Pub/Sub、RxJS Observable","programming","设计模式,观察者,事件驱动","GoF",2),
        k("数据库索引原理与优化","**类型**：B+树(MySQL默认) | 哈希(等值O(1)) | 全文\n**优化原则**：WHERE/JOIN/ORDER BY列建索引，联合索引最左前缀\n**失效场景**：LIKE '%x'左模糊、OR有非索引列、隐式类型转换","programming","数据库,索引,SQL优化","MySQL文档",3)
    );}

    private List<KnowledgeItem> buildMath() { return List.of(
        k("欧拉公式","$$e^{i\\pi} + 1 = 0$$\n将五个最重要常数联系：e、i、π、1、0\n推导：由 e^(iθ)=cosθ+i·sinθ 代入θ=π","math","欧拉公式,数学之美","Euler, 1748",3),
        k("费马小定理","p为质数，a与p互素：$$a^{p-1} \\equiv 1 \\pmod{p}$$\n**应用**：RSA加密、快速幂取模、Miller-Rabin素数测试","math","数论,密码学","Fermat, 1640",4),
        k("PCA 主成分分析","最常用降维算法。步骤：标准化→协方差矩阵→特征值分解→选前k特征向量→投影\n**应用**：图像压缩、人脸识别、数据可视化","math","机器学习,降维,线性代数","Pearson, 1901",4),
        k("贝叶斯定理","$$P(A|B) = \\frac{P(B|A) \\cdot P(A)}{P(B)}$$\n后验=似然×先验/归一化\n**悖论**：检测准确率99%，发病率1/万，阳性实际患病仅约0.98%","math","概率论,贝叶斯,统计","Bayes, 1763",3),
        k("傅里叶变换","$$F(\\omega) = \\int f(t)e^{-i\\omega t}dt$$\n任何信号=不同频率正弦波叠加\n**应用**：音频频谱分析、JPEG图像压缩(DCT)、信号滤波、量子力学","math","傅里叶,信号处理","Fourier, 1822",4),
        k("矩阵分解","**LU分解**：A=LU，求解线性方程组\n**QR分解**：A=QR，最小二乘问题\n**SVD**：A=UΣVᵀ——最重要！应用：推荐系统、图像压缩、PCA\n**特征值分解**：A=PΛP⁻¹（方阵）","math","线性代数,矩阵,SVD","Strang",4),
        k("微积分基本定理","$$\\int_a^b f(x)dx = F(b)-F(a)$$\n连接微分与积分：找原函数即可求定积分\n推广：格林公式、斯托克斯公式、高斯散度定理","math","微积分,基本定理","Newton & Leibniz",2)
    );}

    private List<KnowledgeItem> buildHistory() { return List.of(
        k("中国朝代顺序歌","夏商与西周，东周分两段。春秋和战国，一统秦两汉。\n三分魏蜀吴，二晋前后沿。南北朝并立，隋唐五代传。\n宋元明清后，皇朝至此完。","history","中国历史,朝代","教科书",1),
        k("丝绸之路","古代东西方贸易路线，约6400公里\n陆上：长安→河西走廊→西域→中亚→波斯→地中海\n海上：泉州/广州→东南亚→印度洋→红海\n丝绸、瓷器、茶叶 ↔ 玻璃、香料、宝石","history","丝绸之路,贸易,文化交流","历史文献",2),
        k("工业革命四阶段","**第一次(1760s)**：蒸汽机→机械化\n**第二次(1870s)**：电力→大规模生产\n**第三次(1960s)**：计算机→信息时代\n**第四次(2010s)**：AI/IoT/5G→智能生产","history","工业革命,科技史","世界经济论坛",2),
        k("古希腊三大哲学家","**苏格拉底**：产婆术(反问法)，「我知道我什么都不知道」\n**柏拉图**：理型论，创建雅典学院，《理想国》\n**亚里士多德**：百科全书式学者，三段论，亚历山大帝师","history","古希腊,哲学","哲学史",2),
        k("大航海时代(15-17世纪)","奥斯曼控制陆路，欧洲寻新航线\n1492哥伦布→美洲 | 1498达伽马→印度 | 1519麦哲伦→环球\n1405-1433郑和七下西洋（早欧洲半世纪）\n影响：全球化开始、殖民兴起、物种大交换","history","大航海,全球化,殖民","世界近代史",2),
        k("中国古代四大发明","**造纸术**(东汉蔡伦105年)→取代竹简\n**印刷术**(北宋毕昇约1040年)→活字，早谷登堡400年\n**火药**(唐代约9世纪)→炼丹→军事\n**指南针**(战国司南→北宋罗盘)→远洋航行\n经阿拉伯传入欧洲，推动文艺复兴","history","四大发明,中国,科技史","中国科技史",1)
    );}

    private List<KnowledgeItem> buildScience() { return List.of(
        k("相对论","**狭义(1905)**：光速不变、时间膨胀、E=mc²\n**广义(1915)**：引力=时空弯曲，预测引力波(2015LIGO证实)、黑洞\nGPS卫星须修正相对论效应，否则日误差10km","science","相对论,物理,爱因斯坦","Einstein",4),
        k("DNA双螺旋","Watson & Crick 1953年发现\n双螺旋右旋，反向平行，A=T(二氢键) G≡C(三氢键)\n中心法则：DNA→RNA→蛋白质\n人类基因组：约30亿碱基对，约2万蛋白编码基因","science","DNA,分子生物学,遗传","Nature, 1953",3),
        k("元素周期表规律","按原子序数排列，性质周期性\n周期(横)：左→右金属性减弱\n族(纵)：上→下金属性增强\n碱金属：氢锂钠钾铷铯钫 | 卤素：氟氯溴碘砹","science","化学,元素周期表","Mendeleev, 1869",2),
        k("量子力学入门","**波粒二象性**：微观粒子同时具有波动和粒子性\n**不确定性原理**：Δx·Δp ≥ ℏ/2\n**量子叠加**：粒子可同时处于多状态(薛定谔的猫)\n**量子纠缠**：测量一个粒子瞬间影响另一个\n应用：半导体、激光、MRI、量子计算","science","量子力学,物理","量子物理导论",4),
        k("进化论与自然选择","达尔文1859《物种起源》：变异→遗传→竞争→自然选择\n现代综合：基因突变提供变异，自然选择定方向\n证据：化石、同源结构、DNA比较、抗生素耐药性","science","进化论,自然选择,达尔文","Darwin, 1859",2),
        k("热力学三大定律","**第零定律**：热平衡传递性（温度定义基础）\n**第一定律**：能量守恒 ΔU=Q-W\n**第二定律**：孤立系统熵永不减少（热量不能自发从低温到高温）\n**第三定律**：绝对零度不可达到\n启示：宇宙终极可能是「热寂」","science","热力学,熵,能量守恒","物理教科书",3)
    );}

    private List<KnowledgeItem> buildLiterature() { return List.of(
        k("莎士比亚四大悲剧","**《哈姆雷特》**：丹麦王子复仇，「To be or not to be」\n**《奥赛罗》**：嫉妒与信任\n**《李尔王》**：权力与亲情\n**《麦克白》**：野心与罪恶\n共同主题：人性弱点、命运无常、道德困境","literature","莎士比亚,悲剧,英国文学","Shakespeare",2),
        k("中国古典四大名著","**《红楼梦》**曹雪芹：宝黛爱情，封建家族兴衰\n**《三国演义》**罗贯中：魏蜀吴争霸\n**《水浒传》**施耐庵：108好汉梁山聚义\n**《西游记》**吴承恩：师徒取经，孙悟空自由精神","literature","四大名著,中国文学","中国文学史",1),
        k("意识流文学代表作","模拟思维自然流动，打破线性叙事\n**《尤利西斯》**Joyce：一天都柏林=奥德修斯十年\n**《追忆似水年华》**Proust：七卷本，无意识记忆\n**《到灯塔去》**Woolf：时间与意识\n**《喧哗与骚动》**Faulkner：四视角南方家族","literature","意识流,现代主义,小说","文学理论",4),
        k("唐诗·诗仙李白","李白(701-762)，字太白，号青莲居士\n《静夜思》「床前明月光」——最广传唐诗\n《将进酒》「天生我材必有用」——豪放\n《蜀道难》「蜀道之难难于上青天」——浪漫\n风格：豪放飘逸，想象奇特，善用夸张\n与杜甫并称「李杜」","literature","唐诗,李白,古典诗歌","唐诗三百首",1),
        k("魔幻现实主义文学","现实中融入魔幻元素，模糊现实与幻想\n**《百年孤独》**马尔克斯：布恩迪亚家族七代，拉美历史缩影\n**《佩德罗·巴拉莫》**鲁尔福：先驱\n博尔赫斯、阿连德、莫言《红高粱》","literature","魔幻现实主义,拉美文学,百年孤独","文学流派",3),
        k("电影蒙太奇手法","通过镜头组接创造新含义\n**叙事蒙太奇**：按时间推进\n**对比蒙太奇**：贫富交替\n**平行蒙太奇**：同时空交替(悬疑常用)\n**隐喻蒙太奇**：画面比喻抽象概念\n经典：《教父》洗礼与杀戮平行剪辑","literature","电影,蒙太奇,艺术","电影艺术导论",3)
    );}

    private List<KnowledgeItem> buildPhilosophy() { return List.of(
        k("存在主义哲学","「存在先于本质」——人先存在，通过选择定义自己\n**萨特**：人被判定为自由的，「他人即地狱」\n**加缪**：荒诞哲学，必须想象西西弗斯是幸福的\n**克尔凯郭尔**：先驱，人生三阶段——审美→伦理→宗教","philosophy","存在主义,萨特,加缪,自由","哲学史",3),
        k("常见逻辑谬误","**形式谬误**：肯定后件、否定前件\n**非形式谬误**：\n- 稻草人：曲解对方论点\n- 滑坡：「如果A就会B就会C…」\n- 诉诸权威/情感\n- 非黑即白\n- 循环论证\n- 沉没成本","philosophy","逻辑学,谬误,批判性思维","逻辑学导论",2),
        k("功利主义 vs 义务论","**功利主义**(边沁/密尔)：结果最大化幸福，「最大多数人的最大幸福」\n**义务论**(康德)：行为本身有道德属性，「只按你希望它成为普遍法则的准则行动」\n应用：公共政策(功利) vs 医学伦理(义务)","philosophy","伦理学,功利主义,康德","伦理学导论",3),
        k("中国古代哲学：儒道法墨","**儒家**：仁义礼智信，「己所不欲勿施于人」，德治\n**道家**：道法自然，无为而治，「道可道非常道」\n**法家**：以法治国，赏罚分明，人性本恶\n**墨家**：兼爱非攻，尚贤节用","philosophy","中国哲学,儒道法墨,先秦","中国哲学史",2),
        k("博弈论：囚徒困境与纳什均衡","**囚徒困境**：都沉默各1年，都检举各5年，一方检举释放/沉默10年\n理性选择→都检举→各5年（非最优！）\n**纳什均衡**：无人能单方面改变策略提高收益\n应用：军备竞赛、价格战、公地悲剧","philosophy","博弈论,纳什均衡,囚徒困境","Nash, 1950",3),
        k("认知偏差","**确认偏差**：只找支持已有信念的证据\n**锚定效应**：过度依赖第一个信息\n**可得性偏差**：容易想到的=常见的\n**沉没成本**：因已投入而继续错误\n**达克效应**：能力不足者高估自己\n**幸存者偏差**：忽略失败者\n行为经济学基础(卡尼曼《思考快与慢》)","philosophy","认知偏差,心理学,行为经济学","Kahneman, 2011",2)
    );}

    private List<KnowledgeItem> buildBusiness() { return List.of(
        k("供需理论与市场均衡","**需求定律**：价格↑→需求量↓\n**供给定律**：价格↑→供给量↑\n**均衡价格**：供需交点\n**弹性**：>1富有弹性(奢侈品)，<1缺乏弹性(必需品)\n应用：涨价策略(弹性低)、促销策略(弹性高)","business","供需,微观经济学,市场","经济学原理",2),
        k("SWOT 分析框架","**S**trengths(优势)：内部正面因素\n**W**eaknesses(劣势)：内部负面因素\n**O**pportunities(机会)：外部正面因素\n**T**hreats(威胁)：外部负面因素\n矩阵组合：SO(增长)、WO(转型)、ST(多元化)、WT(防御)","business","SWOT,战略分析,管理","管理学基础",1),
        k("波特五力模型","分析行业竞争格局：\n1. **行业内竞争**：现有企业间竞争强度\n2. **新进入者威胁**：进入壁垒高低\n3. **替代品威胁**：替代产品/服务\n4. **买方议价能力**：客户压价能力\n5. **供方议价能力**：供应商涨价能力\n五力决定行业盈利能力","business","波特五力,竞争战略,行业分析","Porter, 1979",2),
        k("财务报表三张核心报表","**资产负债表**：资产=负债+所有者权益（某一时点的快照）\n**利润表**：收入-成本-费用=利润（一段时间的录像）\n**现金流量表**：经营/投资/筹资三类现金流\n\n关键指标：ROE(净资产收益率)、资产负债率、毛利率、经营现金流/净利润","business","财务报表,会计,投资","财务报表分析",2),
        k("商业模式画布 (Business Model Canvas)","九大模块：\n1. **客户细分**：目标用户是谁\n2. **价值主张**：解决什么问题\n3. **渠道通路**：如何触达客户\n4. **客户关系**：如何维系客户\n5. **收入来源**：如何赚钱\n6. **核心资源**：需要什么资产\n7. **关键业务**：需要做什么\n8. **重要合作**：需要谁帮助\n9. **成本结构**：花多少钱","business","商业模式,创业,画布","Osterwalder",2),
        k("凯恩斯主义 vs 古典经济学","**古典经济学**(亚当·斯密)：\n- 「看不见的手」——市场自发调节\n- 供给创造需求(萨伊定律)\n- 政府应最小干预\n\n**凯恩斯主义**(1936)：\n- 经济衰退时总需求不足是主因\n- 政府应通过财政政策(增加支出/减税)刺激需求\n- 乘数效应：1元政府支出→多倍GDP增长\n\n**现代应用**：2008金融危机各国大规模刺激政策即为凯恩斯主义","business","宏观经济学,凯恩斯,财政政策","经济学思想史",3)
    );}

    private static KnowledgeItem k(String title, String content, String category,
            String tags, String source, int difficulty) {
        return KnowledgeItem.builder().title(title).content(content)
                .summary(content.length() > 120 ? content.substring(0, 120) + "..." : content)
                .category(category).tags(tags).source(source).difficulty(difficulty).viewCount(0L).build();
    }

    // ==================== 新增高级分类 ====================

    private List<KnowledgeItem> buildAI() { return List.of(
        k("Transformer架构详解","**自注意力机制(Self-Attention)**：\n- Q/K/V矩阵：Query, Key, Value\n- 注意力分数 = softmax(QK^T/sqrt(d_k))V\n- 多头注意力：并行学习不同子空间\n\n**位置编码**：正弦/余弦函数或可学习位置嵌入\n\n**编码器-解码器结构**：\n- 编码器：N层堆叠，每层含自注意力+FFN\n- 解码器：掩码自注意力+交叉注意力+FFN\n\n**关键创新**：并行计算、长距离依赖、迁移学习","ai","Transformer,注意力机制,NLP","Vaswani et al., 2017",4),
        k("大语言模型(LLM)原理","**预训练目标**：\n- 自回归(GPT)：预测下一个token\n- 自编码(BERT)：掩码语言模型\n- Seq2Seq(T5)：文本到文本\n\n**缩放定律(Scaling Laws)**：\n- 模型大小、数据量、计算量的幂律关系\n- Chinchilla最优：数据量约20倍参数量\n\n**涌现能力**：\n- 上下文学习(In-Context Learning)\n- 思维链推理(Chain-of-Thought)\n- 指令遵循(Instruction Following)\n\n**RLHF**：人类反馈强化学习对齐","ai","LLM,GPT,BERT,RLHF","OpenAI/Anthropic",5),
        k("卷积神经网络(CNN)","**核心操作**：\n- 卷积层：特征提取，参数共享\n- 池化层：降维，最大/平均池化\n- 全连接层：分类决策\n\n**经典架构**：\n- LeNet - AlexNet - VGG - ResNet - EfficientNet\n- 残差连接：解决梯度消失\n\n**应用**：\n- 图像分类、目标检测(YOLO)、语义分割\n- 医学影像、自动驾驶、人脸识别","ai","CNN,深度学习,计算机视觉","LeCun, 1998",3),
        k("生成对抗网络(GAN)","**原理**：生成器G vs 判别器D的博弈\n- G：生成假数据骗过D\n- D：区分真假数据\n- 纳什均衡：G生成的数据分布=真实分布\n\n**训练技巧**：\n- 模式崩塌：G只生成少数样本\n- 训练不稳定：WGAN、谱归一化\n- 条件生成：cGAN、StyleGAN\n\n**应用**：\n- 图像生成、超分辨率、风格迁移\n- 数据增强、深度伪造(Deepfake)\n- 文本到图像(DALL-E, Midjourney)","ai","GAN,生成模型,深度学习","Goodfellow, 2014",4),
        k("强化学习(RL)入门","**核心概念**：\n- Agent/Environment/State/Action/Reward\n- 马尔可夫决策过程(MDP)\n- 折扣累积奖励\n\n**算法分类**：\n- 基于价值：Q-Learning, DQN, Double DQN\n- 基于策略：REINFORCE, PPO, A3C\n- Actor-Critic：A2C, SAC, TD3\n\n**应用**：\n- 游戏AI(AlphaGo, Atari)\n- 机器人控制、自动驾驶\n- 推荐系统、资源调度","ai","强化学习,MDP,PPO","Sutton & Barto",4)
    );}

    private List<KnowledgeItem> buildCybersecurity() { return List.of(
        k("常见Web攻击与防御","**XSS(跨站脚本)**：\n- 反射型：URL注入脚本\n- 存储型：数据库存储恶意脚本\n- 防御：输入验证、CSP、HttpOnly Cookie\n\n**CSRF(跨站请求伪造)**：\n- 诱导用户执行非本意操作\n- 防御：SameSite Cookie、CSRF Token\n\n**SQL注入**：\n- 拼接SQL执行恶意查询\n- 防御：参数化查询、ORM、最小权限\n\n**SSRF(服务端请求伪造)**：\n- 防御：白名单、禁用内网访问","cybersecurity","Web安全,XSS,CSRF,SQL注入","OWASP Top 10",3),
        k("密码学基础","**对称加密**：\n- AES(推荐)：128/192/256位密钥\n- ChaCha20：移动端友好\n- 模式：GCM(认证加密) > CBC\n\n**非对称加密**：\n- RSA：2048位以上，用于密钥交换\n- ECC：更短密钥同等安全\n- Ed25519：签名首选\n\n**哈希函数**：\n- SHA-256/SHA-3：密码存储\n- bcrypt/scrypt/Argon2：密码哈希(加盐+慢哈希)\n- HMAC：消息认证码\n\n**密钥管理**：KMS、HSM、密钥轮转","cybersecurity","密码学,AES,RSA,哈希","密码学工程",4),
        k("渗透测试方法论","**五阶段**：\n1. 侦察(Reconnaissance)：信息收集\n2. 扫描(Scanning)：端口/漏洞扫描\n3. 获取访问(Gaining Access)：漏洞利用\n4. 维持访问(Maintaining Access)：后门\n5. 清除痕迹(Covering Tracks)：日志清理\n\n**常用工具**：\n- Nmap：端口扫描\n- Burp Suite：Web漏洞扫描\n- Metasploit：漏洞利用框架\n- Wireshark：流量分析\n- John/Hashcat：密码破解\n\n**合法边界**：授权测试、范围约定、漏洞披露","cybersecurity","渗透测试,漏洞扫描,安全","PTES",4),
        k("零信任安全架构","**核心原则**：\n- 永不信任，始终验证\n- 最小权限访问\n- 假设已被入侵\n\n**三大支柱**：\n1. 身份验证：MFA、FIDO2、无密码\n2. 设备信任：端点检测、合规检查\n3. 网络分段：微分段、SASE\n\n**实现路径**：\n- SSO + MFA + RBAC/ABAC\n- API网关 + 服务网格\n- 持续监控 + 自动响应\n\n**框架**：NIST SP 800-207","cybersecurity","零信任,安全架构,MFA","NIST",4)
    );}

    private List<KnowledgeItem> buildAlgorithms() { return List.of(
        k("动态规划(DP)核心思想","**本质**：最优子结构 + 重叠子问题\n\n**解题框架**：\n1. 定义状态：dp[i]的含义\n2. 状态转移：dp[i] = f(dp[i-1], ...)\n3. 初始化：base case\n4. 遍历顺序\n\n**经典问题**：\n- 背包问题：0-1/完全/多重\n- 最长公共子序列(LCS)\n- 最长递增子序列(LIS)\n- 编辑距离\n- 区间DP：矩阵链乘法\n\n**优化**：\n- 滚动数组空间优化\n- 记忆化搜索(自顶向下)","algorithms","动态规划,背包,LeetCode","CLRS",3),
        k("图论算法","**遍历**：\n- BFS：层序遍历，最短路径(无权图)\n- DFS：深度优先，连通性、拓扑排序\n\n**最短路径**：\n- Dijkstra：非负权图，O((V+E)logV)\n- Bellman-Ford：负权图，O(VE)\n- Floyd-Warshall：全源最短路，O(V^3)\n- SPFA：队列优化Bellman-Ford\n\n**最小生成树**：\n- Prim：稠密图，O(ElogV)\n- Kruskal：稀疏图，O(ElogE)\n\n**高级**：\n- 网络流：Ford-Fulkerson, Dinic\n- 二分图匹配：匈牙利算法","algorithms","图论,最短路径,LeetCode","算法导论",4),
        k("高级数据结构","**树结构**：\n- 红黑树：Java TreeMap/C++ map\n- B+树：数据库索引\n- 字典树(Trie)：字符串前缀匹配\n- 线段树：区间查询O(logN)\n- 树状数组(Binary Indexed Tree)\n\n**并查集(Union-Find)**：\n- 路径压缩 + 按秩合并\n- 应用：连通性、Kruskal算法\n\n**堆**：\n- 二叉堆：优先队列\n- 斐波那契堆：Dijkstra优化\n- 配对堆：实践中最快\n\n**跳表(Skip List)**：\n- Redis有序集合底层实现\n- 期望O(logN)查找","algorithms","数据结构,红黑树,线段树","数据结构与算法",4),
        k("排序算法对比","**比较排序下界**：O(NlogN)\n\n| 算法 | 平均 | 最坏 | 空间 | 稳定 |\n|------|------|------|------|------|\n| 快速排序 | NlogN | N^2 | logN | 否 |\n| 归并排序 | NlogN | NlogN | N | 是 |\n| 堆排序 | NlogN | NlogN | 1 | 否 |\n| TimSort | NlogN | NlogN | N | 是 |\n\n**非比较排序**：\n- 计数排序：O(N+K)，整数\n- 基数排序：O(dN)，定长整数\n- 桶排序：O(N)，均匀分布\n\n**实践选择**：\n- Java：双轴快排(基本类型) / TimSort(对象)\n- C++：IntroSort\n- Python：TimSort","algorithms","排序,算法复杂度,LeetCode","算法导论",2),
        k("字符串算法","**KMP算法**：\n- 失配函数(前缀函数)：O(M)构建\n- 匹配：O(N+M)，不回溯主串\n- 应用：模式匹配、循环节\n\n**后缀数组/后缀树**：\n- 后缀数组：所有后缀排序\n- 最长公共前缀(LCP)\n- 应用：最长重复子串、不同子串计数\n\n**AC自动机**：\n- Trie + 失配指针\n- 多模式匹配O(N+M+Z)\n\n**Manacher算法**：\n- 最长回文子串O(N)\n- 应用：回文检测","algorithms","字符串,KMP,后缀数组","算法竞赛",4)
    );}

    private List<KnowledgeItem> buildSystemDesign() { return List.of(
        k("分布式系统设计原则","**CAP定理回顾**：\n- CP：ZooKeeper, etcd, HBase\n- AP：Cassandra, DynamoDB, CouchDB\n- 实际：多数系统在CA之间权衡\n\n**一致性模型**：\n- 强一致：Raft, Paxos, ZAB\n- 最终一致：Gossip协议\n- 因果一致：向量时钟\n\n**分布式ID生成**：\n- Snowflake：时间戳+机器ID+序列号\n- UUID：128位，无序\n- 数据库自增：简单但单点\n\n**幂等性设计**：\n- 唯一请求ID\n- 状态机约束","system-design","分布式,CAP,一致性","DDIA",4),
        k("微服务架构","**核心原则**：\n- 单一职责：每个服务独立部署\n- 服务自治：独立数据库\n- 去中心化治理：技术栈自由\n\n**服务通信**：\n- 同步：gRPC(高性能), REST(简单)\n- 异步：Kafka(高吞吐), RabbitMQ(低延迟)\n\n**服务治理**：\n- 服务发现：Consul, Nacos, Eureka\n- 负载均衡：Ribbon, Envoy\n- 熔断降级：Hystrix, Resilience4j, Sentinel\n- 链路追踪：Jaeger, Zipkin, SkyWalking\n\n**数据一致性**：\n- Saga模式：编排 vs 协同\n- 事件溯源：Event Sourcing\n- CQRS：读写分离","system-design","微服务,gRPC,服务治理","微服务架构设计",4),
        k("高并发系统设计","**限流算法**：\n- 固定窗口：简单但有边界突发\n- 滑动窗口：更平滑\n- 令牌桶：允许突发\n- 漏桶：恒定速率\n\n**缓存策略**：\n- 缓存穿透：布隆过滤器、空值缓存\n- 缓存击穿：互斥锁、永不过期\n- 缓存雪崩：随机过期时间\n- 一致性：Cache Aside / Read Through / Write Behind\n\n**数据库优化**：\n- 读写分离：主从复制\n- 分库分表：ShardingSphere\n- 索引优化：覆盖索引、联合索引\n\n**消息队列**：\n- 削峰填谷、异步解耦\n- 顺序消息、延迟消息、死信队列","system-design","高并发,限流,缓存","架构设计",4),
        k("容器编排与云原生","**Kubernetes核心概念**：\n- Pod：最小部署单元\n- Service：服务发现+负载均衡\n- Deployment：声明式部署\n- StatefulSet：有状态服务\n\n**服务网格(Service Mesh)**：\n- Istio/Linkerd：边车代理\n- 流量管理、安全、可观测\n\n**可观测性三支柱**：\n- Metrics：Prometheus + Grafana\n- Logs：ELK Stack / Loki\n- Traces：Jaeger / Tempo\n\n**CI/CD**：\n- GitOps：ArgoCD, Flux\n- 蓝绿/金丝雀/滚动发布\n- 特性开关(Feature Flag)","system-design","Kubernetes,云原生,DevOps","CNCF",4)
    );}
}
