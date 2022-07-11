package com.joojn.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageUtil {

    private static Color intToColor(int color)
    {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float blue = (float) (color >> 8 & 255) / 255.0F;
        float green = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        return new Color(red, green, blue, alpha);
    }

    public static BufferedImage createImageOverlay(BufferedImage image, int color)
    {
        Set<Position> positionList = getOverlayPositions(image);

        BufferedImage newImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);


        positionList.forEach(position -> {
            int x = position.getX();
            int y = position.getY();

            newImage.setRGB(x, y, color);
        });

        return newImage;
    }

    public static BufferedImage createImageOffsetOverlay(BufferedImage image, int lineWidth, int colorIn)
    {
        Set<Position> positionList = getOverlayPositions(image);

        BufferedImage newImage = new BufferedImage(
                image.getWidth() + lineWidth * 2,
                image.getHeight() + lineWidth * 2,
                BufferedImage.TYPE_INT_ARGB);

        positionList.forEach(position -> {
            int x = position.getX();
            int y = position.getY();

            for(int i = 1 ; i < lineWidth + 1 ; i++)
            {
                setImagePoint(image, newImage, x + i, y, lineWidth, colorIn);
                setImagePoint(image, newImage, x - i, y, lineWidth, colorIn);
                setImagePoint(image, newImage, x, y + i, lineWidth, colorIn);
                setImagePoint(image, newImage, x, y - i, lineWidth, colorIn);
            }
        });

        return newImage;
    }

    private static Set<Position> getOverlayPositions(BufferedImage image)
    {
        Set<Position> positionList = new HashSet<>();

        // for y values
        for(int x = 0; x < image.getWidth() ; x++)
        {
            Position topValue = null;

            // from top to bottom
            for(int y = 0 ; y < image.getHeight() ; y++)
            {
                Color color = intToColor(image.getRGB(x, y));

                if(color.getAlpha() > 0) {
                    topValue = new Position(x, y);
                    break;
                }
            }

            // if the top value is null that means bottomValue is also null, so no need to check
            if(topValue != null)
            {
                Position bottomValue = null;

                // from bottom to top
                for(int y = image.getHeight() - 1 ; y >= 0 ; y--)
                {
                    Color color = intToColor(image.getRGB(x, y));

                    if(color.getAlpha() > 0){
                        bottomValue = new Position(x, y);
                        break;
                    }
                }

                positionList.add(bottomValue);
                positionList.add(topValue);
            }
        }

        // for x values
        for(int y = 0 ; y < image.getHeight() ; y++)
        {
            Position leftPosition = null;
            Position rightPosition = null;

            for(int x = 0 ; x < image.getWidth() ; x++)
            {
                Color color = intToColor(image.getRGB(x, y));

                if(color.getAlpha() > 0){
                    leftPosition = new Position(x, y);
                    break;
                }
            }

            if(leftPosition != null)
            {
                for(int x = image.getWidth() - 1 ; x >= 0 ; x--)
                {
                    Color color = intToColor(image.getRGB(x, y));

                    if(color.getAlpha() > 0)
                    {
                        rightPosition = new Position(x, y);
                        break;
                    }
                }

                positionList.add(leftPosition);
                positionList.add(rightPosition);
            }
        }

        return positionList;
    }

    private static class Position{

        private final int x;
        private final int y;

        public Position(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public boolean equals(Object object)
        {
            if(this == object) return true;
            if(!(object instanceof Position)) return false;

            Position position = (Position) object;

            return position.x == this.x && position.y == this.y;
        }

        @Override
        public String toString()
        {
            return "Position[x=" + x + ", y=" + y + "]";
        }
    }

    private static void setImagePoint(BufferedImage originalImage, BufferedImage image, int x, int y, int lineWidth, int color)
    {
        try
        {
            if(intToColor(originalImage.getRGB(x, y)).getAlpha() == 0)
            {
                image.setRGB(x + lineWidth, y + lineWidth, color);
            }
        }
        // if the point x is like -1, because the new image is larger -> -1 + 1 == 0
        catch(ArrayIndexOutOfBoundsException ignored)
        {
            image.setRGB(x + lineWidth, y + lineWidth, color);
        }
    }

    public static BufferedImage scale(BufferedImage src, int w, int h)
    {
        BufferedImage img =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int x, y;

        int ww = src.getWidth();
        int hh = src.getHeight();

        int[] ys = new int[h];

        for (y = 0; y < h; y++)
            ys[y] = y * hh / h;

        for (x = 0; x < w; x++)
        {
            int newX = x * ww / w;

            for (y = 0; y < h; y++)
            {
                int col = src.getRGB(newX, ys[y]);
                img.setRGB(x, y, col);
            }
        }

        return img;
    }

    public static BufferedImage getScreenImage(int newWidth, int newHeight)
    {
        try
        {
            Robot robot = new Robot();
            Rectangle rect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            BufferedImage image = robot.createScreenCapture(rect);
            if(image.getWidth() == newWidth && image.getHeight() == newHeight) return image;

            return scale(image, newWidth, newHeight);
        }
        catch (AWTException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static java.util.List<java.util.List<Color>> getRGBFromImage(BufferedImage image)
    {
        java.util.List<java.util.List<Color>> rgbData = new ArrayList<>();

        for(int x = 0; x < image.getWidth() ; x++)
        {
            List<Color> rgbList = new ArrayList<>();

            for(int y = 0; y < image.getHeight(); y++)
            {
                int rgb = image.getRGB(x, y);

                float red = (float)(rgb >> 16 & 255) / 255.0F;
                float blue = (float)(rgb >> 8 & 255) / 255.0F;
                float green = (float)(rgb & 255) / 255.0F;
                float alpha = (float)(rgb >> 24 & 255) / 255.0F;

                rgbList.add(new Color(red, green, blue, alpha));
            }

            rgbData.add(rgbList);
        }

        return rgbData;
    }

    public static BufferedImage resizeImage(BufferedImage image, int x, int y, int width, int height)
    {
        assert image != null;
        assert x + width < image.getWidth();
        assert y + height < image.getHeight();
        assert x > 0 && y > 0 && width > 0 && height > 0;

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for(int newX = 0 ; newX < width ; newX++)
        {
            for(int newY = 0 ; newY < height ; newY++)
            {
                int posX = newX + x;
                int posY = newY + y;

                newImage.setRGB(newX, newY, image.getRGB(posX, posY));
            }
        }

        return newImage;
    }
}
