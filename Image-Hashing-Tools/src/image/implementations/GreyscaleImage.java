package image.implementations;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import attack.IAttack;
import image.IImage;
import image.PixelUtils;
import utils.ImageUtils;

public class GreyscaleImage implements IImage<GreyscaleImage> {

	private final byte[] pixels;
	private final int width;
	private final int height;

	public GreyscaleImage(int width, int height) {
		this.width = width;
		this.height = height;
		// All arrays in java have default value null for objects and 0 for primitives.
		// Therefore, the default will be all black.
		this.pixels = new byte[PixelUtils.safeMult(width, height)];
	}

	// Pixel array is not copied. It becomes the backing array.
	public GreyscaleImage(byte[] pixels, int width, int height) throws IllegalArgumentException {
		if (pixels.length != PixelUtils.safeMult(width, height)) {
			throw new IllegalArgumentException("The length of the pixel array must be width * height.\n" + "Width: "
					+ width + "  Height: " + height + "  Pixel length: " + pixels.length);
		}
		if (pixels.length == 0) { throw new IllegalArgumentException("Width/Height may not be zero."); }
		this.width = width;
		this.height = height;
		this.pixels = pixels;
	}

	public GreyscaleImage(int[] pixels, int width, int height) throws IllegalArgumentException {
		if (pixels.length != PixelUtils.safeMult(width, height)) {
			throw new IllegalArgumentException("The length of the pixel array must be width * height.\n" + "Width: "
					+ width + "  Height: " + height + "  Pixel length: " + pixels.length);
		}
		if (pixels.length == 0) { throw new IllegalArgumentException("Width/Height may not be zero."); }
		this.width = width;
		this.height = height;
		this.pixels = PixelUtils.intArrayToByte(pixels);
	}

	public GreyscaleImage(byte[][] pixels, int width, int height) throws IllegalArgumentException {
		this.width = width;
		this.height = height;

		this.pixels = new byte[PixelUtils.safeMult(width, height)];
		for (int y = 0; y < this.height; y++) {
			byte[] currentArray = pixels[y];
			if (currentArray.length != this.width) {
				throw new IllegalArgumentException("All subarrays of pixels must be of length equal to the width.");
			}

			for (int x = 0; x < this.width; x++) {
				this.pixels[y * this.width + x] = currentArray[x];
			}
		}
	}

	public GreyscaleImage(IImage<?> img) {
		this.width = img.getWidth();
		this.height = img.getHeight();
		this.pixels = img.toGreyscale().getPixels();
	}

	public GreyscaleImage(BufferedImage img) {
		GreyscaleImage self = new RGBImage(img).toGreyscale();
		this.width = self.getWidth();
		this.height = self.getHeight();
		this.pixels = self.getPixels();
	}

	public GreyscaleImage(GreyscaleImage[] g) {
		if (g.length != 1) { throw new IllegalArgumentException("Array must contain exactly one color channel."); }
		GreyscaleImage self = new GreyscaleImage(g[0]);
		this.width = self.getWidth();
		this.height = self.getHeight();
		this.pixels = self.getPixels();
	}

	public GreyscaleImage(File imgFile) throws IOException {
		this(ImageUtils.openImage(imgFile));
	}

	public GreyscaleImage(URL imgURL) throws IOException {
		this(ImageUtils.openImage(imgURL));
	}

	public int getPixel(int index) throws ArrayIndexOutOfBoundsException {
		return this.pixels[index];
	}

	public int getPixel(int x, int y) throws ArrayIndexOutOfBoundsException {
		return this.pixels[y * this.width + x] & 0xff;
	}

	public void setPixel(int x, int y, int val) throws ArrayIndexOutOfBoundsException {
		this.pixels[y * this.width + x] = (byte) val;
	}

	public void setPixel(int index, int val) throws ArrayIndexOutOfBoundsException {
		this.pixels[index] = (byte) val;
	}

	public byte[] getPixels() { return this.pixels; }

	public int[] getIntPixels() {
		int[] intPixels = new int[this.pixels.length];
		for (int i = 0; i < intPixels.length; i++) {
			intPixels[i] = this.pixels[i] & 0xff;
		}
		return intPixels;
	}

