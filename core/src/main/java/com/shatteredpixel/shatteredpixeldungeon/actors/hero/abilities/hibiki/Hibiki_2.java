package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.hibiki;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Hibiki_2 extends ArmorAbility {

    {
        baseChargeUse = 35f;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        armor.charge -= chargeUse(hero);
        armor.updateQuickslot();
        Invisibility.dispel();
        hero.spendAndNext(Actor.TICK);

    }

    @Override
    public int icon() {
        return ItemSpriteSheet.UNDONE_MARK;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.HIBIKI_ABIL_11, Talent.HIBIKI_ABIL_12, Talent.HIBIKI_ABIL_13, Talent.HEROIC_ENERGY};
    }
}
