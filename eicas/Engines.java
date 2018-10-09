/**
* Engines.java
* 
* Engine instruments
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
/**
* Modified for CRJ 3-27-2017
**/
package net.sourceforge.xhsi.flightdeck.eicas;

//modified for CRJ 27 Mar 2016
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.Color;
import java.awt.BasicStroke;

import java.awt.Component;

import java.awt.Graphics2D;

import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.logging.Logger;

import net.sourceforge.xhsi.XHSIPreferences;
import net.sourceforge.xhsi.XHSISettings;

import net.sourceforge.xhsi.model.ModelFactory;



public class Engines extends EICASSubcomponent {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger("net.sourceforge.xhsi");


    private Stroke original_stroke;

    private boolean inhibit;
    private DecimalFormat one_decimal_format;
    private DecimalFormat two_decimals_format;
    private DecimalFormat three_wholes_format;
    private DecimalFormat four_wholes_format;
    private DecimalFormatSymbols format_symbols;

    private int prim_dial_x[] = new int[8];
    private int seco_dial_x[] = new int[8];


    public Engines(ModelFactory model_factory, EICASGraphicsConfig hsi_gc, Component parent_component) {
        super(model_factory, hsi_gc, parent_component);

        one_decimal_format = new DecimalFormat("##0.0");
        format_symbols = one_decimal_format.getDecimalFormatSymbols();
        format_symbols.setDecimalSeparator('.');
        one_decimal_format.setDecimalFormatSymbols(format_symbols);

        two_decimals_format = new DecimalFormat("#0.00");
        format_symbols = two_decimals_format.getDecimalFormatSymbols();
        format_symbols.setDecimalSeparator('.');
        two_decimals_format.setDecimalFormatSymbols(format_symbols);
        
        three_wholes_format = new DecimalFormat("#000");
        format_symbols = three_wholes_format.getDecimalFormatSymbols();
        format_symbols.setDecimalSeparator('.');
        three_wholes_format.setDecimalFormatSymbols(format_symbols);
        
        four_wholes_format = new DecimalFormat("#0000");
        format_symbols = four_wholes_format.getDecimalFormatSymbols();
        format_symbols.setDecimalSeparator('.');
        four_wholes_format.setDecimalFormatSymbols(format_symbols);

    }

