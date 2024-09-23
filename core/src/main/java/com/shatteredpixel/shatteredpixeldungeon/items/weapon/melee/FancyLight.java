package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.PROFICIENT_RELOAD;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.RAPID_SHOOTING;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SHELL_SHOCK;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.THERMO_YEILD;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SmokeScreen;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
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
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    public boolean isSet(){return isSet;}
    private boolean isFirstLight = true;
    private boolean isFly = false;
    private boolean isSplit = false;
    private boolean isStar = false;
    public boolean isStar(){return isStar;}
    private boolean isHE = false;
    private boolean isAP = false;
    private boolean isMine = false;
    public boolean isMine(){return isMine;}
    private int napalmTimes = 0;
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
        req += Math.round(get(FL.STR));
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
    public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
        if (super.doUnequip(hero, collect, single)){
            if (hero.belongings.secondWep!=null){
                hero.belongings.secondWep.doUnequip(hero,true);
            }
            return true;
        } else {
            return false;
        }
    }
    //按键绑定
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
    //按键操作
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
            float rate = fl.get(FL.RATE);
            if(hero.hasTalent(PROFICIENT_RELOAD)) rate*=(1f-0.1f*hero.pointsInTalent(PROFICIENT_RELOAD));
            hero.spend(Math.max(0,rate));
            for(int i=0;i<fl.get(FL.SPLIT);i++) {
                Buff.append(Dungeon.hero, ShellIncoming.class).set(
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
        private Shell shell = Reflection.newInstance(Shell.class);
        FancyLight fl;
        public void set(int pos, int explodeDepth, FancyLight fl){
            this.fl = fl;
            shell.set(pos,explodeDepth,fl);
        }
        @Override
        public boolean act() {
            if (shell.explodeDepth == Dungeon.depth) {
                shell.left--;
                if (shell.left <= 0) {
                    MagicMissile missile = ((MagicMissile) curUser.sprite.parent.recycle(MagicMissile.class));
                    missile.reset(MagicMissile.MAGIC_MISSILE, shell.targetpos % Dungeon.level.width(), shell.targetpos, new Callback() {
                        @Override
                        public void call() {
                            if(shell.isMine) {
                                FLMine mine = Objects.requireNonNull(Reflection.newInstance(FLMine.class)).set(shell.targetpos, Dungeon.depth, fl);
                                Dungeon.level.drop(mine, shell.targetpos);
                                Dungeon.level.mine(mine, shell.targetpos);
                            }else {
                                shell.doExplode();
                            }
                            next();
                            detach();
                        }
                    },
                            1000);
                } else {
                    curUser.sprite.parent.addToBack(new TargetedCell(shell.selectedpos, 0xFF0000));
                }
            }
            spend(TICK);
            return true;
        }
    }
    //地雷
    public static class FLMine extends Item implements Bundlable{
        {
            image = ItemSpriteSheet.AMMO_BOX;
        }
        private Shell shell = Reflection.newInstance(Shell.class);
        public int pos;
        private static final String POS	= "pos";
        public FLMine set(int pos, int explodeDepth, FancyLight fl){
            shell.set(pos,explodeDepth,fl);
            return this;
        }
        public void trigger(){
            shell.doExplode();
        }
        @Override
        public boolean doPickUp(Hero hero, int pos) {
            GLog.i(Messages.get(FancyLight.FLMine.class, "defuse"));
            hero.spendAndNext( TIME_TO_PICK_UP );
            return true;
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
    //爆炸
    public static class Shell{
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
        protected void set(int pos, int explodeDepth, FancyLight fl){
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
                targetpos = targetpos + PathFinder.NEIGHBOURS8[Random.Int(8)];}
        protected void doExplode() {
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
                if(hero.hasTalent(THERMO_YEILD)) damage+= (1+2*hero.pointsInTalent(THERMO_YEILD));
                if(hero.hasTalent(SHELL_SHOCK) && ch instanceof Mob){
                    if(ch.pos == targetpos && Random.Int(20)<hero.pointsInTalent(SHELL_SHOCK)+2) affectShellShockDebuff((Mob)ch);
                    else if (ch.pos != targetpos && Random.Int(10)<hero.pointsInTalent(SHELL_SHOCK)+1) affectShellShockDebuff((Mob)ch);
                }

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
                    if(hero.hasTalent(SHELL_SHOCK)){
                        if(Dungeon.level.distance(targetpos, mob.pos) > explodeRange && Random.Int(20)<hero.pointsInTalent(SHELL_SHOCK)){
                            affectShellShockDebuff(mob);
                        }
                    }
                }
            }
        }
    }
    //弹震症Debuff
    private static void affectShellShockDebuff(Mob mob){
        switch (Random.Int(4)){
        case 0:
            Buff.affect(mob, Blindness.class, Blindness.DURATION);
            break;
        case 1:
            Buff.affect(mob, Slow.class, Slow.DURATION);
            break;
        case 2:
            Buff.affect(mob, Vertigo.class, Vertigo.DURATION);
            break;
        case 3: default:
            Buff.affect(mob, Terror.class, Terror.DURATION);
            break;
    }}
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
        public enum FLUpCategory {
            HG(0),SR(0),GL(0),MG(0),SMG(0),SG(0),AR(0),UN(0);
            public String[] strs;
            public String str;
            public float prob;
            public float[] probs;
            public float[] defaultProbs = null;
            private FLUpCategory(float prob) {
                this.prob = prob;
            }
            static {
                HG.strs = new String[]{"LightWeight","StarShell","SemiClosed"};
                HG.defaultProbs= new float[]{1,1,1};
                HG.probs = HG.defaultProbs.clone();
                SR.strs = new String[]{"FireControl","ArmorPiercing"};
                SR.defaultProbs= new float[]{1,1};
                SR.probs = SR.defaultProbs.clone();
                GL.strs = new String[]{"HighExplosive","LayMine"};
                GL.defaultProbs= new float[]{1,1};
                GL.probs = GL.defaultProbs.clone();
                MG.strs = new String[]{"RapidFire","HeavyWarhead"};
                MG.defaultProbs= new float[]{1,1};
                MG.probs = MG.defaultProbs.clone();
                SMG.strs = new String[]{"Portable","GlideRangeExtend"};
                SMG.defaultProbs= new float[]{1,1};
                SMG.probs = SMG.defaultProbs.clone();
                SG.strs = new String[]{"ClusterBomb","Fragmentation"};
                SG.defaultProbs= new float[]{1,1};
                SG.probs = SG.defaultProbs.clone();
                AR.strs = new String[]{"PowderCharge","Hypervelocity"};
                AR.defaultProbs= new float[]{1,1};
                AR.probs = AR.defaultProbs.clone();
                UN.strs = new String[]{"Napalm","Toxic","Smoke"};
                UN.defaultProbs= new float[]{1,1,1};
                UN.probs = UN.defaultProbs.clone();
            }
        }
        public static void reset(FLUpCategory cat) {
            if (cat.defaultProbs != null) cat.probs = cat.defaultProbs.clone();
        }
        public static <T extends Enum<?>> T getRandomEnum(Class<T> enumClass) {
            T[] enumConstants = enumClass.getEnumConstants();
            return enumConstants[Random.Int(enumConstants.length)];
        }
        public static String getRandom(){
            return getRandomByCategory(getRandomEnum(FLUpCategory.class));
        }
        public static String getRandomByItem(Item item){
            FLUpCategory cat;
            switch (item.getClass().getSuperclass().getSimpleName()){
                case "HG":  cat = FLUpCategory.HG;  break;
                case "SR":  cat = FLUpCategory.SR;  break;
                case "GL":  cat = FLUpCategory.GL;  break;
                case "MG":  cat = FLUpCategory.MG;  break;
                case "SMG": cat = FLUpCategory.SMG; break;
                case "SG":  cat = FLUpCategory.SG;  break;
                case "AR":  cat = FLUpCategory.AR;  break;
                default:    cat = FLUpCategory.UN;  break;
            }
            return getRandomByCategory(cat);
        }
        public static String getRandomByCategory(FLUpCategory cat){
            if(cat.probs != null){
                int i = Random.chances(cat.probs);
                if (i == -1) {
                    getRandom();
                }
                String result = cat.strs[i];
                checkFLUpResult(result);
                return result;
            }else{
                return getRandom();
            }
        }
        private static void checkFLUpResult(String result){
            FancyLight fl = hero.belongings.getItem(FancyLight.class);
            if(Objects.equals(result, "StarShell"))                                     FLUpCategory.HG.probs[1] = 0;
            if(Objects.equals(result, "SemiClosed") && fl.get(FL.FIRENOISE) == 1)       FLUpCategory.HG.probs[2] = 0;
            if(Objects.equals(result, "FireControl") && fl.get(FancyLight.FL.ACC)>=100) FLUpCategory.SR.probs[0] = 0;
            if(Objects.equals(result, "HighExplosive"))                                 FLUpCategory.GL.probs[0]/= 2;
            if(Objects.equals(result, "LayMine") && fl.isMine())                        FLUpCategory.GL.probs[1] = 0;
            if(Objects.equals(result,"Portable") && fl.get(FancyLight.FL.SETTIME)<=0)   FLUpCategory.SMG.probs[0] = 0;
            if(Objects.equals(result,"Hypervelocity") && fl.get(FancyLight.FL.FLYTIME)<=0)FLUpCategory.AR.probs[0] = 0;
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
                    fl.napalmTimes++;
                    fl.fireTime = fl.napalmTimes *(fl.napalmTimes +1)/2;
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
    private static final String FACTORS = "Factors";
    private static final String ISSET = "isset";
    private static final String ISFIRSTLIGHT = "isfirstlight";
    private static final String ISFLY = "isfly";
    private static final String ISSPLIT = "issplit";
    private static final String ISSTAR = "isstar";
    private static final String ISHE = "ishe";
    private static final String ISMINE = "ismine";
    private static final String NAPALMTIMES = "napalmtimes";
    private static final String FIRETIME = "firetime";
    private static final String TOXIC = "toxic";
    private static final String SMOKE = "smoke";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        if (factors != null) {
            bundle.put(FACTORS + "_size", factors.size()); // 存储 HashMap 的大小
            int index = 0;
            for (Map.Entry<FL, Float> entry : factors.entrySet()) {
                bundle.put(FACTORS + "_key_" + index, entry.getKey().name()); // 使用枚举的 name() 方法存储键
                bundle.put(FACTORS + "_value_" + index, entry.getValue()); // 存储值
                index++;
            }
        }
        bundle.put(ISSET, isSet);
        bundle.put(ISFIRSTLIGHT, isFirstLight);
        bundle.put(ISFLY, isFly);
        bundle.put(ISSPLIT, isSplit);
        bundle.put(ISSTAR, isStar);
        bundle.put(ISHE, isHE);
        bundle.put(ISMINE, isMine);
        bundle.put(NAPALMTIMES, napalmTimes);
        bundle.put(FIRETIME, fireTime);
        bundle.put(TOXIC, toxic);
        bundle.put(SMOKE, smoke);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);

        factors.clear(); // 清空原来的 HashMap
        int size = bundle.getInt(FACTORS + "_size"); // 获取 HashMap 的大小
        for (int i = 0; i < size; i++) {
            String keyName = bundle.getString(FACTORS + "_key_" + i); // 取出键名
            FL key = FL.valueOf(keyName); // 将字符串转换为对应的枚举类型
            Float value = bundle.getFloat(FACTORS + "_value_" + i); // 取出值
            factors.put(key, value); // 将键值对放回 HashMap
        }
        isSet = bundle.getBoolean(ISSET);
        isFirstLight = bundle.getBoolean(ISFIRSTLIGHT);
        isFly = bundle.getBoolean(ISFLY);
        isSplit = bundle.getBoolean(ISSPLIT);
        isStar = bundle.getBoolean(ISSTAR);
        isHE = bundle.getBoolean(ISHE);
        isMine = bundle.getBoolean(ISMINE);
        napalmTimes = bundle.getInt(NAPALMTIMES);
        fireTime = bundle.getInt(FIRETIME);
        toxic = bundle.getInt(TOXIC);
        smoke = bundle.getInt(SMOKE);
        if(isSet) defaultAction = AC_SHOOT;
    }
}
