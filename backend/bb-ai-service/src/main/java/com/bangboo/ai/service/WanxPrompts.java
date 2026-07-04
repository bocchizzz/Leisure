package com.bangboo.ai.service;

import com.bangboo.ai.dto.BangbooAvatarRequest;

import java.util.Map;

/**
 * 文生图提示词构造工具（通义万相）。
 * 为任务封面与邦布头像分别构造高质量中文提示词与反向提示词。
 * 美术风格统一对齐《绝区零（Zenless Zone Zero）》，画面角色为其中的「邦布（Bangboo）」形象。
 */
public final class WanxPrompts {

    private WanxPrompts() {
    }

    /**
     * 《绝区零》整体美术风格描述（用于所有生成图，保证风格统一）。
     * 参考游戏视觉：都市潮流、赛博朋克、街头涂鸦、高饱和撞色、强烈光影与霓虹。
     */
    private static final String ZZZ_ART_STYLE =
                    "1. 画面以大字文案为主，邦布形象为辅。\n" +
                    "2. 主文案要直接表达用户的求助、吐槽、委托或愿望。\n" +
                    "3. 文案占据画面主要视觉中心，可分成 2~4 行。 \n" +
                    "4. 画面只出现 1 个原创邦布形象，放在下方或角落，作为辅助表达情绪的小角色。 \n" +
                    "5. 邦布需要可爱、简洁、圆润，兔耳但不要复刻官方角色。 \n" +
                    "6. 背景使用浅色系的纯色背景。 \n" +
                    "7. 色彩尽量克制，只使用： - 背景：浅色系 - 文字：黑色 / 深灰 - 点缀：1 种小面积强调色 \n" +
                    "8. 不要复杂背景，不要复杂构图，不要过多装饰，不要多角色，不要有小字，直接打字强调。 \n" +
                    "9. 整体风格像“简约求助卡 / 委托情绪卡 / 文字海报”。 \n" +
                    "10. 让人一眼看懂用户当前的意图。 \n" +
                    "输出效果： 一张简约、留白多、文字突出、带有轻微幽默感和情绪感的邦布主题插图海报，需要的是横版的！。";

    /**
     * 「邦布（Bangboo）」标准形象描述（对齐绝区零中的邦布外观）。
     * 邦布是绝区零中的机械小伙伴：圆润的胶囊/方块状机体、屏幕式大眼睛、头顶天线、短小四肢。
     */
    private static final String BANGBOO_IDENTITY =
            " 邦布的动漫风格插画，《绝区零》角色。" +
            "可爱的机器兔，长耳朵和布偶一样的身体，身体圆滚滚，四肢比较短小。" +
            "细节： 大而明亮的眼睛，眼睛是一对环形灯，没有嘴巴鼻子" +
            "动作： 舒适地坐着，看着观众。艺术风格： 京阿尼风格，色彩鲜艳，线条干净，柔和阴影，杰作，最高画质，平涂加赛璐璐阴影。";

    /** 分类 -> 邦布执行该类任务的画面场景（融入绝区零都市气质）。 */
    private static final Map<String, String> CATEGORY_SCENE = Map.of(
            "ERRAND", "邦布抱着快递包裹在霓虹街区中飞奔送货，动感十足",
            "STUDY", "邦布戴着小眼镜在堆满书本的桌前辅导学习，台灯与全息笔记环绕",
            "DESIGN", "邦布手持画笔与数位板进行创作，周围漂浮调色板与设计草图",
            "COPYWRITING", "邦布敲击发光键盘进行文字创作，头顶漂浮灵感气泡与文字符号",
            "REPAIR", "邦布拿着螺丝刀维修电子设备，火花与电路光效四溅",
            "ACTIVITY", "邦布在热闹的校园活动现场帮忙布置，彩旗气球与霓虹招牌",
            "ONLINE", "邦布面对多块悬浮全息屏幕进行线上协作，赛博网络光线环绕",
            "URGENT", "邦布风风火火冲刺执行加急任务，身后拖着速度线与警示光效",
            "OTHER", "邦布在潮流都市街区热心帮忙，温馨又酷炫"
    );