    protected Path2D buildNeedlePath(int n1_radius) 
    {
       Path2D path = new Path2D.Double();
       
       //g2.drawLine(eicas_gc.prim_dial_x[pos], n1_y, eicas_gc.prim_dial_x[pos]+n1_r*11/10, n1_y);
       final int n1_radius_tenth = n1_radius/5;
       
       final Point2D.Double P1 = new Point2D.Double(0,n1_radius_tenth/4);
       final Point2D.Double P2 = new Point2D.Double(n1_radius-n1_radius_tenth,n1_radius_tenth/4);
       
       final Point2D.Double P3 = new Point2D.Double(n1_radius-n1_radius_tenth,n1_radius_tenth/2);
       
       final Point2D.Double P4 = new Point2D.Double(n1_radius,0);//point
       
       final Point2D.Double P5 = new Point2D.Double(n1_radius-n1_radius_tenth,-n1_radius_tenth/2);
       
       final Point2D.Double P6 = new Point2D.Double(n1_radius-n1_radius_tenth,-n1_radius_tenth/4);
       final Point2D.Double P7 = new Point2D.Double(0,-n1_radius_tenth/4);
       
       path.moveTo(P1.x,P1.y);
       path.lineTo(P2.x,P2.y);
       path.lineTo(P3.x,P3.y);
       path.lineTo(P4.x,P4.y);
       path.lineTo(P5.x,P5.y);
       path.lineTo(P6.x,P6.y);
       path.lineTo(P7.x,P7.y);
       
       return path;
    }
    protected void drawCRJNeedle(Graphics2D g2, int x,int y,int r) {
    
      // CRJ arrow needle
      //g2.setColor(eicas_gc.ecam_normal_color);
      Path2D needle = buildNeedlePath(r);
      g2.translate(x,y);
      g2.draw(needle);
      g2.translate(-x,-y);
    
    }
    public void paint(Graphics2D g2) {
        int num = this.aircraft.num_engines();
        int ff_row = eicas_gc.dial_main4_y;
        int oilt_row = ff_row + eicas_gc.dial_font_h[num];
        int oilp_row = oilt_row + eicas_gc.dial_font_h[num];
        
        if ( eicas_gc.boeing_style && eicas_gc.powered && ( this.aircraft.num_engines() > 0 ) ) {


            this.inhibit = ( this.aircraft.agl_m() < 1000.0f / 3.28084f );


            boolean piston = ( this.avionics.get_engine_type() == XHSISettings.ENGINE_TYPE_MAP );
            boolean turboprop = ( this.avionics.get_engine_type() == XHSISettings.ENGINE_TYPE_TRQ );
            boolean epr_jet = ( this.avionics.get_engine_type() == XHSISettings.ENGINE_TYPE_EPR );
            
            int num_eng = this.aircraft.num_engines();

            int cols = Math.max(num_eng, 2);
            for (int i=0; i<cols; i++) {
                prim_dial_x[i] = eicas_gc.panel_rect.x + eicas_gc.dials_width*50/100/cols + i*eicas_gc.dials_width/cols;
                seco_dial_x[i] = eicas_gc.alerts_x0 + i*eicas_gc.alerts_w/cols + (eicas_gc.alerts_w/cols*15/16)/2;
            }

         
            if(true) /* crj jet only most be jet */ {

                for (int i=0; i<num_eng; i++) {

                    drawN1(g2, i, num_eng, epr_jet);
                    //3-27-2017 crj
                    drawITT(g2,i,num_eng);
                    drawN2(g2, i, num_eng);
                    
                    
                    drawFFDigital(g2,i,num_eng,eicas_gc.dial_main4_y);
                    drawOilTDigital(g2,i,num_eng,eicas_gc.dial_main4_y + eicas_gc.dial_font_h[num_eng]);
                    drawOilPDigital(g2,i,num_eng,eicas_gc.dial_main4_y + 2*eicas_gc.dial_font_h[num_eng]);
                    
                    //drawOilP(g2,i,num_eng);
                    
                    if(this.aircraft.get_N2(0)>55.0f && this.aircraft.get_N2(1)>55.0f)
                      drawVIB(g2,i,num_eng);
                    else
                      drawOilP(g2,i,num_eng);
                    
                }
                
            }

            String ind_str;
            int ind_x = 0;
            g2.setColor(eicas_gc.color_boeingcyan);
            g2.setFont(eicas_gc.font_m);
            
            int dh = (eicas_gc.dial_font_h[num_eng])/2;
            if(dh<0) dh=-dh;
            
            // main1
            ind_str = "N1";
            if ( cols == 2 ) {
                ind_x = (prim_dial_x[0] + prim_dial_x[1]) / 2 - eicas_gc.get_text_width(g2, eicas_gc.font_m, ind_str)/2;
            } else {
                //ind_x = prim_dial_x[num_eng-1] + eicas_gc.dial_r[num_eng]*145/100 - eicas_gc.get_text_width(g2, eicas_gc.font_m, ind_str);
            }
            g2.drawString(ind_str, ind_x, eicas_gc.dial_main1_y + Math.min(eicas_gc.eicas_size*9/100 + eicas_gc.dial_font_h[num_eng], eicas_gc.dial_r[2]) - 2);
            
            // main2
            ind_str = "ITT";
            if ( cols == 2 ) {
                ind_x = (prim_dial_x[0] + prim_dial_x[1]) / 2 - eicas_gc.get_text_width(g2, eicas_gc.font_m, ind_str)/2;
            } else {
                //ind_x = prim_dial_x[num_eng-1] + eicas_gc.dial_r[num_eng]*145/100 - eicas_gc.get_text_width(g2, eicas_gc.font_m, ind_str);
            }
            g2.drawString(ind_str, ind_x, eicas_gc.dial_main2_y + Math.min(eicas_gc.eicas_size*9/100 + eicas_gc.dial_font_h[num_eng], eicas_gc.dial_r[2]) - 2);

            if (true) { // was: ( piston || turboprop || ! this.preferences.get_eicas_primary_only() ) {
                
                // main3
                ind_str = "N2";
                if ( cols == 2 ) {
                    ind_x = (prim_dial_x[0] + prim_dial_x[1]) / 2 - eicas_gc.get_text_width(g2, eicas_gc.font_m, ind_str)/2;
                } else {
                    //ind_x = prim_dial_x[num_eng-1] + eicas_gc.dial_r[num_eng]*145/100 - eicas_gc.get_text_width(g2, eicas_gc.font_m, ind_str);
                }
                g2.drawString(ind_str, ind_x, eicas_gc.dial_main3_y + Math.min(eicas_gc.eicas_size*9/100 + eicas_gc.dial_font_h[num_eng], eicas_gc.dial_r[2]) - 2);
                
                g2.setFont(eicas_gc.font_xs);

            }

            if (true) {
                
                // main4
                ind_str = "FF (PPH)";
                if ( cols == 2 ) {
                    ind_x = (prim_dial_x[0] + prim_dial_x[1]) / 2 - eicas_gc.get_text_width(g2, eicas_gc.font_xs, ind_str)/2;
                } else {
                    //ind_x = prim_dial_x[num_eng-1] + eicas_gc.dial_r[num_eng]*145/100 - eicas_gc.get_text_width(g2, eicas_gc.font_m, ind_str);
                }
                g2.drawString(ind_str, ind_x,  ff_row-dh);

                // main5
                if (true) {
                    ind_str = "OIL TEMP";
                    if ( cols == 2 ) {
                        ind_x = (prim_dial_x[0] + prim_dial_x[1]) / 2 - eicas_gc.get_text_width(g2, eicas_gc.font_xs, ind_str)/2;
                    } else {
                        //ind_x = prim_dial_x[num_eng-1] + eicas_gc.dial_r[num_eng]*145/100 - eicas_gc.get_text_width(g2, eicas_gc.font_m, ind_str);
                    }
                    g2.drawString(ind_str, ind_x, oilt_row-dh);
                }
                // main5
                if (true) {
                    ind_str = "OIL PRESS";
                    if ( cols == 2 ) {
                        ind_x = (prim_dial_x[0] + prim_dial_x[1]) / 2 - eicas_gc.get_text_width(g2, eicas_gc.font_xs, ind_str)/2;
                    } else {
                        //ind_x = prim_dial_x[num_eng-1] + eicas_gc.dial_r[num_eng]*145/100 - eicas_gc.get_text_width(g2, eicas_gc.font_m, ind_str);
                    }
                    g2.drawString(ind_str, ind_x, oilp_row-dh);
                }
                g2.setFont(eicas_gc.font_xs);
                
            }

            if ( ! this.preferences.get_eicas_primary_only() ) {

                // OIL P
                ind_str = "OIL P";
                if ( cols == 2 ) {
                    ind_x = (seco_dial_x[0] + seco_dial_x[1]) / 2 - eicas_gc.get_text_width(g2, eicas_gc.font_xs, ind_str)/2;
                } else {
                    ind_x = seco_dial_x[0] - eicas_gc.dial_r[num_eng]*85/100;
                }
                g2.drawString(ind_str, ind_x, eicas_gc.dial_oil_p_y + eicas_gc.dial_r[2]*70/100 + eicas_gc.line_height_xs);
                // OIL T
                ind_str = "OIL T";
                if ( cols == 2 ) {
                    ind_x = (seco_dial_x[0] + seco_dial_x[1]) / 2 - eicas_gc.get_text_width(g2, eicas_gc.font_xs, ind_str)/2;
                } else {
                    ind_x = seco_dial_x[0] - eicas_gc.dial_r[num_eng]*85/100;
                }
                g2.drawString(ind_str, ind_x, eicas_gc.dial_oil_t_y + eicas_gc.dial_r[2]*70/100 + eicas_gc.line_height_xs);
                // OIL Q
                ind_str = "OIL Q";
                if ( cols == 2 ) {
                    ind_x = (seco_dial_x[0] + seco_dial_x[1]) / 2 - eicas_gc.get_text_width(g2, eicas_gc.font_xs, ind_str)/2;
                } else {
                    ind_x = seco_dial_x[0] - eicas_gc.dial_r[num_eng]*85/100;
                }
                g2.drawString(ind_str, ind_x, eicas_gc.dial_oil_q_y + eicas_gc.line_height_xs*35/20);
                // VIB or NG or CHT
                // CHT not implemented, NG goes to primary
                if ( ! piston && ! turboprop ) {
                    ind_str = piston ? "CHT" : ( turboprop ? "NG" : "VIB" );
                    if ( cols == 2 ) {
                        ind_x = (seco_dial_x[0] + seco_dial_x[1]) / 2 - eicas_gc.get_text_width(g2, eicas_gc.font_xs, ind_str)/2;
                    } else {
                        ind_x = seco_dial_x[0] - eicas_gc.dial_r[num_eng]*85/100;
                    }
                    g2.drawString(ind_str, ind_x, eicas_gc.dial_vib_y + eicas_gc.dial_r[2]*70/100 + eicas_gc.line_height_xs);
                }

            }

        }

    }


