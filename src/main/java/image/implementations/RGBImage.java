package image.implementations;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;

import image.IImage;

public class RGBImage implements IImage<RGBImage> {

	private GreyscaleImage r;
	private GreyscaleImage g;
	private GreyscaleImage b;
	private int width;
	private int height;

	public RGBImage(int width, int height) {
		this.width = width;
		this.height = height;
		this.r = new GreyscaleImage(width, height);
		this.g = new GreyscaleImage(width, height);
		this.b = new GreyscaleImage(width, height);
	}

	public RGBImage(byte[] red, byte[] green, byte[] blue, int width, int height) throws IllegalArgumentException {
		this.width = width;
		this.height = height;
		// GreyscaleImage constructor will throw IllegalArgumentException if the byte
		// arrays are not all the same, correct size.
		this.r = new GreyscaleImage(red, width, height);
		this.g = new GreyscaleImage(green, width, height);
		this.b = new GreyscaleImage(blue, width, height);
	}

	public RGBImage(byte[][] red, byte[][] green, byte[][] blue, int width, int height) throws IllegalArgumentException {
		this.width = width;
		this.height = height;
		// GreyscaleImage constructor will throw IllegalArgumentException if the byte
		// arrays are not all the same, correct size.
		this.r = new GreyscaleImage(red, width, height);
		this.g = new GreyscaleImage(green, width, height);
		this.b = new GreyscaleImage(blue, width, height);
	}

	public RGBImage(GreyscaleImage img) {
		this.width = img.getWidth();
		this.height = img.getHeight();
		this.r = img.deepClone();
		this.g = img.deepClone();
		this.b = img.deepClone();
	}
	
	// r, g, b, become backing 
	public RGBImage(GreyscaleImage red, GreyscaleImage green, GreyscaleImage blue) {
		int len = red.getPixels().length;
		if (len != green.getPixels().length || len != blue.getPixels().length) {
			throw new IllegalArgumentException("All three images must be the same size.");
		}

		this.width = red.getWidth();
		this.height = red.getHeight();
		this.r = red;
		this.g = green;
		this.b = blue;
	}

	public RGBImage(BufferedImage img) {
		Raster raster = img.getRaster();
		byte[] imgPixels = ((DataBufferByte) raster.getDataBuffer()).getData();
		boolean alphaExists = img.getAlphaRaster() != null;

		this.width = raster.getWidth();
		this.height = raster.getHeight();
		byte[] red = new byte[width * height];
		byte[] green = new byte[width * height];
		byte[] blue = new byte[width * height];

		if (alphaExists) {
			for (int pixel = 0, idx = 0; pixel < imgPixels.length; idx++) {
				// 0xFF is the mask required to convert byte to int
				pixel++;
				blue[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				green[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				red[idx] = (byte) (imgPixels[pixel++] & 0xFF);
			}
		} else {
			for (int pixel = 0, idx = 0; pixel < imgPixels.length; idx++) {
				blue[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				green[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				red[idx] = (byte) (imgPixels[pixel++] & 0xFF);
			}
		}

		this.r = new GreyscaleImage(red, width, height);
		this.g = new GreyscaleImage(green, width, height);
		this.b = new GreyscaleImage(blue, width, height);

	}

	public RGBImage(File imgFile) throws IOException {
		this(ImageIO.read(imgFile));
	}

	public RGBImage(URL imgURL) throws IOException {
		this(ImageIO.read(imgURL));
	}

	public GreyscaleImage getRed() {
		return r;
	}

	public GreyscaleImage getGreen() {
		return g;
	}

	public GreyscaleImage getBlue() {
		return b;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public RGBImage deepClone() {
		return new RGBImage(r.deepClone(), g.deepClone(), b.deepClone());
	}

	@Override
	public RGBImage resizeNearest(int width, int height) {
		return new RGBImage(r.resizeNearest(width, height), g.resizeNearest(width, height),
				b.resizeNearest(width, height));
	}

	@Override
	public RGBImage resizeBilinear(int width, int height) {
		return new RGBImage(r.resizeBilinear(width, height), g.resizeBilinear(width, height),
				b.resizeBilinear(width, height));
	}

	@Override
	public RGBImage rescaleNearest(float widthFactor, float heightFactor) {
		return new RGBImage(r.rescaleNearest(widthFactor, heightFactor), g.rescaleNearest(widthFactor, heightFactor),
				b.rescaleNearest(widthFactor, heightFactor));
	}

	@Override
	public RGBImage rescaleBilinear(float widthFactor, float heightFactor) {
		return new RGBImage(r.rescaleBilinear(widthFactor, heightFactor), g.rescaleBilinear(widthFactor, heightFactor),
				b.rescaleBilinear(widthFactor, heightFactor));
	}

	@Override
	public BufferedImage toBufferedImage() {
		byte[] BIPixels = new byte[this.width * this.height * 3];

		byte[] blue = b.getPixels();
		byte[] green = g.getPixels();
		byte[] red = r.getPixels();

		int offset = 0;
		int pos = 0;
		for (;;) {
			BIPixels[offset++] = blue[pos];
			BIPixels[offset++] = green[pos];
			BIPixels[offset++] = red[pos];

			if (offset == BIPixels.length) {
				break;
			}
			pos++;
		}

		BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_3BYTE_BGR);
		img.setData(Raster.createRaster(img.getSampleModel(), new DataBufferByte(BIPixels, BIPixels.length), null));

		return img;
	}

	// Creates new
	@Override
	public GreyscaleImage toGreyscale() {
		byte[] red = this.r.getPixels();
		byte[] green = this.g.getPixels();
		byte[] blue = this.b.getPixels();

		// Paralell average as int
		int[] average = new int[this.width * this.height];
		Arrays.parallelSetAll(average,
				i -> Math.round((((red[i] & 0xff) + (green[i] & 0xff) + (blue[i] & 0xff)) / 3f)));
		
		// Convert to byte
		byte[] byteAverage = new byte[average.length];
		for (int i = 0; i < average.length; i++) {
			byteAverage[i] = (byte) average[i];
		}
		return new GreyscaleImage(byteAverage, this.width, this.height);
	}

	// Returns self
	@Override
	public RGBImage toRGB() {
		return this;
	}

	// Uses self to back RGBAImage
	@Override
	public RGBAImage toRGBA() {
		// Zero alpha represents completely transparent, so we must set them all to
		// opaque.
		byte[] alpha = new byte[this.width * this.height];
		Arrays.fill(alpha, (byte) 255);
		return new RGBAImage(this, alpha);
	}

}
