import java.awt.image.RGBImageFilter;

public class dimFilter extends RGBImageFilter
{
    int dim = 128;  // 0 to 255

    public dimFilter (int dim_amount)
    {
        dim = dim_amount;
        canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int pixel )
    {
        return 	255 << 24
        |	(((pixel & 0xff0000) >> 16) * dim/255) << 16
        |			(((pixel & 0xff00) >> 8) * dim/255) << 8
        |			(pixel & 0xff) * dim/255 ;
    }
}