    private void drawN1(Graphics2D g2, int pos, int num, boolean with_epr) {

        AffineTransform original_at = g2.getTransform();
        scalePen(g2);

        float n1_value = this.aircraft.get_N1(pos);
        float n1_dial = Math.min(n1_value, 110.0f) / 100.0f;
        int epr_value = Math.round( this.aircraft.get_EPR(pos) * 100.0f );
        String n1_str = with_epr ? Integer.toString(epr_value) : one_decimal_format.format(n1_value);

        int n1_y = eicas_gc.dial_main1_y;
        int n1_r = eicas_gc.dial_r[num];
        int n1_box_y = n1_y - n1_r/8;
        
        //3-27-2017 no filled arc for CRJ
        /*
        if ( ( n1_dial <= 1.0f ) || this.inhibit ) {
            // inhibit caution or warning below 1000ft
            g2.setColor(eicas_gc.instrument_background_color);
        } else if ( n1_dial < 1.1f ) {
            g2.setColor(eicas_gc.caution_color.darker().darker());
        } else {
            g2.setColor(eicas_gc.warning_color.darker().darker());
        }
        g2.fillArc(prim_dial_x[pos]-n1_r, n1_y-n1_r, 2*n1_r, 2*n1_r, 0, -Math.round(n1_dial*200.0f));
        */
        
        /* 3-27-2017 no scale for CRJ
        // scale markings every 10%
        g2.setColor(eicas_gc.dim_markings_color);
        for (int i=0; i<=10; i++) {
            g2.drawLine(prim_dial_x[pos]+n1_r*14/16, n1_y, prim_dial_x[pos]+n1_r-1, n1_y);
            g2.rotate(Math.toRadians(20), prim_dial_x[pos], n1_y);
        }
        g2.setTransform(original_at);
        
        // scale numbers 2, 4, 6, 8 and 10
        if ( num <= 4 ) {
            g2.setFont(eicas_gc.font_xs);
            int n1_digit_x;
            int n1_digit_y;
            int n1_digit_angle = 40;
            for (int i=2; i<=10; i+=2) {
                n1_digit_x = prim_dial_x[pos] + (int)(Math.cos(Math.toRadians(n1_digit_angle))*n1_r*11/16);
                n1_digit_y = n1_y + (int)(Math.sin(Math.toRadians(n1_digit_angle))*n1_r*11/16);
                g2.drawString(Integer.toString(i), n1_digit_x - eicas_gc.digit_width_xs/2, n1_digit_y+eicas_gc.line_height_xs*3/8);
                n1_digit_angle += 40;
            }
        }
        */
        g2.setColor(eicas_gc.ecam_normal_color);//3-27-2016 added for crj arc color
        
        g2.drawArc(prim_dial_x[pos]-n1_r, n1_y-n1_r, 2*n1_r, 2*n1_r, 0, -200);
        g2.setColor(eicas_gc.caution_color);
        g2.drawArc(prim_dial_x[pos]-n1_r, n1_y-n1_r, 2*n1_r, 2*n1_r, -200, -20);
        g2.rotate(Math.toRadians(220), prim_dial_x[pos], n1_y);
        g2.setColor(eicas_gc.warning_color);
        g2.drawLine(prim_dial_x[pos]+n1_r, n1_y, prim_dial_x[pos]+n1_r*19/16, n1_y);
        g2.setTransform(original_at);

        // needle
        g2.rotate(Math.toRadians(Math.round(n1_dial*200.0f)), prim_dial_x[pos], n1_y);
        //g2.setColor(eicas_gc.markings_color);
        //g2.drawLine(prim_dial_x[pos], n1_y, prim_dial_x[pos]+n1_r-2, n1_y);
        
        // CRJ arrow needle
        g2.setColor(eicas_gc.ecam_normal_color);
        Path2D needle = buildNeedlePath(n1_r-2);
        g2.translate(prim_dial_x[pos],n1_y);
        g2.draw(needle);
        g2.translate(-prim_dial_x[pos],-n1_y);
        
       
        g2.setTransform(original_at);


        // value box
        if ( num < 5 ) {
            //g2.setColor(eicas_gc.markings_color);
            //3-27-2017 no box for CRJ
            //g2.setColor(eicas_gc.dim_markings_color);
            //g2.drawRect(prim_dial_x[pos], n1_box_y - eicas_gc.dial_font_h[num]*140/100, eicas_gc.dial_font_w[num]*55/10, eicas_gc.dial_font_h[num]*140/100);
            if ( ( n1_dial <= 1.0f ) || this.inhibit ) {
                // inhibit caution or warning below 1000ft
                g2.setColor(eicas_gc.markings_color);
            } else if ( n1_dial < 1.1f ) {
                g2.setColor(eicas_gc.caution_color);
            } else {
                g2.setColor(eicas_gc.warning_color);
            }
            g2.setFont(eicas_gc.dial_font[num]);
            g2.drawString(n1_str, prim_dial_x[pos]+eicas_gc.dial_font_w[num]*51/10-eicas_gc.get_text_width(g2, eicas_gc.dial_font[num], n1_str), n1_box_y-eicas_gc.dial_font_h[num]*25/100-2);
        }


        // Reverser
        float rev = this.aircraft.reverser_position(pos);

        if ( rev > 0.0f ) {
            if ( rev == 1.0f ) {
                g2.setColor(eicas_gc.color_lime);
            } else {
                g2.setColor(eicas_gc.caution_color);
            }
            g2.drawString("REV", prim_dial_x[pos]+eicas_gc.dial_font_w[num], n1_box_y-eicas_gc.dial_font_h[num]*165/100-2);
        }

        // target N1 bug not for reverse
        if ( (rev==0.0f) ) {

            float ref_n1 = this.aircraft.get_ref_N1(pos);

            if ( ref_n1 > 0.0f ) {

if ( ref_n1 <= 1.0f ) {
    logger.warning("UFMC N1 is probably ratio, not percent");
    ref_n1 *= 100.0f;
}
                g2.setColor(eicas_gc.color_lime);
                g2.rotate(Math.toRadians(ref_n1*2.0f), prim_dial_x[pos], n1_y);
                g2.drawLine(prim_dial_x[pos]+n1_r+1, n1_y, prim_dial_x[pos]+n1_r+n1_r/10, n1_y);
                g2.drawLine(prim_dial_x[pos]+n1_r+n1_r/10, n1_y, prim_dial_x[pos]+n1_r+n1_r/10+n1_r/8, n1_y+n1_r/10);
                g2.drawLine(prim_dial_x[pos]+n1_r+n1_r/10, n1_y, prim_dial_x[pos]+n1_r+n1_r/10+n1_r/8, n1_y-n1_r/10);
                g2.setTransform(original_at);
                String ref_n1_str = one_decimal_format.format(ref_n1);
                g2.drawString(ref_n1_str, prim_dial_x[pos]+eicas_gc.dial_font_w[num]*51/10-eicas_gc.get_text_width(g2, eicas_gc.dial_font[num], ref_n1_str), n1_box_y-eicas_gc.dial_font_h[num]*165/100-2);

            }

        }

        resetPen(g2);

    }


