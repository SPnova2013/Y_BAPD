package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SmokeScreen;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.UniversalAccessories;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChooseFLUps;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FancyLight extends MeleeWeapon{
    {
        image = ItemSpriteSheet.UNDONE_MARK;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 0.8f;

        unique = true;
        bones = false;
        tier = 1;
        defaultAction = AC_SET;
    }
    public static final String AC_SET   = "SET";
    public static final String AC_UNSET = "UNSET";
    public static final String AC_SHOOT	= "SHOOT";
    public static final String AC_REFORM= "REFORM";
    public static final String AC_STARSHELL = "STARSHELL";
    protected int accessories = 0;
    private boolean isSet = false;
    private boolean isFirstLight = true;
    private boolean isFly = false;
    private boolean isSplit = false;
    private boolean isStar = false;
    public boolean isStar(){return isStar;}
    private boolean isHE = false;
    private boolean isAP = false;
    private boolean isMine = false;
    public boolean isMine(){return isMine;}
    private int getNapalmTimes = 0;
    private int fireTime = 0;
    private int toxic = 0;
    private int smoke = 0;

    public enum FL{
        STR,        //力量需求
        FIRENOISE,  //开火噪音
        ACC,        //精准--迫击炮不会被闪避，但有较低几率不落在目标地点。精准指的是命中中心的概率
        DMG,        //伤害修正
        CENTDMG,    //中心伤害修正
        EDGEDMG,    //边缘伤害修正
        EXPRNG,     //爆炸范围
        RATE,       //射速(射击耗时)
        MXRNG,      //最远射程
        MNRNG,      //最近射程
        SETTIME,    //架设耗时
        FLYTIME,    //炮弹飞行时间
        SPLIT       //分裂弹头数
    }
    private Map<FL, Float> factors = new HashMap<>();

    public FancyLight(){
        isHE = false;
        factors.put(FL.STR, 0f);//力量修正——初始0
        factors.put(FL.FIRENOISE, 6f);//开火噪音——初始6格
        factors.put(FL.ACC, 0.75f);//精准——初始75%
        factors.put(FL.DMG, 1f);//伤害修正——初始100%
        factors.put(FL.CENTDMG, 1f);//中心伤害修正——初始100%
        factors.put(FL.EDGEDMG, 0.75f);//边缘伤害修正——初始75%
        factors.put(FL.EXPRNG, 1f);//爆炸范围——初始1
        factors.put(FL.RATE, 2f);//射击耗时——初始2
        factors.put(FL.MXRNG, 15f);//最远射程——初始15格
        factors.put(FL.MNRNG, 4f);//最近射程——初始4格
        factors.put(FL.SETTIME, 3f);//架设耗时——初始3回合
        factors.put(FL.FLYTIME, 2f);//炮弹飞行时间——初始2回合
        factors.put(FL.SPLIT, 1f);//弹头数——初始1
    }
    public void add(FL fl, float value) {
        factors.put(fl, factors.get(fl) + value);
    }
    public void multi(FL fl, float value) {
        factors.put(fl, factors.get(fl)*value);
    }

    public float get(FL fl) {
        return factors.get(fl);
    }
    @Override
    public int STRReq(int lvl){
        int req = 10;
        req += accessories;
        req -= hero.pointsInTalent(Talent.MT_MASTER);
        req += get(FL.STR);
        if (masteryPotionBonus) req -= 2;
        return req;
    }
    private static float tier(int acc){
        float tier = Math.max(1, (acc - 8)/2f);
        //each str point after 18 is half as effective
        if (tier > 5){
            tier = 5 + (tier - 5) / 2f;
        }
        return Math.max(1,tier-1);
    }
    @Override
    public int min(int lvl){
        return Math.max( 0, Math.round(
                tier(accessories) +  //base
                        lvl     //level scaling
        ));
    }
    @Override
    public int max(int lvl){
        return Math.max( 0, Math.round(
                5*(tier(accessories)+1) +    //base
                        lvl*(tier(accessories)+1)    //level scaling
        ));
    }
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (isEquipped( hero )) {
            actions.add(AC_REFORM);
            if(isSet) {
                actions.add(AC_UNSET);
                actions.add(AC_SHOOT);
                if(isStar) actions.add(AC_STARSHELL);
            }
            else actions.add(AC_SET);
        }
        return actions;
    }
    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        curItem = this;
        FancyLight fl = hero.belongings.getItem(FancyLight.class);
        if(fl == null) return;
        if (action.equals(AC_SET)) {
            hero.spendAndNext(Math.max(0,fl.get(FL.SETTIME)));
            isSet = true;
            //TODO 恢复这里
            //hero.rooted=true;
            defaultAction = AC_SHOOT;
        }
        if (action.equals(AC_UNSET)) {
            hero.spendAndNext(2f);
            isSet = false;
            if(hero.buff(Roots.class)==null) hero.rooted=false;
            defaultAction = AC_SET;
        }
        if (action.equals(AC_SHOOT)) {
            GameScene.selectCell(shoot);
        }
        if (action.equals(AC_REFORM)) {
            GameScene.selectItem(gunSelector);
        }
    }
    //选择射击地点
    protected static final CellSelector.Listener shoot = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target != null) {
                if(!Dungeon.level.passable[target]){
                    GLog.i(Messages.get(FancyLight.class, "invalid_target"));
                    return;
                }
                if(!(curItem instanceof FancyLight)){
                    GLog.i(Messages.get(FancyLight.class, "no_fancy_light"));
                    return;
                }
                FancyLight fl = (FancyLight) curItem;
                if(Dungeon.level.distance(target, hero.pos) < fl.get(FL.MNRNG)) {
                    GLog.i(Messages.get(FancyLight.class, "over_min_range"));
                    return;
                }
                if(Dungeon.level.distance(target, hero.pos) > fl.get(FL.MXRNG)) {
                    GLog.i(Messages.get(FancyLight.class, "over_max_range"));
                    return;
                }
                Sample.INSTANCE.play( Assets.Sounds.BLAST, 1, Random.Float(0.87f, 1.15f) );
                MagicMissile.boltFromChar(curUser.sprite.parent,
                        MagicMissile.MAGIC_MISSILE,
                        curUser.sprite,
                        curUser.pos % Dungeon.level.width(),
                        new Callback() {
                            @Override
                            public void call() {
                                callShell(fl,target);
                            }
                        }
                ).setSpeed(1000);
                for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) { //吸引开火位置附近敌人
                    if (mob.paralysed <= 0
                            && Dungeon.level.distance(curUser.pos, mob.pos) <= fl.get(FL.FIRENOISE)
                            && mob.state != mob.HUNTING) {
                        mob.beckon( curUser.pos );
                    }
                }
            }
        }
        private void callShell(FancyLight fl, int target){
            hero.spend(Math.max(0,fl.get(FL.RATE)));
            for(int i=0;i<fl.get(FL.SPLIT);i++) {
                Buff.append(Dungeon.hero, ShellIncoming.class).setup(
                        target,                             //目标地点
                        Dungeon.depth,                      //楼层，不在同一层则不引爆
                        fl                                  //武器本体
                );
            }
            hero.next();
        }
        @Override
        public String prompt() {
            return Messages.get(FancyLight.class, "select_shoot_target");
        }
    };
    //处理炮弹飞行及落地爆炸过程用的buff
    public static class ShellIncoming extends Buff {
        private int selectedpos;//用来显示炮弹选择时的点，因为实际落点可能和选择的位置不一样
        private int targetpos;
        private int explodeDepth;
        private int left;
        private int weaponlevel;
        private int explodeRange;
        private int fireTime;
        private int gasAmount;
        private int smokeAmount;
        private float dmgFactor;
        private float centerDmgFactor;
        private float edgeDmgFactor;
        private boolean isHE;
        private boolean isAP;
        private boolean isMine;

        public void setup(int pos, int explodeDepth, FancyLight fl) {
            selectedpos = pos;
            this.targetpos = pos;
            this.explodeDepth = explodeDepth;
            this.left = Math.max(Math.round(fl.get(FL.FLYTIME)),0);
            weaponlevel = fl.buffedLvl();
            explodeRange = Math.round(fl.get(FL.EXPRNG));
            dmgFactor = fl.get(FL.DMG);
            centerDmgFactor = fl.get(FL.CENTDMG);
            edgeDmgFactor = fl.get(FL.EDGEDMG);
            fireTime = fl.fireTime;
            gasAmount = fl.toxic;
            smokeAmount = fl.smoke;

            isHE = fl.isHE;
            isAP = fl.isAP;
            isMine = fl.isMine;

            if (Random.Float(1) >= fl.get(FL.ACC))
                targetpos = targetpos + PathFinder.NEIGHBOURS8[Random.Int(8)];
        }

        @Override
        public boolean act() {
            if (explodeDepth == Dungeon.depth) {
                left--;
                if (left <= 0) {
                    MagicMissile missile = ((MagicMissile) curUser.sprite.parent.recycle(MagicMissile.class));
                    missile.reset(MagicMissile.MAGIC_MISSILE, targetpos % Dungeon.level.width(), targetpos, new Callback() {
                        @Override
                        public void call() {
                            if(isMine) Dungeon.level.mine();
                            else doExplode();
                        }
                    },
                            1000);
                } else {
                    curUser.sprite.parent.addToBack(new TargetedCell(selectedpos, 0xFF0000));
                }
            }
            spend(TICK);
            return true;
        }
        private void doExplode() {
            boolean[] FOV = new boolean[Dungeon.level.length()];
            Point c = Dungeon.level.cellToPoint(targetpos);
            ShadowCaster.castShadow(c.x, c.y, FOV, Dungeon.level.losBlocking, explodeRange);

            ArrayList<Char> affected = new ArrayList<>();
            for (int i = 0; i < FOV.length; i++) {
                if (FOV[i]) {
                    if (fireTime > 0) GameScene.add(Blob.seed(i, fireTime, Fire.class));
                    if (Dungeon.level.heroFOV[i] && !Dungeon.level.solid[i]) {
                        CellEmitter.center(i).burst(BlastParticle.FACTORY, 5);
                    }
                    Char ch = Actor.findChar(i);
                    if (ch != null) {
                        affected.add(ch);
                    }
                }
            }
            if (gasAmount > 0) GameScene.add(Blob.seed(targetpos, gasAmount, ToxicGas.class));
            if (smokeAmount > 0)
                GameScene.add(Blob.seed(targetpos, smokeAmount, SmokeScreen.class));
            for (Char ch : affected) {
                int damage = Math.round(Random.NormalIntRange(1 + weaponlevel * 2, 10 + weaponlevel * 12));
                damage += Random.NormalIntRange(0, 3 * (1 + explodeDepth / 5));//所谓的对低甲增伤
                if (ch.pos != targetpos) {
                    damage = Math.round(damage * edgeDmgFactor);
                } else {
                    damage = Math.round(damage * centerDmgFactor);
                }
                damage = Math.round(damage * dmgFactor);
                if (!(ch.pos == targetpos && isAP)) damage -= ch.drRoll();
                if (isHE) damage -= ch.drRoll();
                ch.damage(damage, this);

                if (ch == Dungeon.hero && !ch.isAlive()) {
                    Dungeon.fail(Bomb.class);
                }
            }

            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) { //吸引爆炸位置的敌人
                if (mob.paralysed <= 0
                        && Dungeon.level.distance(targetpos, mob.pos) <= explodeRange * 2 + 2
                        && mob.state != mob.HUNTING) {
                    mob.notice();
                    mob.state = mob.WANDERING;
                }
            }

            next();
            detach();
        }
    }
    //地雷
    public class FLMine implements Bundlable{

        public int pos;
        private static final String POS	= "pos";
        public void trigger(){
            Char ch = Actor.findChar(pos);
            if (ch instanceof Hero){
                return;
            }
            //doExplode();
        }
        @Override
        public void restoreFromBundle(Bundle bundle) {
            pos = bundle.getInt( POS );
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            bundle.put( POS, pos );
        }
    }
    private class Shell{

        private int selectedpos;//用来显示炮弹选择时的点，因为实际落点可能和选择的位置不一样
        private int targetpos;
        private int explodeDepth;
        private int left;
        private int weaponlevel;
        private int explodeRange;
        private int fireTime;
        private int gasAmount;
        private int smokeAmount;
        private float dmgFactor;
        private float centerDmgFactor;
        private float edgeDmgFactor;
        private boolean isHE;
        private boolean isAP;
        private boolean isMine;
        protected void set(int pos, int explodeDepth, FancyLight fl){}
    }
    //选择拆解强化用的物品
    protected static WndBag.ItemSelector gunSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return  Messages.get(FancyLight.class, "select_weapon");
        }

        @Override
        public Class<?extends Bag> preferredBag(){
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return (item instanceof Gun || item instanceof UniversalAccessories);
        }

        @Override
        public void onSelect( Item item ) {
            if(item != null) {
                GameScene.show( new WndChooseFLUps((FancyLight)curItem, item));
            }
        }
    };
    //强化项目
    public static abstract class FLUpgrades implements Bundlable {
        public static final String[] Kinds = new String[]{"HG","SR","GL","MG","SMG","SG","AR","UN"};
        public static final String[] HG = new String[]{
                "LightWeight","StarShell","SemiClosed"};
        public static final String[] SR = new String[]{
                "FireControl","ArmorPiercing"};
        public static final String[] GL = new String[]{
                "HighExplosive","LayMine"};
        public static final String[] MG = new String[]{
                "RapidFire","HeavyWarhead"};
        public static final String[] SMG = new String[]{
                "Portable","GlideRangeExtend"};
        public static final String[] SG = new String[]{
                "ClusterBomb","Fragmentation"};
        public static final String[] AR = new String[]{
                "PowderCharge","Hypervelocity"};
        public static final String[] UN = new String[]{
                "Napalm","Toxic","Smoke"};
        public static String getRandom(){
            String weapon = Kinds[Random.Int(Kinds.length)];
            return getRandomByWeapon(weapon);
        }
        public static String getRandomByWeapon(String weapon){
            switch (weapon){
                case "HG": return HG[Random.Int(HG.length)];
                case "SR": return SR[Random.Int(SR.length)];
                case "GL": return GL[Random.Int(GL.length)];
                case "MG": return MG[Random.Int(MG.length)];
                case "SMG": return SMG[Random.Int(SMG.length)];
                case "SG": return SG[Random.Int(SG.length)];
                case "AR": return AR[Random.Int(AR.length)];
                case "UN": return UN[Random.Int(UN.length)];
                default: return getRandom();
            }
        }
        public static void changeFLStatus(FancyLight fl, String selected){
            switch (selected){
                case "LightWeight":
                    if(fl.isFirstLight) fl.add(FL.STR,-1);
                    fl.isFirstLight = false;
                    fl.add(FL.STR,-1);
                    break;
                case "StarShell":
                    fl.isStar = true;
                    break;
                case "SemiClosed":
                    fl.add(FL.FIRENOISE,-1);
                    break;
                case "FireControl":
                    fl.add(FL.ACC,0.05f);
                    break;
                case "ArmorPiercing":
                    fl.add(FL.CENTDMG,0.3f);
                    fl.add(FL.EDGEDMG,-0.1f);
                    fl.isAP = true;
                    break;
                case "HighExplosive":
                    fl.add(FL.DMG,0.05f);
                    fl.add(FL.EXPRNG,1);
                    fl.isHE = true;
                    break;
                case "LayMine":
                    fl.isMine = true;
                    break;
                case "RapidFire":
                    fl.multi(FL.RATE,0.8f);
                    break;
                case "HeavyWarhead":
                    fl.add(FL.MNRNG,-1);
                    fl.add(FL.MXRNG,-1);
                    break;
                case "Portable" :
                    fl.add(FL.SETTIME,-1);
                    break;
                case "GlideRangeExtend":
                    fl.add(FL.MXRNG,7);
                    if(!fl.isFly) fl.add(FL.FLYTIME,1);
                    fl.isFly = true;
                    break;
                case "ClusterBomb":
                    if(!fl.isSplit) {
                        fl.add(FL.SPLIT, 1);
                        fl.multi(FL.DMG,0.40f);
                        fl.multi(FL.ACC,0.5f);
                    }
                    fl.add(FL.SPLIT,1);
                    if(fl.isSplit)fl.add(FL.DMG,-0.02f);
                    fl.isSplit = true;
                    break;
                case "Fragmentation":
                    fl.add(FL.EDGEDMG,0.13f);
                    break;
                case "PowderCharge":
                    fl.add(FL.MXRNG,5);
                    fl.add(FL.MNRNG,1);
                    break;
                case "Hypervelocity":
                    fl.add(FL.FLYTIME,-1);
                    break;
                case "Napalm":
                    fl.getNapalmTimes++;
                    fl.fireTime = fl.getNapalmTimes*(fl.getNapalmTimes+1)/2;
                    break;
                case "Toxic":
                    fl.toxic+=250;
                    break;
                case "Smoke":
                    fl.smoke+=500;
                    break;
                default:
                    GLog.h(selected);
            }
            updateQuickslot();
        }
    }
}
