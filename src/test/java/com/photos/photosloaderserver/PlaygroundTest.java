package com.photos.photosloaderserver;

import static java.nio.file.Files.walk;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
public class PlaygroundTest {
  
//  private Path imageJpgPath = Paths.get("/Users/maarkeez/Documents/GitHub/photos-loader-server/photos/108APPLE/IMG_8440.JPG");
//  private Path imageHeicPath = Paths.get("/Users/maarkeez/Documents/GitHub/photos-loader-server/photos/108APPLE/IMG_8427.HEIC");
//
//  @Test
//  @SneakyThrows
//  public void showImageWithAWT () {
//
//    // Loading image
//    BufferedImage myPicture = ImageIO.read(imageHeicPath.toFile());
//
//    // Editing image
//    // Error (HEIC): java.lang.NullPointerException
//    Graphics2D g = (Graphics2D) myPicture.getGraphics();
//    g.setStroke(new BasicStroke(3));
//    g.setColor(Color.BLUE);
//    g.drawRect(10, 10, myPicture.getWidth() - 20, myPicture.getHeight() - 20);
//
//    // Display image
//    JLabel picLabel = new JLabel(new ImageIcon(myPicture));
//    JPanel jPanel = new JPanel();
//    jPanel.add(picLabel);
//    JFrame f = new JFrame();
//    f.setSize(new Dimension(myPicture.getWidth(), myPicture.getHeight()));
//    f.add(jPanel);
//    f.setVisible(true);
//
//
//    Thread.sleep(60000);
//  }
//
//  @Test
//  @SneakyThrows
//  public void showImageWithImageJ () {
//
//    // Loading image
//    ImagePlus imp = IJ.openImage(imageHeicPath.toAbsolutePath()
//                                              .toString());
//
//    // Editing image
//    // Error(HEIC): Unsupported format or not found
//    ImageProcessor ip = imp.getProcessor();
//    ip.setColor(Color.BLUE);
//    ip.setLineWidth(4);
//    ip.drawRect(10, 10, imp.getWidth() - 20, imp.getHeight() - 20);
//
//    // Display image
//    imp.show();
//
//
//    Thread.sleep(60000);
//  }
//
//
//
//  @Test
//  @SneakyThrows
//  public void showImageWithOpenImaJ () {
//    // Loading image
//    MBFImage image = ImageUtilities.readMBF(imageJpgPath.toFile());
//
//    // Loading image
//    // Not supported. Error: java.io.IOException: org.apache.sanselan.ImageReadException: Can't parse this format.
//    //    MBFImage image = ImageUtilities.readMBF(imageHeicPath.toFile());
//
//    // Editing image
//    Point2d tl = new Point2dImpl(10, 10);
//    Point2d bl = new Point2dImpl(10, image.getHeight() - 10);
//    Point2d br = new Point2dImpl(image.getWidth() - 10, image.getHeight() - 10);
//    Point2d tr = new Point2dImpl(image.getWidth() - 10, 10);
//    Polygon polygon = new Polygon(asList(tl, bl, br, tr));
//    image.drawPolygon(polygon, 4, new Float[]{0f, 0f, 255.0f});
//
//    // Display image
//    DisplayUtilities.display(image);
//    Thread.sleep(60000);
//  }
//
//  @Test
//  @SneakyThrows
//  public void showImageWithJMagick () {
//    System.setProperty("java.library.path", "/Users/maarkeez/Documents/GitHub/photos-loader-server/lib/ImageMagick-7.0.10/lib");
//    // Loading image
//    ImageInfo origInfo = new ImageInfo(imageJpgPath.toAbsolutePath()
//                                                   .toString());
//    MagickImage image = new MagickImage(origInfo);
//
//    // Editing image
//    image = image.scaleImage(250, 250); //to Scale image
//
//    // Display image
//    image.setFileName("/Users/maarkeez/Desktop/" + UUID.randomUUID()
//                                                       .toString());
//    image.writeImage(origInfo);
//
//  }
  
  @Disabled
  @Test
  @SneakyThrows
  public void createThumbnails () {
    // magick IMG_8427.HEIC  -resize 15% output_2.jpg
    Path photosPath = Paths.get("/Users/maarkeez/Documents/GitHub/photos-loader-server/photos");
    Path thumbnailPath = Paths.get("/Users/maarkeez/Documents/GitHub/photos-loader-server/thumbnails");
    
    Set<String> allowedExtensions = new HashSet<>(asList("JPG", "jpg", "HEIC", "PNG", "JPEG"));
    //    Set<String> allowedExtensions = new HashSet<>(asList("HEIC"));
    
    try (Stream<Path> paths = walk(photosPath, 10)) {
      val relativePhotoPaths = paths.filter(Files::isRegularFile)
                                    .map(photosPath::relativize)
                                    .filter(path -> hasAllowedExtension(allowedExtensions, path))
                                    //                       .limit(3)
                                    .map(Path::toString)
                                    .collect(toList());
      resizePhotos(photosPath, thumbnailPath, relativePhotoPaths);
    }
  }
  
  @SneakyThrows
  private void resizePhotos ( Path photosPath, Path thumbnailPath, List<String> relativePhotoPaths ) {
    log.info("Files: {}", relativePhotoPaths);
    AtomicInteger counter = new AtomicInteger(0);
    ForkJoinPool customThreadPool = new ForkJoinPool(4);
    customThreadPool.submit(() -> relativePhotoPaths.parallelStream()
                                                    .forEach(resizePhoto(photosPath, thumbnailPath, relativePhotoPaths.size(), counter))).get();
  }
  
  private Consumer<String> resizePhoto ( Path photosPath, Path thumbnailPath, int totalSize, AtomicInteger counter ) {
    return file -> {
      log.info("Resizing {} of {}", counter.incrementAndGet(), totalSize);
      
      val sourceFile = photosPath.resolve(file)
                                 .toAbsolutePath()
                                 .toString();
      
      val destFilePath = thumbnailPath.resolve(file);
      destFilePath.toFile()
                  .getParentFile()
                  .mkdirs();
      
      val destFile = destFilePath.toAbsolutePath()
                                 .toString();
      
      resizePhoto(sourceFile, destFile);
    };
  }
  
  private boolean hasAllowedExtension ( Set<String> allowedExtensions, Path path ) {
    val fileName = path.getFileName()
                       .toString();
    val extension = fileName.substring(fileName.lastIndexOf(".") + 1);
    return allowedExtensions.contains(extension);
  }
  
  @SneakyThrows
  private void resizePhoto ( String sourceFile, String destFile ) {
    String[] commands = {"bash", "-c",
                         "/Users/maarkeez/Downloads/ImageMagick-7.0.10/bin/magick \"" + sourceFile + "\"  -resize 15% \"" + destFile + "\""};
    
    val p = Runtime.getRuntime()
                   .exec(commands);
    val exitValue = p.waitFor();
    if (exitValue!=0) {
      log.error("Could not execute command:");
      log.error("\t{}", asList(commands));
      
      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      log.error("ERROR:");
      String errorLine;
      while (( errorLine = stdError.readLine() )!=null) {
        log.error("\t{}", errorLine);
      }
      log.info("");
    }
  }
}
