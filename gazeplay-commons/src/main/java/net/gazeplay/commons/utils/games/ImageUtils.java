package net.gazeplay.commons.utils.games;

import com.google.common.collect.Sets;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

@Slf4j
public class ImageUtils {

    private static final Set<String> supportedFilesExtensions = Collections
            .unmodifiableSet(Sets.newHashSet("jpg", "jpeg", "png", "gif", "bmp", "wbmp"));

    public static ImageLibrary createImageLibrary(File directoryFile) {
        return new LazyImageLibrary(directoryFile, createDefaultImageLibrary(null));
    }

    public static ImageLibrary createImageLibrary(File directoryFile, File defaultDirectoryFile) {
        return new LazyImageLibrary(directoryFile,
                createDefaultImageLibrary(new LazyImageLibrary(defaultDirectoryFile)));
    }

    public static ImageLibrary createDefaultImageLibrary(ImageLibrary fallbackImageLibrary) {
        File defaultImageDirectory = ImageDirectoryLocator
                .locateImagesDirectoryInUnpackedDistDirectory("data/common/default/images/");

        if (defaultImageDirectory == null) {
            defaultImageDirectory = ImageDirectoryLocator
                    .locateImagesDirectoryInExplodedClassPath("data/common/default/images/");
        }

        return new LazyImageLibrary(defaultImageDirectory, fallbackImageLibrary);
    }

    public static List<Image> loadAllImages(File directoryFile) {
        List<File> files = listImageFiles(directoryFile);

        return loadAllAsImages(files);
    }

    public static List<File> listImageFiles(File directoryFile) {
        log.info("Listing images in directory {}", directoryFile.getAbsolutePath());
        File[] files = directoryFile.listFiles(ImageUtils::isImage);
        if (files == null) {
            files = new File[0];
        }
        List<File> result = Arrays.asList(files);
        log.info("Found {} files in directory {}", files.length, directoryFile.getAbsolutePath());
        return result;
    }

    public static List<Image> loadAllAsImages(List<File> imageFiles) {
        List<Image> result = new ArrayList<>();
        for (File currentFile : imageFiles) {
            result.add(loadImage(currentFile));
        }
        return result;
    }

    private static Image loadImage(File file) {
        return new Image(file.toURI().toString());
    }

    private static boolean isImage(File file) {
        if (file.isDirectory()) {
            return false;
        }
        if (!file.isFile()) {
            return false;
        }
        if (file.isHidden()) {
            return false;
        }

        final String filename = file.getName();
        if (filename.startsWith(".")) {
            // Problems with filenames starting with a dot on Windows
            return false;
        }
        final String extension = filename.substring(filename.lastIndexOf('.') + 1);
        final String extensionAsLowercase = extension.toLowerCase(Locale.getDefault());
        return supportedFilesExtensions.contains(extensionAsLowercase);
    }

}