    private void drawEGT(Graphics2D g2, int pos, int num, int line) {

        AffineTransform original_at = g2.getTransform();
        scalePen(g2);

        float egt_percent = this.aircraft.get_EGT_percent(pos);
        float egt_dial = Math.min(egt_percent, 110.0f) / 100.0f;
        int egt_value = Math.round(this.aircraft.get_EGT_value(pos));
//egt_value=500;

        int egt_x = prim_dial_x[pos];
        int egt_y = ( line == 2 ) ? eicas_gc.dial_main2_y :eicas_gc.dial_main3_y;
        int egt_r = eicas_gc.dial_r[num];

        if ( ( egt_dial <= 1.0f ) || this.inhibit ) {
            // inhibit caution or warning below 1000ft
            g2.setColor(eicas_gc.instrument_background_color);
        } else if ( egt_dial < 1.1f ) {
            g2.setColor(eicas_gc.caution_color.darker().darker());
        } else {
            g2.setColor(eicas_gc.warning_color.darker().darker());
        }
        g2.fillArc(egt_x-egt_r, egt_y-egt_r, 2*egt_r, 2*egt_r, 0, -Math.round(egt_dial*200.0f));

        g2.setColor(eicas_gc.dim_markings_color);
        g2.drawArc(egt_x-egt_r, egt_y-egt_r, 2*egt_r, 2*egt_r, 0, -200);
        g2.setColor(eicas_gc.caution_color);
        g2.drawArc(egt_x-egt_r, egt_y-egt_r, 2*egt_r, 2*egt_r, -200, -20);
        g2.rotate(Math.toRadians(220), egt_x, egt_y);
        g2.setColor(eicas_gc.warning_color);
        g2.drawLine(egt_x+egt_r, egt_y, egt_x+egt_r*19/16, egt_y);
        g2.setTransform(original_at);

        // needle
        g2.rotate(Math.toRadians(Math.round(egt_dial*200.0f)), egt_x, egt_y);
        g2.setColor(eicas_gc.markings_color);
        g2.drawLine(egt_x, egt_y, egt_x+egt_r-2, egt_y);
        g2.setTransform(original_at);

        // value box
        egt_y -= egt_r/8;
        if ( num < 5 ) {
            g2.setColor(eicas_gc.dim_markings_color);
            g2.drawRect(egt_x, egt_y - eicas_gc.dial_font_h[num]*140/100, eicas_gc.dial_font_w[num]*47/10, eicas_gc.dial_font_h[num]*140/100);
            if ( ( egt_dial <= 1.0f ) || this.inhibit ) {
                // inhibit caution or warning below 1000ft
                g2.setColor(eicas_gc.markings_color);
            } else if ( egt_dial < 1.1f ) {
                g2.setColor(eicas_gc.caution_color);
            } else {
                g2.setColor(eicas_gc.warning_color);
            }
            g2.setFont(eicas_gc.dial_font[num]);
            String egt_str = Integer.toString(egt_value);
            g2.drawString(egt_str, egt_x+eicas_gc.dial_font_w[num]*44/10-eicas_gc.get_text_width(g2, eicas_gc.dial_font[num], egt_str), egt_y-eicas_gc.dial_font_h[num]*25/100-2);
        }

        resetPen(g2);

    }
    