    /** mascotKey -> 邦布款式差异化描述（在标准邦布形象基础上区分配色/装扮）。 */
    private static final Map<String, String> MASCOT_STYLE = Map.of(
            "kacha", "经典款邦布，蓝白配色机体，屏幕脸显示明亮圆眼，头顶天线，呆萌活泼",
            "phantom", "幻影款邦布，紫黑配色机体，屏幕透出神秘幽光，带霓虹描边与光效，酷炫",
            "amillion", "富豪款邦布，金色与黑色机体，戴迷你礼帽，屏幕眼睛化作金币符号，奢华感",
            "sunny", "阳光款邦布，明黄暖色机体，屏幕显示灿烂笑眼，活力四射",
            "butler", "管家款邦布，深色机体配白色领结装饰，屏幕表情沉稳有礼，绅士优雅"
    );

    /** 任务封面提示词。 */
    public static String coverPrompt(String title, String description, String categoryCn, String category) {
        String scene = CATEGORY_SCENE.getOrDefault(
                category == null ? "OTHER" : category.toUpperCase(), "邦布在潮流都市街区热心帮忙，温馨又酷炫");
        StringBuilder sb = new StringBuilder();
        sb.append("为校园悬赏任务平台「赏金布」生成一张精美的任务封面插画（横版海报构图）。");
        sb.append(BANGBOO_IDENTITY);
        sb.append("画面内容：").append(scene).append("。");
        sb.append("任务主题：").append(nonBlank(title, categoryCn)).append("。");
        if (description != null && !description.isBlank()) {
            sb.append("任务细节参考：").append(trim(description, 60)).append("。");
        }
        sb.append(ZZZ_ART_STYLE);
        sb.append("画质要求：高清细腻，主体居中突出，层次分明的背景，电影级光影，"
                + "色彩鲜明有冲击力，构图有设计感，画面中不要出现任何文字或水印。");
        return sb.toString();
    }

    /** 邦布头像提示词。 */
    public static String avatarPrompt(BangbooAvatarRequest request) {
        String mascot = (request != null && request.mascotKey() != null && !request.mascotKey().isBlank())
                ? request.mascotKey() : "kacha";
        String style = MASCOT_STYLE.getOrDefault(mascot.toLowerCase(),
                "经典款邦布，蓝白配色机体，屏幕脸显示明亮圆眼，头顶天线，呆萌活泼");
        StringBuilder sb = new StringBuilder();
        sb.append("生成一个用于个人头像的「邦布(Bangboo)」角色形象。");
        sb.append(BANGBOO_IDENTITY);
        sb.append("本次款式：").append(style).append("。");
        sb.append("构图：正面或四分之三侧面半身特写，邦布居中，表情生动可爱，占据画面主体；"
                + "背景为纯色或柔和渐变搭配少量绝区零风格的几何/霓虹装饰，突出主体。");
        sb.append(ZZZ_ART_STYLE);
        sb.append("画质要求：高清，3D 卡通渲染质感与精致光影，材质细节丰富，"
                + "萌系可爱，适合作为社交头像，画面中不要出现任何文字或水印。");
        return sb.toString();
    }

    /** 通用反向提示词。 */
    public static String negativePrompt() {
        return "文字, 字幕, 水印, 商标, 签名, 低质量, 模糊, 噪点, 变形, 畸形, "
                + "多余的肢体, 多眼, 多手, 缺肢, 比例失调, 写实照片风格, 真人, 人脸, "
                + "杂乱背景, 阴暗压抑, 恐怖, 惊悚, 血腥, 暴力, 与绝区零风格不符的老旧画风";
    }

    private static String nonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : (b == null ? "校园任务" : b);
    }

    private static String trim(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }
}
