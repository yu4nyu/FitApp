package com.yuanyu.fitapp.utils.gifview;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * @hide
 * @author liao
 * 
 */
public class GifDecoder extends Thread {

	/** 状�?：正在解码中 */
	public static final int STATUS_PARSING = 0;
	/** 状�?：图片格式错�?*/
	public static final int STATUS_FORMAT_ERROR = 1;
	/** 状�?：打�?���?*/
	public static final int STATUS_OPEN_ERROR = 2;
	/** 状�?：解码成�?*/
	public static final int STATUS_FINISH = -1;

	public boolean isDestroy = false;

	private InputStream in;
	private InputStream backUpIn;
	private int status;

	public int width; // full image width
	public int height; // full image height
	private boolean gctFlag; // global color table used
	private int gctSize; // size of global color table
	private int loopCount = 1; // iterations; 0 = repeat forever

	private int[] gct; // global color table
	private int[] lct; // local color table
	private int[] act; // active color table

	private int bgIndex; // background color index
	private int bgColor; // background color
	private int lastBgColor; // previous bg color
	private int pixelAspect; // pixel aspect ratio

	private boolean lctFlag; // local color table flag
	private boolean interlace; // interlace flag
	private int lctSize; // local color table size

	private int ix, iy, iw, ih; // current image rectangle
	private int lrx, lry, lrw, lrh;
	private Bitmap image; // current frame
	private Bitmap lastImage; // previous frame

	private byte[] block = new byte[256]; // current data block
	private int blockSize = 0; // block size

	// last graphic control extension info
	private int dispose = 0;
	// 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev
	private int lastDispose = 0;
	private boolean transparency = false; // use transparent color
	private int delay = 0; // delay in milliseconds
	private int transIndex; // transparent color index

	private static final int MaxStackSize = 4096;
	// max decoder pixel stack size

	// LZW decoder working arrays
	private short[] prefix;
	private byte[] suffix;
	private byte[] pixelStack;
	private byte[] pixels;

	private int frameCount;

	private static final int MAX_QUEUE = 15;
	private Queue<GifFrame> frameQueue = new ArrayBlockingQueue<GifFrame>(MAX_QUEUE);
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition rCondition = lock.newCondition();
	private final Condition wCondition = lock.newCondition();
	private int icacheParse = 0; // 缓存解码成功的帧�?
	private boolean loopParse = false; // 是否已经进入了循环解�?
	private ArrayList<GifFrame> frameCache = new ArrayList<GifFrame>(frameCount);; // 当帧数小于MAX_QUEUE时，�?��的帧全部缓存
	private int iCurrentFrame = 0;
	private boolean loopCache = false;

	private GifAction action = null;

	private byte[] gifData = null;

	private boolean isLoop = false;

	private int fileType = 0; // 1:resource,2:file,3:byte[]

	private Resources res = null;
	private int resId = 0;
	private String strFileName = null;

	public GifDecoder(GifAction action) {
		this.action = action;
	}

	public GifDecoder(GifAction action, boolean isLoop) {
		this.action = action;
		this.isLoop = isLoop;
	}

	public void setLoopAnimation() {
		isLoop = true;
	}

	public InputStream getInputStream() {
		return backUpIn;
	}

	public void setGifImage(byte[] data) {
		gifData = data;
		openInputstream();
		fileType = 3;
	}

	private void openInputstream() {
		in = new ByteArrayInputStream(gifData);
	}

	public void setGifImage(Resources res, int resId) {
		this.res = res;
		this.resId = resId;
		openResourceFile();
		fileType = 1;
	}

	private void openResourceFile() {
		in = res.openRawResource(resId);
	}

	public void setGifImage(String strFileName) {
		this.strFileName = strFileName;
		fileType = 2;
		openFile();
	}

	private void openFile() {
		try {
			in = new FileInputStream(strFileName);
		} catch (Exception ex) {
			Log.e("open failed", ex.toString());
		}
	}

	public void run() {
		try {
			readStream();
		} catch (Exception ex) {
			Log.e("GifView decode run", ex.toString());
			ex.printStackTrace();
		}
	}