    private void drawITT(Graphics2D g2, int pos, int num) {
     
        AffineTransform original_at = g2.getTransform();
        scalePen(g2);

        float itt_percent = this.aircraft.get_ITT_percent(pos);

        float itt_dial = Math.min(itt_percent, 110.0f) / 100.0f;
        int itt_value = Math.round(this.aircraft.get_ITT_value(pos));


        int itt_x = prim_dial_x[pos];
        //3-27-2017 CRJ
        int itt_y = eicas_gc.dial_main2_y;
        //int itt_y = eicas_gc.dial_main3_y;
        int itt_r = eicas_gc.dial_r[num];

        if ( itt_dial <= 1.0f ) {
            g2.setColor(eicas_gc.instrument_background_color);
        } else {
            g2.setColor(eicas_gc.warning_color.darker().darker());
        }
        
        //3-27-2017
        //g2.fillArc(itt_x-itt_r, itt_y-itt_r, 2*itt_r, 2*itt_r, 0, -Math.round(itt_dial*200.0f));

        g2.setColor(eicas_gc.dim_markings_color);
        g2.drawArc(itt_x-itt_r, itt_y-itt_r, 2*itt_r, 2*itt_r, 0, -200);
        g2.setColor(eicas_gc.warning_color);
        g2.drawArc(itt_x-itt_r, itt_y-itt_r, 2*itt_r, 2*itt_r, -200, -20);
        g2.rotate(Math.toRadians(200), itt_x, itt_y);
        g2.drawLine(itt_x+itt_r, itt_y, itt_x+itt_r*19/16, itt_y);
        g2.setTransform(original_at);

        // needle
        g2.rotate(Math.toRadians(Math.round(itt_dial*200.0f)), itt_x, itt_y);
        //g2.setColor(eicas_gc.markings_color);
        //g2.drawLine(itt_x, itt_y, itt_x+itt_r-2, itt_y);
        //3-27-2017 CRJ Needle
        g2.setColor(eicas_gc.ecam_normal_color);
        drawCRJNeedle(g2,itt_x,itt_y,itt_r-2);
        g2.setTransform(original_at);

        // value box
        itt_y -= itt_r/8;
        if ( num < 5 ) {
            g2.setColor(eicas_gc.dim_markings_color);
            //3-27-2017
            //g2.drawRect(itt_x, itt_y - eicas_gc.dial_font_h[num]*140/100, eicas_gc.dial_font_w[num]*47/10, eicas_gc.dial_font_h[num]*140/100);
            if ( itt_dial <= 1.0f ) {
                g2.setColor(eicas_gc.markings_color);
            } else {
                g2.setColor(eicas_gc.warning_color);
            }
            g2.setFont(eicas_gc.dial_font[num]);
            String itt_str = Integer.toString(itt_value);
            g2.drawString(itt_str, itt_x+eicas_gc.dial_font_w[num]*44/10-eicas_gc.get_text_width(g2, eicas_gc.dial_font[num], itt_str), itt_y-eicas_gc.dial_font_h[num]*25/100-2);
        }

        resetPen(g2);

    }
    

