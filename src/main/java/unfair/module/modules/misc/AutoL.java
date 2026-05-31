package unfair.module.modules.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import unfair.event.EventTarget;
import unfair.events.AttackEvent;
import unfair.events.TickEvent;
import unfair.event.types.EventType;
import unfair.module.Module;
import unfair.property.properties.BooleanProperty;
import unfair.property.properties.ModeProperty;
import unfair.property.properties.TextProperty;
import unfair.util.ChatUtil;
import unfair.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public class AutoL extends Module {
    private final ModeProperty wordPattern = new ModeProperty("WordPattern", 0,
            new String[]{"Poem", "Mama", "PridePlus", "Cixiaogui", "CrystalPVP", "Clear", "English", "SanGuo", "Custom"});
    private final BooleanProperty nameInFront = new BooleanProperty("NameInFront", true);
    private final TextProperty content = new TextProperty("Content", "");

    private final List<Entity> enemies = new ArrayList<>();

    public AutoL() {
        super("AutoL", false);
    }

    @Override
    public String[] getSuffix() {
        return new String[]{wordPattern.getModeString()};
    }

    @Override
    public void onEnabled() {
        enemies.clear();
    }

    @Override
    public void onDisabled() {
        enemies.clear();
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (!isEnabled()) return;
        Entity target = event.getTarget();
        if (target instanceof EntityPlayer && !enemies.contains(target)) {
            enemies.add(target);
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!isEnabled()) return;
        if (event.getType() != EventType.POST) return;

        List<Entity> toRemove = new ArrayList<>();
        for (Entity entity : enemies) {
            if (!entity.isEntityAlive()) {
                sayMessage(entity);
                toRemove.add(entity);
            }
        }
        enemies.removeAll(toRemove);
    }

    private void sayMessage(Entity entity) {
        String playerName = entity instanceof EntityPlayer ? entity.getName() : entity.getName();
        String rawMessage = getRandomMessage();
        String message = nameInFront.getValue() ? playerName + " " + rawMessage : rawMessage;
        ChatUtil.sendMessage(message);
    }

    private String getRandomMessage() {
        String mode = wordPattern.getModeString();
        switch (mode) {
            case "Custom":
                return content.getValue().isEmpty() ? "Custom message not set!" : content.getValue();
            case "SanGuo":
                return SAN_GUO[RandomUtil.nextInt(0, SAN_GUO.length)];
            case "Clear":
                return CLEAR[RandomUtil.nextInt(0, CLEAR.length)];
            case "English":
                return ENGLISH[RandomUtil.nextInt(0, ENGLISH.length)];
            case "Mama":
                return MAMA[RandomUtil.nextInt(0, MAMA.length)];
            case "Poem":
                return POEMS[RandomUtil.nextInt(0, POEMS.length)];
            case "PridePlus":
                return PRIDEPLUS[RandomUtil.nextInt(0, PRIDEPLUS.length)];
            case "Cixiaogui":
                return CIXIAOGUI[RandomUtil.nextInt(0, CIXIAOGUI.length)];
            case "CrystalPVP":
                return CRYSTAL_PVP[RandomUtil.nextInt(0, CRYSTAL_PVP.length)];
            default:
                return POEMS[RandomUtil.nextInt(0, POEMS.length)];
        }
    }

    private static final String[] POEMS = {
            "立志用功如种树然，方其根芽，犹未有干；及其有干，尚未有枝；枝而后叶，叶而后花。",
            "骐骥一跃，不能十步；驽马十驾，功在不舍；锲而舍之，朽木不折；锲而不舍，金石可镂。",
            "天见其明，地见其光，君子贵其全也。",
            "只有功夫深，铁杵磨成针。",
            "一言既出，驷马难追。",
            "为一身谋则愚，而为天下则智。",
            "处其厚，不居其薄，处其实，不居其华。",
            "白沙在涅，与之俱黑。",
            "如果永远是晴天，土地也会布满裂痕。",
            "只有知识之海，才能载起成才之舟。",
            "谬论从门缝钻进，真理立于门前。",
            "自其变者而观之，则天地曾不能以一瞬；自其不变者而观之，则物与我皆无尽也。"
    };

    private static final String[] SAN_GUO = {
            "一破：卧龙出山，你已被SouthSide客户端击毙",
            "双连：一战成名，你已被SilenceFix客户端击毙",
            "三连：举世皆惊，你已被Myau客户端击毙",
            "四连：天下无敌，你已被Leader客户端击毙",
            "五连：诛天灭地，你已被Augustus客户端击毙"
    };

    private static final String[] MAMA = {"妈妈"};
    private static final String[] CLEAR = {"L"};

    private static final String[] PRIDEPLUS = {
            "嗨，我是风动，这是我的neibu神器，3000收neibu是我的秘密武器，花钱一分钟，赚钱两个月，不要告诉别人哦",
            "嗨，我是Pro，这是我的neibu神器，200整30个conf是我的秘密武器，花钱一分钟，赚钱两年半，不要告诉别人哦",
            "嗨，我是回想，这是我的fix神器，fix各种端是我的秘密武器，fix一小时，高兴一个月，不要告诉别人哦",
            "嗨，我是小职，这是我的cookies神器，3000+cookies是我的秘密武器，获取一秒钟，游戏一小时，不要告诉小手哦",
            "嗨，我是原批，这是我的启动神器，你说的对，但是原神启动是我的秘密武器，启动十分钟，充电五小时，不要告诉别人哦",
            "嗨，我是风动，这是我的抽烟神器，3000买下锐刻114514代是我的秘密武器，花钱一秒钟，抽烟一辈子，不要告诉丁真哦",
            "嗨，我是小手冰凉,这是我的Cherish,Cherish是我的秘密武器，出击一分钟，殴打两小时，不要告诉小手哦",
            "嗨，我是瓦瓦，这是我的pride+神器，是我的秘密武器，vel一分钟，死号两小时，不要告诉瓦瓦哦"
    };

    private static final String[] CIXIAOGUI = {
            "呐呐~杂鱼哥哥不会这样就被捉弄的不会说话了吧♡",
            "嘻嘻~杂鱼哥哥不会以为竖个大拇哥就能欺负我了吧~不会吧♡不会吧♡",
            "杂鱼哥哥怎么可能欺负得了别人呢~只能欺负自己哦♡~",
            "哥哥真是好欺负啊♡嘻嘻~",
            "哎♡~杂鱼说话就是无趣唉~",
            "呐呐~杂鱼哥哥发这个是想教育我吗~嘻嘻~怎么可能啊♡",
            "什么嘛~废柴哥哥会想这种事情啊~唔呃",
            "把你肮脏的目光拿开啦~很恶心哦♡",
            "咱的期待就是被你这样的笨蛋破坏了~♡",
            "诶~这么快就认输了？咱还没开始认真呢♡",
            "哥哥的操作破绽比芝士奶酪的洞还多哦~嘻嘻♡",
            "不会吧不会吧~这就是传说中的‘高手’吗？真是有够好笑的♡",
            "建议哥哥把游戏ID改成‘易推倒’呢~简直太合适了♡",
            "啊啦~这么简单的连招都接不住，哥哥是闭着眼睛在玩吗？",
            "需要咱放点水吗？毕竟欺负残疾人是不好的呢~唔噗♡",
            "哥哥的失败数据咱会好好收藏的~这可是珍贵的杂鱼样本呢♡",
            "知道为什么输得这么惨吗？因为从开始到现在的每一步都在咱计算中哦~",
            "快去论坛发帖‘被美少女暴打怎么办’吧~咱会去给你点赞的♡"
    };

    private static final String[] CRYSTAL_PVP = {
            "鼠标明天到，触摸板打的", "转人工", "收徒", "不收徒", "有真人吗", "墨镜上车", "素材局", "不接单", "接单",
            "征婚", "4399?", "暂时不考虑打职业", "bot?", "叫你家大人来打", "假肢上门安装", "浪费我的网费",
            "不收残疾人", "下课", "自己找差距", "不接代", "代+", "这样的治好了也流口水", "人机", "人机怎么调难度啊",
            "只收不被0封的", "Bot吗这是", "领养", "纳亲", "正视差距", "近亲繁殖?", "我玩的是新手教程?",
            "来调灵敏度的", "来调参数的", "小号", "不是本人别加", "下次记得晚点玩", "随便玩玩,不带妹", "扣1上车"
    };

    private static final String[] ENGLISH = {
            "Good fight! Well played.",
            "Nice one — keep it up!",
            "GG, that was fun.",
            "Close one! Wanna rematch?",
            "Not bad, you surprised me.",
            "Sweet move! Respect.",
            "That was intense, great game!",
            "Careful next time — watch your back!",
            "Wow, that was clean. Props.",
            "You got lucky — nice try!",
            "Play again sometime, champ!",
            "Who taught you that combo? Impressive.",
            "Nice aim — for a stormtrooper.",
            "Blink twice if you need a tutorial.",
            "I’d explain what you did wrong, but it’s more fun watching.",
            "Do you want a medal or just the respawn?",
            "Save that strategy for casual mode, okay?",
            "I thought I queued into hard mode. Turns out it was you.",
            "You’re the plot twist nobody asked for.",
            "Legend says your K/D is a myth.",
            "I’d call you lucky, but that would be generous.",
            "Is your controller plugged in? Asking for science.",
            "Cute try — did it come with instructions?",
            "Next time bring snacks; this is getting embarrassing."
    };
}