	/**
	 * 释放资源
	 */
	private void free() {
		if (in != null) {
			try {
				in.close();
			} catch (Exception ex) {
			}
			in = null;
		}
		gifData = null;
		status = 0;
		if (frameCache != null) {
			frameCache.clear();
			frameCache = null;
		}
		if (frameQueue != null) {
			frameQueue.clear();
			frameQueue = null;
		}
	}

	public void destroy() {
			isDestroy = true;
			free();
			action = null;
			
	}

	/**
	 * 当前状�?
	 * 
	 * @return
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * 取�?�?�?
	 * 
	 * @return 图片的�?帧数
	 */
	public int getFrameCount() {
		if (loopParse == false && status != STATUS_FINISH)
			return -1;
		else
			return frameCount;
	}

	/**
	 * 取第�?��图片
	 * 
	 * @return
	 */
	public Bitmap getImage() {
		return getFrameImage();
	}

	public int getLoopCount() {
		return loopCount;
	}

	private int[] dest = null;

	private void setPixels() {
		try {
			// int[] dest = new int[width * height];
			if (dest == null) {
				dest = new int[width * height];
			}

			// fill in starting image contents based on last image's dispose
			// code

			if (lastDispose > 0) {
				if (lastDispose == 3) {
					// use image before last
					int n = frameCount - 2;
					if (n > 0) {
						// lastImage = getFrameImage(n - 1);
					} else {
						lastImage = null;
					}
					lastImage = null;
				}
				if (lastImage != null) {
					lastImage.getPixels(dest, 0, width, 0, 0, width, height);
					// copy pixels
					if (lastDispose == 2) {
						// fill last image rect area with background color
						int c = 0;
						if (!transparency) {
							c = lastBgColor;
						}
						for (int i = 0; i < lrh; i++) {
							int n1 = (lry + i) * width + lrx;
							int n2 = n1 + lrw;
							for (int k = n1; k < n2; k++) {
								dest[k] = c;
							}
						}
					}
				}
			}

			// copy each source line to the appropriate place in the destination
			int pass = 1;
			int inc = 8;
			int iline = 0;
			for (int i = 0; i < ih; i++) {
				int line = i;
				if (interlace) {
					if (iline >= ih) {
						pass++;
						switch (pass) {
						case 2:
							iline = 4;
							break;
						case 3:
							iline = 2;
							inc = 4;
							break;
						case 4:
							iline = 1;
							inc = 2;
						}
					}
					line = iline;
					iline += inc;
				}
				line += iy;
				if (line < height) {
					int k = line * width;
					int dx = k + ix; // start of line in dest
					int dlim = dx + iw; // end of dest line
					if ((k + width) < dlim) {
						dlim = k + width; // past dest edge
					}
					int sx = i * iw; // start of line in source
					while (dx < dlim) {
						// map color and insert in destination
						int index = ((int) pixels[sx++]) & 0xff;
						int c = act[index];
						if (c != 0) {
							dest[dx] = c;
						}
						dx++;
					}
				}
			}

			image = Bitmap.createBitmap(dest, width, height, Config.ARGB_8888);  //如果你的gif没有透明，且想节省资源，这里可以设置为Config.RGB_565
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (StackOverflowError ee) {
			ee.printStackTrace();
		} catch (Exception ex) {
			Log.e("GifView decode setpixel", ex.toString());
		}
	}

	/**
	 * 取第几帧的图�?
	 * 
	 * @param n
	 *            帧数
	 * @return 可画的图片，如果没有此帧或�?出错，返回null
	 */
	public Bitmap getFrameImage() {
		GifFrame frame = getCurrentFrame();
		if (frame == null)
			return null;
		else
			return frame.image;
	}

	/**
	 * 取当前帧图片
	 * 
	 * @hide
	 * @return 当前帧可画的图片
	 */
	public GifFrame getCurrentFrame() {
		GifFrame gif = null;
		if (loopCache && frameQueue.size() == 0) {
			if (iCurrentFrame >= frameCount) {
				iCurrentFrame = 0;
				action.loopEnd();
			}
			gif = frameCache.get(iCurrentFrame);
			iCurrentFrame++;

		} else {
			try {
				lock.lockInterruptibly();
				try {
					while (loopCache == false && frameQueue.size() == 0) {
						rCondition.await();
					}
					gif = frameQueue.poll();
					wCondition.signal();
					iCurrentFrame++;
					if (loopParse && iCurrentFrame >= frameCount) {
						action.loopEnd();
						iCurrentFrame = 0;
					}
				} catch (Exception ex) {
					rCondition.signal();
					return null;
				}
			} catch (Exception ex) {
				return null;
			} finally {
				lock.unlock();
			}
		}
		return gif;
	}

	/**
	 * 下一帧，进行本操作后，�?过getCurrentFrame得到的是下一�?
	 * 
	 * @hide
	 * @return 返回下一�?
	 */
	public GifFrame next() {
		return getCurrentFrame();
	}

	private int readStream() {
		init();
		if (in != null) {
			readHeader();
			if (!err()) {
				readContents();
				if (loopParse == false) {
					if (frameCount < 0) {
						status = STATUS_FORMAT_ERROR;
						if(action != null)
						action.parseReturn(GifAction.RETURN_ERROR);
					} else {
//						status = STATUS_FINISH;
//						if(action != null)
//						action.parseReturn(GifAction.RETURN_FINISH);
					}
				}
			}
			try {
				if (null != in)
					in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			in = null;
			checkLoop();
		} else {
			status = STATUS_OPEN_ERROR;
			if(action != null)
			action.parseReturn(GifAction.RETURN_ERROR);
		}
		return status;
	}

	private void checkLoop() {
		if (isLoop) {
			if (loopCache) {
				return;
			}
			
			if (frameCount <= MAX_QUEUE) {
				// frameCache = new ArrayList<GifFrame>(frameCount);
				try {
					lock.lockInterruptibly();
					loopCache = true;
					status = STATUS_FINISH;
					if(action != null)
					action.parseReturn(GifAction.RETURN_FINISH);
					rCondition.signal();
				} catch (Exception ex) {

				} finally {
					lock.unlock();
				}
			} else {
				if(frameCache != null)
				frameCache.clear();
			}
			switch (fileType) {
			case 1: // resource
				openResourceFile();
				break;
			case 2:
				openFile();
				break;
			case 3:
				openInputstream();
				break;
			}
			loopParse = true;
			if(isDestroy == false)
				readStream();
		}
	}

	private void decodeImageData() {
		int NullCode = -1;
		int npix = iw * ih;
		int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits, code, count, i, datum, data_size, first, top, bi, pi;

		if ((pixels == null) || (pixels.length < npix)) {
			pixels = new byte[npix]; // allocate new pixel array
		}
		if (prefix == null) {
			prefix = new short[MaxStackSize];
		}
		if (suffix == null) {
			suffix = new byte[MaxStackSize];
		}
		if (pixelStack == null) {
			pixelStack = new byte[MaxStackSize + 1];
		}
		// Initialize GIF data stream decoder.
		data_size = read();
		clear = 1 << data_size;
		end_of_information = clear + 1;
		available = clear + 2;
		old_code = NullCode;
		code_size = data_size + 1;
		code_mask = (1 << code_size) - 1;
		for (code = 0; code < clear; code++) {
			prefix[code] = 0;
			suffix[code] = (byte) code;
		}

		// Decode GIF pixel stream.
		datum = bits = count = first = top = pi = bi = 0;
		for (i = 0; i < npix;) {
			if (top == 0) {
				if (bits < code_size) {
					// Load bytes until there are enough bits for a code.
					if (count == 0) {
						// Read a new data block.
						count = readBlock();
						if (count <= 0) {
							break;
						}
						bi = 0;
					}
					datum += (((int) block[bi]) & 0xff) << bits;
					bits += 8;
					bi++;
					count--;
					continue;
				}
				// Get the next code.
				code = datum & code_mask;
				datum >>= code_size;
				bits -= code_size;

				// Interpret the code
				if ((code > available) || (code == end_of_information)) {
					break;
				}
				if (code == clear) {
					// Reset decoder.
					code_size = data_size + 1;
					code_mask = (1 << code_size) - 1;
					available = clear + 2;
					old_code = NullCode;
					continue;
				}
				if (old_code == NullCode) {
					pixelStack[top++] = suffix[code];
					old_code = code;
					first = code;
					continue;
				}
				in_code = code;
				if (code == available) {
					pixelStack[top++] = (byte) first;
					code = old_code;
				}
				while (code > clear) {
					pixelStack[top++] = suffix[code];
					code = prefix[code];
				}
				first = ((int) suffix[code]) & 0xff;
				// Add a new string to the string table,
				if (available >= MaxStackSize) {
					break;
				}
				pixelStack[top++] = (byte) first;
				prefix[available] = (short) old_code;
				suffix[available] = (byte) first;
				available++;
				if (((available & code_mask) == 0) && (available < MaxStackSize)) {
					code_size++;
					code_mask += available;
				}
				old_code = in_code;
			}

			// Pop a pixel off the pixel stack.
			top--;
			pixels[pi++] = pixelStack[top];
			i++;
		}
		for (i = pi; i < npix; i++) {
			pixels[i] = 0; // clear missing pixels
		}
	}

	private boolean err() {
		return status != STATUS_PARSING;
	}

	private void init() {
		status = STATUS_PARSING;
		if (loopParse == false)
			frameCount = 0;
		gct = null;
		lct = null;
		icacheParse = 0;
	}

	private int read() {
		int curByte = 0;
		try {
			curByte = in.read();
		} catch (Exception e) {
			status = STATUS_FORMAT_ERROR;
		}
		return curByte;
	}

	private int readBlock() {
		blockSize = read();
		int n = 0;
		if (blockSize > 0) {
			try {
				int count = 0;
				while (n < blockSize) {
					count = in.read(block, n, blockSize - n);
					if (count == -1) {
						break;
					}
					n += count;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (n < blockSize) {
				status = STATUS_FORMAT_ERROR;
			}
		}
		return n;
	}

	private int[] tab = new int[256];

	private int[] readColorTable(int ncolors) {
		int nbytes = 3 * ncolors;
		// int[] tab = null;
		byte[] c = new byte[nbytes];
		int n = 0;
		try {
			n = in.read(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (n < nbytes) {
			status = STATUS_FORMAT_ERROR;
		} else {
			// tab = new int[256]; // max size to avoid bounds checks
			int i = 0;
			int j = 0;
			while (i < ncolors) {
				int r = ((int) c[j++]) & 0xff;
				int g = ((int) c[j++]) & 0xff;
				int b = ((int) c[j++]) & 0xff;
				tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
			}
		}
		return tab;
	}

	private void readContents() {
		// read GIF file content blocks
		boolean done = false;
		while (!(done || err()) && isDestroy == false) {
				int code = read();
				switch (code) {
				case 0x2C: // image separator
					readImage();
					break;
				case 0x21: // extension
					code = read();
					switch (code) {
					case 0xf9: // graphics control extension
						readGraphicControlExt();
						break;
					case 0xff: // application extension
						readBlock();
						String app = "";
						for (int i = 0; i < 11; i++) {
							app += (char) block[i];
						}
						if (app.equals("NETSCAPE2.0")) {
							readNetscapeExt();
						} else {
							skip(); // don't care
						}
						break;
					default: // uninteresting extension
						skip();
					}
					break;
				case 0x3b: // terminator
					done = true;
					break;
				case 0x00: // bad byte, but keep going and see what happens
					break;
				default:
					status = STATUS_FORMAT_ERROR;
				}
		}
	}

	private void readGraphicControlExt() {
		read(); // block size
		int packed = read(); // packed fields
		dispose = (packed & 0x1c) >> 2; // disposal method
		if (dispose == 0) {
			dispose = 1; // elect to keep old image if discretionary
		}
		transparency = (packed & 1) != 0;
		delay = readShort() * 10; // delay in milliseconds
		if(delay == 0){
			delay = 100;
		}
		transIndex = read(); // transparent color index
		read(); // block terminator
	}

	private void readHeader() {
		String id = "";
		for (int i = 0; i < 6; i++) {
			id += (char) read();
		}
		if (!id.startsWith("GIF")) {
			status = STATUS_FORMAT_ERROR;
			return;
		}
		readLSD();
		if (gctFlag && !err()) {
			gct = readColorTable(gctSize);
			bgColor = gct[bgIndex];
		}
	}

	private void readImage() {
		ix = readShort(); // (sub)image position & size
		iy = readShort();
		iw = readShort();
		ih = readShort();
		int packed = read();
		lctFlag = (packed & 0x80) != 0; // 1 - local color table flag
		interlace = (packed & 0x40) != 0; // 2 - interlace flag
		// 3 - sort flag
		// 4-5 - reserved
		lctSize = 2 << (packed & 7); // 6-8 - local color table size
		if (lctFlag) {
			lct = readColorTable(lctSize); // read table
			act = lct; // make local table active
		} else {
			act = gct; // make global table active
			if (bgIndex == transIndex) {
				bgColor = 0;
			}
		}
		int save = 0;
		if (transparency) {
			if (act != null && act.length > 0 && act.length > transIndex) {
				save = act[transIndex];
				act[transIndex] = 0; // set transparent color if specified
			}
		}
		if (act == null) {
			status = STATUS_FORMAT_ERROR; // no color table defined
		}
		if (err()) {
			return;
		}
		decodeImageData(); // decode pixel data
		skip();
		if (err()) {
			return;
		}
		if (loopParse == false)
			frameCount++;
		// create new image to receive frame data
		//image = Bitmap.createBitmap(width, height, Config.ARGB_4444);
		// createImage(width, height);
		setPixels(); // transfer pixel data to image

		try {

			lock.lockInterruptibly();
			try {

				while (frameQueue != null && frameQueue.size() >= MAX_QUEUE) {
					wCondition.await();
				}
				if(frameQueue != null){
				GifFrame gif = new GifFrame(image, delay);
				frameQueue.add(gif);
				if (loopParse == false) {
					frameCache.add(gif);
				}
				rCondition.signal();
				if (loopParse == false && icacheParse >= 0) {
					icacheParse++;
					if (icacheParse >= MAX_QUEUE) {
						// 只在第一轮解码，且缓存已经满时，才发这个事件
						action.parseReturn(GifAction.RETURN_CACHE_FINISH);
						icacheParse = -1;
					} else if (icacheParse == 1) {
						// 第一贴成功解码事�?
						action.parseReturn(GifAction.RETURN_FIRST);
					}
				}
				}
			} catch (InterruptedException ie) {
				wCondition.signal();
			} finally {
				lock.unlock();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (transparency) {
			act[transIndex] = save;
		}
		resetFrame();

	}

	private void readLSD() {
		// logical screen size
		width = readShort();
		height = readShort();
		// packed fields
		int packed = read();
		gctFlag = (packed & 0x80) != 0; // 1 : global color table flag
		// 2-4 : color resolution
		// 5 : gct sort flag
		gctSize = 2 << (packed & 7); // 6-8 : gct size
		bgIndex = read(); // background color index
		pixelAspect = read(); // pixel aspect ratio
	}

	private void readNetscapeExt() {
		do {
			readBlock();
			if (block[0] == 1) {
				// loop count sub-block
				int b1 = ((int) block[1]) & 0xff;
				int b2 = ((int) block[2]) & 0xff;
				loopCount = (b2 << 8) | b1;
			}
		} while ((blockSize > 0) && !err());
	}

	private int readShort() {
		// read 16-bit value, LSB first
		int s = read();
		int f = read();
		int t = s | (f << 8);
		return t;
		//return read() | (read() << 8);
	}

	private void resetFrame() {
		lastDispose = dispose;
		lrx = ix;
		lry = iy;
		lrw = iw;
		lrh = ih;
		lastImage = image;
		lastBgColor = bgColor;
		dispose = 0;
		transparency = false;
		delay = 0;
		lct = null;
	}

	/**
	 * Skips variable length blocks up to and including next zero length block.
	 */
	private void skip() {
		do {
			readBlock();
		} while ((blockSize > 0) && !err());
	}
}
