/**
* GearIndicator.java
* 
* Oil end Fuel annunciators
* 
* Copyright (C) 2010-2011  Marc Rogiers (marrog.123@gmail.com)
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2 
* of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package net.sourceforge.xhsi.flightdeck.eicas;

import java.awt.BasicStroke;
//import java.awt.Color;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
//import java.awt.image.BufferedImage;

import java.util.logging.Logger;

//import net.sourceforge.xhsi.XHSISettings;

//import net.sourceforge.xhsi.model.Avionics;
import net.sourceforge.xhsi.model.ModelFactory;
//import net.sourceforge.xhsi.model.NavigationRadio;

//import net.sourceforge.xhsi.panel.GraphicsConfig;
//import net.sourceforge.xhsi.panel.Subcomponent;



public class GearIndicator extends EICASSubcomponent {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger("net.sourceforge.xhsi");

  

    private int gear_w;
    private int gear_x[] = new int[8];


    public GearIndicator(ModelFactory model_factory, EICASGraphicsConfig hsi_gc, Component parent_component) {
        super(model_factory, hsi_gc, parent_component);
    }


    public void paint(Graphics2D g2) {
        if ( eicas_gc.powered && eicas_gc.boeing_style) {
            drawAlerts(g2);
        }
    }


    private void drawAlerts(Graphics2D g2) {

        int gears = this.aircraft.num_gears();
        
        if(gears < 3) return;
        
       

        float nose_lowered =  this.aircraft.get_gear(0);
        float left_lowered = this.aircraft.get_gear(1);
        float right_lowered = this.aircraft.get_gear(2);
        
        float[] lowered = new float[3];
        lowered[0] = nose_lowered;
        lowered[1] = left_lowered;
        lowered[2] = right_lowered;
        
        int n = gears;

        if ( gears > 0 ) {

            gear_w = eicas_gc.gears_w/n * 15/16;
              
            for (int i=0; i<gears; i++) {

                gear_x[i] = eicas_gc.gears_x0 + i*eicas_gc.gears_w/n;

                drawGearLabel(g2);
                draw1Gear(g2, i, lowered[i]);
                

            }

        }
        
        
        int flaps_y0 = eicas_gc.gear_y0 + eicas_gc.gear_h + (eicas_gc.gear_h*2);
        gear_w = eicas_gc.gears_w/50;
        
        int[] flaps = {0,16,40,60,90};//table made by multiplying flap deg times 2;
        int flapmax = 90;
        
        //draw detent graphic
        for(int f:flaps) {
        
        
          g2.setColor(eicas_gc.markings_color);

          int x= gear_x[0] + (f*eicas_gc.gears_w)/100;
          g2.drawRect(x,flaps_y0,gear_w,gear_w*2);
        
        }
        
        
        float pos = this.aircraft.get_flap_position();
        float handle = this.aircraft.get_flap_handle();
        int detents = this.aircraft.get_flap_detents();
        if(pos<=0.25f) {
          pos = pos*16.0f/0.25f;
        }
        else if(pos<=0.5f){
          pos = pos*40.0f/0.5f;
        }
        else if(pos<=0.75f){
          pos = pos*60.0f/0.75f;
        }
        else {
          pos=pos*90.0f;
        }
        //System.out.println(pos+" "+handle+" "+detents);
        
        if(pos>0.0f) {
          float p = pos/100.0f;
          int w = Math.round(p*eicas_gc.gears_w); 
          g2.fillRect(gear_x[0],flaps_y0,w,gear_w*2);
        
        }
    }


    private void draw1Gear(Graphics2D g2, int g, float lowered) {
        String line1_str;
        if(lowered<0.1f) {
          g2.setColor(eicas_gc.markings_color);
          line1_str = "UP";  
        }
        else if(lowered>=0.1f && lowered<0.99f) {
        
          g2.setColor(eicas_gc.caution_color);
          line1_str = "///";
        }
        else {
          g2.setColor(eicas_gc.normal_color);
          line1_str = "DN";
        }
        boolean lit = true;
        if ( lit ) {
            
            g2.drawRect(gear_x[g], eicas_gc.gear_y0, gear_w, eicas_gc.gear_h);
            
            g2.setFont(eicas_gc.font_xs);
            g2.drawString(line1_str, 
              gear_x[g] + gear_w/2 - eicas_gc.get_text_width(g2, eicas_gc.font_xs, line1_str)/2,
              eicas_gc.gear_y0 + eicas_gc.gear_h/3 + eicas_gc.line_height_xs*45/40);
            
            }
        
        g2.drawRect(gear_x[g], eicas_gc.gear_y0, gear_w, eicas_gc.gear_h);

    }


    private void drawGearLabel(Graphics2D g2) {

        g2.setColor(eicas_gc.color_boeingcyan);
        g2.setFont(eicas_gc.font_s);
        String eng_str = "GEAR";
        g2.drawString(eng_str, gear_x[1] + gear_w/2 - eicas_gc.get_text_width(g2, eicas_gc.font_s, eng_str)/2, eicas_gc.gear_y0 - eicas_gc.line_height_s*3/8 - 2);
        
        int flaps_yl = eicas_gc.gear_y0 + 2*eicas_gc.gear_h;
        eng_str = "FLAPS";
        g2.drawString(eng_str, gear_x[1] + gear_w/2 - eicas_gc.get_text_width(g2, eicas_gc.font_s, eng_str)/2, flaps_yl - eicas_gc.line_height_s*3/8 - 2);

    }


}
