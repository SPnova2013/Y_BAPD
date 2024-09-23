/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.FancyLight;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Random;

import java.util.Objects;

public class WndChooseFLUps extends Window {

    private static final int WIDTH		= 130;
    private static final float GAP		= 2;

    public WndChooseFLUps(final FancyLight fl, final Item item){

        super();

        IconTitle titlebar = new IconTitle();
        titlebar.icon( new ItemSprite( fl.image(), null ) );
        titlebar.label( Messages.titleCase(fl.name()) );
        titlebar.setRect( 0, 0, WIDTH, 0 );
        add( titlebar );

        RenderedTextBlock body = PixelScene.renderTextBlock( 6 );
        body.text(Messages.get(this, "message"), WIDTH);
        body.setPos( titlebar.left(), titlebar.bottom() + GAP );
        add( body );
        //TODO
        //if(item.flUpSeen) 读取已记录的升级
        //else
        int safeCount = 0;
        float pos = body.bottom() + 3*GAP;
        final String[] flUpgrades = new String[3];
        flUpgrades[0]= FancyLight.FLUpgrades.getRandomByItem(item);
        do{
            safeCount++;
            flUpgrades[1]= Random.Int(2)==0 ? FancyLight.FLUpgrades.getRandomByItem(item) : FancyLight.FLUpgrades.getRandom();
        }while(invalidFLUps(flUpgrades, flUpgrades[1]) && (safeCount<10));
        safeCount = 0;
        do{
            safeCount++;
            flUpgrades[2]= FancyLight.FLUpgrades.getRandom();
        }while(invalidFLUps(flUpgrades, flUpgrades[2]) && (safeCount < 10));
        for (String ups : flUpgrades) {
            RedButton abilityButton = new RedButton(Messages.get(fl, ups+".short_desc"), 6){
                @Override
                protected void onClick() {
                    GameScene.show(new WndOptions( new ItemSprite( fl.image(), null ),
                            Messages.titleCase(Messages.get(fl, ups+".name")),
                            Messages.get(WndChooseFLUps.this, "are_you_sure"),
                            Messages.get(WndChooseFLUps.this, "yes"),
                            Messages.get(WndChooseFLUps.this, "no")){

                        @Override
                        protected void onSelect(int index) {
                            hide();
                            if (index == 0 && WndChooseFLUps.this.parent != null){
                                WndChooseFLUps.this.hide();
                                FancyLight.FLUpgrades.changeFLStatus(fl, ups);
                            }
                        }
                    });
                }
            };
            abilityButton.leftJustify = true;
            abilityButton.multiline = true;
            abilityButton.setSize(WIDTH-20, abilityButton.reqHeight()+2);
            abilityButton.setRect(0, pos, WIDTH-20, abilityButton.reqHeight()+2);
            add(abilityButton);

            IconButton abilityInfo = new IconButton(Icons.get(Icons.INFO)){
                @Override
                protected void onClick() {
                    GameScene.show(new WndTitledMessage(new ItemSprite( item.image(), null ),Messages.get(fl, ups+".name"),Messages.get(fl, ups+".desc")));
                }
            };
            abilityInfo.setRect(WIDTH-20, abilityButton.top() + (abilityButton.height()-20)/2, 20, 20);
            add(abilityInfo);

            pos = abilityButton.bottom() + GAP;
        }

        RedButton cancelButton = new RedButton(Messages.get(this, "cancel")){
            @Override
            protected void onClick() {
                hide();
            }
        };
        cancelButton.setRect(0, pos, WIDTH, 18);
        add(cancelButton);
        pos = cancelButton.bottom() + GAP;

        resize(WIDTH, (int)pos);

    }
    private boolean invalidFLUps(String[] flUpgrades, String str){
        for(String s : flUpgrades){
            if(s!=null){
                if(Objects.equals(str, s)) return true;
            }
        }
        return false;
    }
}
