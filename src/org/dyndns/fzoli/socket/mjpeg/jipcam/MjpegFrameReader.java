package org.dyndns.fzoli.socket.mjpeg.jipcam;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Az MjpegFrame osztályból kivett getImage metódus felváltója.
 * Ezáltal az MjpegFrame kódja használható Androidon is és a getImage
 * metódus továbbra is elérhető ezen az osztályon keresztül.
 * @author zoli
 */
public class MjpegFrameReader {

    private MjpegFrameReader() {
    }

    public static Image getImage(MjpegFrame frame) throws IOException {
        InputStream is = new ByteArrayInputStream(frame.getJpegBytes());
        return ImageIO.read(is);
                //RGBFormat rgbf = (RGBFormat)camStream.getFormat();// get the format from the stream
                //			JPEGFormat jpegF = new JPEGFormat();
                //			BufferToImage conv = new BufferToImage(jpegF); // Grab image from webcam
                //			Buffer b = new Buffer();
                //
                //			//camStream.read(b);// Convert to an AWT image
                //			b.setFormat(jpegF);
                //			b.setData(getJpegBytes());
                //
                //			Image i = conv.createImage(b);
                //
                //			return i;
    }

}
