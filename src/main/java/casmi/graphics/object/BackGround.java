/*
 *   casmi
 *   http://casmi.github.com/
 *   Copyright (C) 2011, Xcoo, Inc.
 *
 *  casmi is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package casmi.graphics.object;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import casmi.graphics.Graphics;
import casmi.graphics.color.Color;
import casmi.graphics.color.ColorSet;
import casmi.graphics.element.Element;

/**
 * Material class.
 * Wrap JOGL and make it easy to use.
 *
 * @author Y. Ban
 */
public class BackGround extends Element implements ObjectRender{
    private double red;
    private double green;
    private double blue;
    private double gray;
    private Color c;
    private ColorSet cset;
    private enum colorMode {
        RGB, Gray, Color, ColorSet
    }

    private colorMode mode;

    /**
     * Creates BackGround object using gray scale.
     *
     * @param gray
     *                 The gray-scale value of the BackGround.
     */
    public BackGround(double gray) {
        this.gray = gray;
        mode = colorMode.Gray;
    }

    /**
     * Creates BackGround object using red, green, blue color values.
     *
     * @param red
     *                 The red color value of the BackGround.
     * @param green
     *                 The green color value of the BackGround.
     * @param blue
     *                 The blue color value of the BackGround.
     */
    public BackGround(double red,double green,double blue){
        this.red = red;
        this.green = green;
        this.blue = blue;
        mode = colorMode.RGB;
    }

    /**
     * Creates BackGround object using color
     *
     * @param color
     *                 The Color of the BackGround.
     *
     * @see casmi.graphics.color.Color
     */
    public BackGround(Color color){
        this.c = color;
        mode = colorMode.Color;
    }

    /**
     * Creates BackGround object using color
     *
     * @param colorSet
     *                 The ColorSet of the BackGround.
     *
     * @see casmi.graphics.color.ColorSet
     */
    public BackGround(ColorSet colorSet){
        this.cset = colorSet;
        mode = colorMode.ColorSet;
    }


    @Override
    public void render(Graphics g){
        switch(mode){
        case Gray:
            g.background((float)gray);
            break;
        case RGB:
            g.background((float)red, (float)green, (float)blue);
            break;
        case Color:
            g.background(c);
            break;
        case ColorSet:
            g.background(cset);
            break;
        }
    }


    @Override
    public void render(GL2 gl, GLU glu, int width, int height) {
    }

}
