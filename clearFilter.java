import java.awt.image.RGBImageFilter;

public class clearFilter extends RGBImageFilter
{
    public clearFilter ()
    {
        canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int pixel )
    {
        return 	0 << 24
        |	pixel & 0xff0000
        |	pixel & 0xff00
        |	pixel & 0xff;
    }
}