	public byte[][] get2dPixels() {
		byte[][] pixel2d = new byte[this.width][this.height];
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				pixel2d[x][y] = this.pixels[y * this.width + x];
			}
		}
		return pixel2d;
	}

	public RGBImage recolor(Color c) {
		double redFactor = c.getRed() / 255.0;
		double greenFactor = c.getGreen() / 255.0;
		double blueFactor = c.getBlue() / 255.0;

		byte[] red = new byte[this.pixels.length];
		byte[] green = new byte[this.pixels.length];
		byte[] blue = new byte[this.pixels.length];

		int pixelValue;
		for (int i = 0; i < this.pixels.length; i++) {
			pixelValue = (this.pixels[i] & 0xff);
			red[i] = (byte) Math.round(pixelValue * redFactor);
			green[i] = (byte) Math.round(pixelValue * greenFactor);
			blue[i] = (byte) Math.round(pixelValue * blueFactor);
		}
		return new RGBImage(red, green, blue, this.width, this.height);
	}

	@Override
	public int getWidth() { return this.width; }

	@Override
	public int getHeight() { return this.height; }

	@Override
	public GreyscaleImage[] getChannels() { return new GreyscaleImage[] { this }; }

	@Override
	public GreyscaleImage deepClone() {
		return new GreyscaleImage(Arrays.copyOf(this.pixels, this.pixels.length), this.width, this.height);
	}

	@Override
	public GreyscaleImage resizeNearest(int width, int height) throws ArithmeticException {
		if (this.width == width && this.height == height) { return this.deepClone(); }
		// Packs and unpacks the int. This is okay, the performance cost is marginal.
		return rescaleNearest(width / (float) this.width, height / (float) this.height);
	}

	@Override
	public GreyscaleImage rescaleNearest(float widthFactor, float heightFactor) throws ArithmeticException {
		if (widthFactor == 1 && heightFactor == 1) { return this.deepClone(); }

		// Throws when new width * new height overflows int.maxvalue
		int newWidth = Math.toIntExact(Math.round(this.width * widthFactor));
		int newHeight = Math.toIntExact(Math.round(this.height * heightFactor));
		byte[] newPixels = new byte[PixelUtils.safeMult(newWidth, newHeight)];

		int xSample, ySample;
		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				xSample = (int) (x / widthFactor);
				ySample = (int) (y / heightFactor);
				newPixels[y * newWidth + x] = this.pixels[ySample * this.width + xSample];
			}
		}

		return new GreyscaleImage(newPixels, newWidth, newHeight);
	}

	@Override
	public GreyscaleImage resizeBilinear(int width, int height) {
		if (this.width == width && this.height == height) { return this.deepClone(); }

		byte[] scaled = new byte[PixelUtils.safeMult(width, height)];

		float xRatio = ((float) (this.width - 1)) / width;
		float yRatio = ((float) (this.height - 1)) / height;

		int offset = 0;
		int A, B, C, D, x, y, index, gray;
		float x_diff, y_diff;

		// @nof
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {

				// To tell the truth I don't really know what's going on here,
				// but it works, so I can't really complain.
				x = (int) (xRatio * j);
				y = (int) (yRatio * i);
				x_diff = (xRatio * j) - x;
				y_diff = (yRatio * i) - y;
				index = y * this.width + x;

				A = pixels[index] & 0xff;
				B = pixels[index + 1] & 0xff;
				C = pixels[index + this.width] & 0xff;
				D = pixels[index + this.width + 1] & 0xff;

				
				gray = (int) (A * (1 - x_diff) * (1 - y_diff) + 
							  B * (x_diff) * (1 - y_diff) + 
							  C * (y_diff) * (1 - x_diff) + 
							  D * (x_diff * y_diff));

				scaled[offset++] = (byte) gray;
			}
		}
		// @dof
		return new GreyscaleImage(scaled, width, height);
	}

	@Override
	public GreyscaleImage rescaleBilinear(float widthFactor, float heightFactor) {
		if (widthFactor == 1 && heightFactor == 1) { return this.deepClone(); }
		return resizeBilinear(Math.round(this.width * widthFactor), Math.round(this.height * heightFactor));
	}

	@Override
	public BufferedImage toBufferedImage() {
		BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_GRAY);
		img.setData(
				Raster.createRaster(img.getSampleModel(), new DataBufferByte(this.pixels, this.pixels.length), null));
		return img;
	}

	// Returns self.
	@Override
	public GreyscaleImage toGreyscale() {
		return this;
	}

	// Returns with each color channel backed by self
	@Override
	public RGBImage toRGB() {
		return new RGBImage(this, this, this);
	}

	// Returns new RGBAImage, with each color channel backed by this, and a new
	// opaque alpha channel.
	@Override
	public RGBAImage toRGBA() {
		// Zero alpha represents completely transparent, so we must set them all to
		// opaque.
		byte[] alpha = new byte[this.width * this.height];
		Arrays.fill(alpha, (byte) 255);
		return new RGBAImage(this.toRGB(), new GreyscaleImage(alpha, this.width, this.height));
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GreyscaleImage) {
			GreyscaleImage other = (GreyscaleImage) o;
			return java.util.Arrays.equals(this.pixels, other.getPixels()) && this.width == other.getWidth();
		}
		return false;
	}

	@Override
	public GreyscaleImage flipHorizontal() {
		return new GreyscaleImage(PixelUtils.flipHorizontal(this.pixels, this.width, this.height), this.width,
				this.height);
	}

	@Override
	public GreyscaleImage flipVertical() {
		return new GreyscaleImage(PixelUtils.flipVertical(this.pixels, this.width, this.height), this.width,
				this.height);
	}

	@Override
	public GreyscaleImage rotate90CW() {
		return new GreyscaleImage(PixelUtils.rotate90CW(pixels, this.width, this.height), this.height, this.width);
	}

	@Override
	public GreyscaleImage rotate90CCW() {
		return new GreyscaleImage(PixelUtils.rotate90CCW(this.pixels, this.width, this.height), this.height,
				this.width);
	}

	@Override
	public GreyscaleImage rotate180() {
		return new GreyscaleImage(PixelUtils.rotate180(this.pixels), this.width, this.height);
	}

	@Override
	public GreyscaleImage extractSubimage(int x1, int y1, int x2, int y2) {
		return PixelUtils.extractSubimage(this.pixels, this.width, this.height, x1, y1, x2, y2);
	}

	@Override
	public GreyscaleImage emplaceSubimage(GreyscaleImage subImage, int x1, int y1, int x2, int y2) {
		return new GreyscaleImage(
				PixelUtils.emplaceSubimage(this.pixels, this.width, this.height, subImage.getPixels(), x1, y1, x2, y2),
				this.width, this.height);
	}

	@Override
	public GreyscaleImage apply(IAttack<GreyscaleImage> attack) {
		return attack.applyToChannel(this);
	}

	@Override
	public boolean hasAlpha() {
		return false;
	}

}