    private void drawN2(Graphics2D g2, int pos, int num) {

        AffineTransform original_at = g2.getTransform();
        scalePen(g2);

        float n2_value = this.aircraft.get_N2(pos);

        float n2_dial = Math.min(n2_value, 110.0f) / 100.0f;

        int n2_y = eicas_gc.dial_main3_y;
        int n2_r = eicas_gc.dial_r[num];

        if ( ( n2_dial <= 1.0f ) || this.inhibit ) {
            // inhibit caution or warning below 1000ft
            g2.setColor(eicas_gc.instrument_background_color);
        } else if ( n2_dial < 1.1f ) {
            g2.setColor(eicas_gc.caution_color.darker().darker());
        } else {
            g2.setColor(eicas_gc.warning_color.darker().darker());
        }
        //3-27-2017
        //g2.fillArc(prim_dial_x[pos]-n2_r, n2_y-n2_r, 2*n2_r, 2*n2_r, 0, -Math.round(n2_dial*200.0f));

        g2.setColor(eicas_gc.dim_markings_color);
        g2.drawArc(prim_dial_x[pos]-n2_r, n2_y-n2_r, 2*n2_r, 2*n2_r, 0, -200);
        g2.setColor(eicas_gc.caution_color);
        g2.drawArc(prim_dial_x[pos]-n2_r, n2_y-n2_r, 2*n2_r, 2*n2_r, -200, -20);
        g2.rotate(Math.toRadians(220), prim_dial_x[pos], n2_y);
        g2.setColor(eicas_gc.warning_color);
        g2.drawLine(prim_dial_x[pos]+n2_r, n2_y, prim_dial_x[pos]+n2_r*19/16, n2_y);
        g2.setTransform(original_at);

        
        //needle
        g2.rotate(Math.toRadians(Math.round(n2_dial*200.0f)), prim_dial_x[pos], n2_y);
      
        //3-27-2017 CRJ Needle
        g2.setColor(getN2ReadoutColor(n2_value,this.aircraft.icing()));
        drawCRJNeedle(g2,prim_dial_x[pos],n2_y,n2_r-2);
        
        g2.setTransform(original_at);

        // value box
        n2_y -= n2_r/8;
        if ( num < 5 ) {
            //g2.setColor(eicas_gc.dim_markings_color);
            //3-27-2017
            //g2.drawRect(prim_dial_x[pos], n2_y - eicas_gc.dial_font_h[num]*140/100, eicas_gc.dial_font_w[num]*55/10, eicas_gc.dial_font_h[num]*140/100);
            //if ( ( n2_dial <= 1.0f ) || this.inhibit ) {
                // inhibit caution or warning below 1000ft
            //    g2.setColor(eicas_gc.markings_color);
            //} else if ( n2_dial < 1.1f ) {
            //    g2.setColor(eicas_gc.caution_color);
            //} else {
            //    g2.setColor(eicas_gc.warning_color);
            //}
            g2.setFont(eicas_gc.dial_font[num]);
            String n2_str = one_decimal_format.format(n2_value);
            g2.drawString(n2_str, prim_dial_x[pos]+eicas_gc.dial_font_w[num]*51/10-eicas_gc.get_text_width(g2, eicas_gc.dial_font[num], n2_str), n2_y-eicas_gc.dial_font_h[num]*25/100-2);
        }

        resetPen(g2);

    }


 
    private void drawFFDigital(Graphics2D g2, int pos, int num, int row) {
    
      float unit_multiplier = this.aircraft.fuel_multiplier();
      float ff_value = this.aircraft.get_FF(pos) * 3600 * unit_multiplier;
      float ff_max = this.aircraft.get_max_FF() * 3600 * unit_multiplier;
      float ff_dial = ff_value / ff_max;
      
      g2.setFont(eicas_gc.dial_font[num]);
      String ff_str = four_wholes_format.format(ff_value);
      
      if(ff_value<100.0f) {
        g2.setColor(eicas_gc.ecam_caution_color);
        ff_str = "***";  
      }
      else {
        g2.setColor(eicas_gc.dim_markings_color);
      }
      
      g2.setFont(eicas_gc.dial_font[num]);
      
      g2.drawString(ff_str, 
                    prim_dial_x[pos]-(eicas_gc.get_text_width(g2, eicas_gc.dial_font[num], "8888"))/2, 
                    row-eicas_gc.dial_font_h[num]*25/100-2);
    
    }

