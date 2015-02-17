package org.jfxvnc.net.rfb.color;

/**
 * depth 24 (32bpp) little-endian rgb888
 * 
 * @author comtel
 *
 */
public interface IPixelFormatRgb888 {

    int BITS_PER_PIXEL = 32;
    int DEPTH = 24;
    int BIG_ENDIAN_FLAG = 0;
    int TRUE_COLOUR_FLAG = 1;
    int RED_MAX = 255;
    int GREEN_MAX = 255;
    int BLUE_MAX = 255;
    int RED_SHIFT = 16;
    int GREEN_SHIFT = 8;
    int BLUE_SHIFT = 0;

}
