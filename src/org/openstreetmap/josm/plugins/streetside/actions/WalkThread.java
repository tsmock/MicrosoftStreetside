// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.actions;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.plugins.streetside.StreetsideData;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.cache.CacheUtils;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.tools.Logging;

/**
 * Thread containing the walk process.
 *
 * @author nokutu
 */
public class WalkThread extends Thread implements StreetsideDataListener {
    private static final Logger LOGGER = Logger.getLogger(WalkThread.class.getCanonicalName());
    private final int interval;
    private final StreetsideData data;
    private final boolean waitForFullQuality;
    private final boolean followSelected;
    private final boolean goForward;
    private boolean end;
    private BufferedImage lastImage;
    private volatile boolean paused;

    /**
     * Main constructor.
     *
     * @param interval     How often the images switch.
     * @param waitForPicture If it must wait for the full resolution picture or just the
     *             thumbnail.
     * @param followSelected Zoom to each image that is selected.
     * @param goForward    true to go forward; false to go backwards.
     */
    public WalkThread(int interval, boolean waitForPicture, boolean followSelected, boolean goForward) {
        this.interval = interval;
        waitForFullQuality = waitForPicture;
        this.followSelected = followSelected;
        this.goForward = goForward;
        data = StreetsideLayer.getInstance().getData();
        data.addListener(this);
    }

    /**
     * Downloads n images into the cache beginning from the supplied start-image (including the start-image itself).
     *
     * @param startImage the image to start with (this and the next n-1 images in the same sequence are downloaded)
     * @param n      the number of images to download
     * @param type     the quality of the image (full or thumbnail)
     */
    private void preDownloadImages(StreetsideImage startImage, int n, CacheUtils.PICTURE type) {
        if (n >= 1 && startImage != null) {
            CacheUtils.downloadPicture(startImage, type);
            if (this.data.next(startImage) != null && n >= 2) {
                preDownloadImages(this.data.next(startImage), n - 1, type);
            }
        }
    }

    @Override
    public void run() {
        try {
            while (!end && this.data.next(this.data.getSelectedImage()) != null) {
                StreetsideImage image = data.getSelectedImage();
                if (image != null && this.data.next(image) != null) {
                    // Predownload next 10 thumbnails.
                    preDownloadImages(this.data.next(image), 10, CacheUtils.PICTURE.THUMBNAIL);
                    if (Boolean.TRUE.equals(StreetsideProperties.PREDOWNLOAD_CUBEMAPS.get())) {
                        preDownloadCubemaps(this.data.next(image), 10);
                    }
                    if (waitForFullQuality) {
                        // Start downloading 3 next full images.
                        StreetsideImage currentImage = this.data.next(image);
                        preDownloadImages(currentImage, 3, CacheUtils.PICTURE.FULL_IMAGE);
                    }
                }
                try {
                    // Waits for full quality picture.
                    final BufferedImage displayImage = StreetsideMainDialog.getInstance().getStreetsideImageDisplay()
                            .getImage();
                    if (waitForFullQuality && image != null) {
                        while (displayImage == lastImage || displayImage == null || displayImage.getWidth() < 2048) {
                            Thread.sleep(100);
                        }
                    } else { // Waits for thumbnail.
                        while (displayImage == lastImage || displayImage == null || displayImage.getWidth() < 320) {
                            Thread.sleep(100);
                        }
                    }
                    while (paused) {
                        Thread.sleep(100);
                    }
                    wait(interval);
                    while (paused) {
                        Thread.sleep(100);
                    }
                    lastImage = StreetsideMainDialog.getInstance().getStreetsideImageDisplay().getImage();
                    if (goForward) {
                        data.selectNext(followSelected);
                    } else {
                        data.selectPrevious(followSelected);
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        } catch (NullPointerException e) {
            if (Boolean.TRUE.equals(StreetsideProperties.DEBUGING_ENABLED.get())) {
                LOGGER.log(Logging.LEVEL_DEBUG,
                        MessageFormat.format("Exception thrown in WalkThread: {0}", e.getMessage()), e);
            }
            return;
        }
        end();
    }

    private void preDownloadCubemaps(StreetsideImage startImage, int n) {
        if (n >= 1 && startImage != null) {

            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 4; j++) {
                    for (int k = 0; k < 4; k++) {

                        CacheUtils.downloadPicture(startImage, CacheUtils.PICTURE.CUBEMAP);
                        if (this.data.next(startImage) != null && n >= 2) {
                            preDownloadCubemaps(this.data.next(startImage), n - 1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void imagesAdded() {
        // Nothing
    }

    @Override
    public void selectedImageChanged(StreetsideImage oldImage, StreetsideImage newImage) {
        if (newImage != this.data.next(oldImage)) {
            end();
            interrupt();
        }
    }

    /**
     * Continues with the execution if paused.
     */
    public void play() {
        paused = false;
    }

    /**
     * Pauses the execution.
     */
    public void pause() {
        paused = true;
    }

    /**
     * Stops the execution.
     */
    public void stopWalk() {
        if (SwingUtilities.isEventDispatchThread()) {
            end();
            interrupt();
        } else {
            SwingUtilities.invokeLater(this::stopWalk);
        }
    }

    /**
     * Called when the walk stops by itself of forcefully.
     */
    public void end() {
        if (SwingUtilities.isEventDispatchThread()) {
            end = true;
            data.removeListener(this);
            StreetsideMainDialog.getInstance().setMode(StreetsideMainDialog.MODE.NORMAL);
        } else {
            SwingUtilities.invokeLater(this::end);
        }
    }
}