    private void drawOilPDigital(Graphics2D g2, int pos, int num, int row) {
      float unit_multiplier = this.aircraft.fuel_multiplier();
      float oilp_value = this.aircraft.get_oil_press_psi(pos);
      
      if(oilp_value<20.0f) {
        g2.setColor(eicas_gc.ecam_caution_color);
      }
      else
        g2.setColor(eicas_gc.dim_markings_color);
        
        
      g2.setFont(eicas_gc.dial_font[num]);
      String oilp_str = three_wholes_format.format(oilp_value);
      g2.drawString(oilp_str, 
                    prim_dial_x[pos]-(eicas_gc.get_text_width(g2, eicas_gc.dial_font[num], "8888"))/2, 
                    row-eicas_gc.dial_font_h[num]*25/100-2);
    
    }
    private void drawOilP(Graphics2D g2, int pos, int num) {

        AffineTransform original_at = g2.getTransform();
        scalePen(g2);

        float oil_p_dial = this.aircraft.get_oil_press_ratio(pos);

        int oil_p_x = prim_dial_x[pos];
        int oil_p_y = eicas_gc.dial_main5_y;
        int oil_p_r = eicas_gc.dial_r[num] * 70 /100;

        g2.setColor(eicas_gc.dim_markings_color);
        g2.drawArc(oil_p_x-oil_p_r, oil_p_y-oil_p_r, 2*oil_p_r, 2*oil_p_r, 0, -200);
        g2.setColor(eicas_gc.caution_color);
        g2.drawArc(oil_p_x-oil_p_r, oil_p_y-oil_p_r, 2*oil_p_r, 2*oil_p_r, -200, -20);
        g2.setColor(eicas_gc.warning_color);
        g2.drawArc(oil_p_x-oil_p_r, oil_p_y-oil_p_r, 2*oil_p_r, 2*oil_p_r, -220, -5);

        // needle
        g2.rotate(Math.toRadians( Math.round(oil_p_dial*220.0f) + 0 ), oil_p_x, oil_p_y);
        g2.setColor(eicas_gc.markings_color);
        g2.drawLine(oil_p_x, oil_p_y, oil_p_x+oil_p_r-2, oil_p_y);
        g2.setTransform(original_at);

        resetPen(g2);

    }

