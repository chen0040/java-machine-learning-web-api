package com.github.chen0040.ml.images;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by xschen on 10/7/2016.
 */
public class ImageIntelliContextFactory {
    public static int get_rgb(int alpha, int r, int g, int b)
    {
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    public static int get_r(int rgb)
    {
        return (rgb >> 16) & 0xff;
    }

    public static int get_b(int rgb)
    {
        return (rgb & 0xff);
    }

    public static int get_g(int rgb)
    {
        return (rgb >> 8) & 0xff;
    }

    public static int get_luminance(int r, int g, int b)
    {
        return (int)(r * 0.3+ g * 0.59 + b * 0.11);
    }

    public static int get_luminance(int rgb)
    {
        int r=((rgb >> 16) & 0xff);
        int g=((rgb >> 8) & 0xff);
        int b=(rgb & 0xff);

        int gray=get_luminance(r, g, b);

        return gray;
    }

    public static IntelliTuple getPixelTuple(IntelliContext context, int rgb){
        IntelliTuple tuple = context.newTuple();

        double r = get_r(rgb);
        double g = get_g(rgb);
        double b = get_b(rgb);
        double l = get_luminance(rgb);
        tuple.set(0, r);
        tuple.set(1, g);
        tuple.set(2, b);
        tuple.set(3, l);

        return tuple;
    }

    private static Random rand = new Random();

    public static IntelliContext newContext(BufferedImage img){
        IntelliContext batch = new IntelliContext();
        for(int i=0; i < 3000; i++)
        {
            int x = rand.nextInt(img.getWidth());
            int y = rand.nextInt(img.getHeight());

            int rgb = img.getRGB(x, y);
            IntelliTuple tuple = getPixelTuple(batch, rgb);
            batch.add(tuple);
        }
        return batch;
    }


}