    private void drawOilTDigital(Graphics2D g2, int pos, int num, int row) {
      float unit_multiplier = this.aircraft.fuel_multiplier();
      float oilt_value = this.aircraft.get_oil_temp_c(pos);
      
      g2.setColor(eicas_gc.dim_markings_color);
        
      int dial_r = eicas_gc.dial_r[num];
      
      g2.setFont(eicas_gc.dial_font[num]);
      String oilt_str = three_wholes_format.format(oilt_value);
      g2.drawString(oilt_str, 
                    prim_dial_x[pos]-(eicas_gc.get_text_width(g2, eicas_gc.dial_font[num], "8888"))/2, 
                    row-eicas_gc.dial_font_h[num]*25/100-2);
      
    
    }
    private void drawOilT(Graphics2D g2, int pos, int num) {

        AffineTransform original_at = g2.getTransform();
        scalePen(g2);

        float oil_t_dial = this.aircraft.get_oil_temp_ratio(pos);

        int oil_t_x = seco_dial_x[pos];
        int oil_t_y = eicas_gc.dial_oil_t_y;
        int oil_t_r = eicas_gc.dial_r[num] * 70 /100;

        g2.setColor(eicas_gc.dim_markings_color);
        g2.drawArc(oil_t_x-oil_t_r, oil_t_y-oil_t_r, 2*oil_t_r, 2*oil_t_r, -45, -180);
        g2.setColor(eicas_gc.caution_color);
        g2.drawArc(oil_t_x-oil_t_r, oil_t_y-oil_t_r, 2*oil_t_r, 2*oil_t_r, -45-180, -15);
        g2.setColor(eicas_gc.warning_color);
        g2.drawArc(oil_t_x-oil_t_r, oil_t_y-oil_t_r, 2*oil_t_r, 2*oil_t_r, -45-180-15, -45);

        // needle
        g2.rotate(Math.toRadians( Math.round(oil_t_dial*240.0f) + 45), oil_t_x, oil_t_y);
        g2.setColor(eicas_gc.markings_color);
        g2.drawLine(oil_t_x, oil_t_y, oil_t_x+oil_t_r-2, oil_t_y);
        g2.setTransform(original_at);

        resetPen(g2);

    }


    private void drawOilQ(Graphics2D g2, int pos) {

        int oil_q_val = Math.round( this.aircraft.get_oil_quant_ratio(pos) * 100.0f );
        String oil_q_str = "" + oil_q_val;

        int oil_q_x = seco_dial_x[pos] - eicas_gc.get_text_width(g2, eicas_gc.font_l, oil_q_str)/2;
        int oil_q_y = eicas_gc.dial_oil_q_y + eicas_gc.line_height_l/2;

        g2.setColor(eicas_gc.markings_color);
        g2.setFont(eicas_gc.font_l);
        g2.drawString(oil_q_str, oil_q_x, oil_q_y);

    }


    private void drawVIB(Graphics2D g2, int pos, int num) {

        AffineTransform original_at = g2.getTransform();
        scalePen(g2);

        float vib_dial = this.aircraft.get_vib(pos) / 100.0f;

        int vib_x = prim_dial_x[pos];
        int vib_y = eicas_gc.dial_main5_y;
        int vib_r = eicas_gc.dial_r[num] * 70 /100;

        g2.setColor(eicas_gc.instrument_background_color);
        g2.fillArc(vib_x-vib_r, vib_y-vib_r, 2*vib_r, 2*vib_r, 0, -Math.round(vib_dial*220.0f));

        g2.setColor(eicas_gc.dim_markings_color);
        g2.drawArc(vib_x-vib_r, vib_y-vib_r, 2*vib_r, 2*vib_r, 0, -220);

        // needle
        g2.rotate(Math.toRadians( Math.round(vib_dial*220.0f) + 0 ), vib_x, vib_y);
        g2.setColor(eicas_gc.markings_color);
        g2.drawLine(vib_x, vib_y, vib_x+vib_r-2, vib_y);
        g2.setTransform(original_at);

        resetPen(g2);

    }
    private Color getN2ReadoutColor(float N2, boolean wing_anti_ice) {
      Color readoutColor;
      
      if(!wing_anti_ice) {
        if(N2<99.3) readoutColor = eicas_gc.ecam_normal_color;
        else readoutColor = eicas_gc.ecam_warning_color;
      }
      else {
        if(N2<77.9) readoutColor = eicas_gc.ecam_caution_color;
        else if(N2<99.2) readoutColor = eicas_gc.ecam_normal_color;
        else readoutColor = eicas_gc.ecam_warning_color;
      }
      
      return readoutColor;
    }

    private void scalePen(Graphics2D g2) {

        original_stroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2.5f * eicas_gc.grow_scaling_factor, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

    }


    private void resetPen(Graphics2D g2) {

        g2.setStroke(original_stroke);

    }


}
