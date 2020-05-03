package com.photos.photosloaderserver.file;

import static java.nio.file.Files.readAttributes;
import static java.nio.file.Files.walk;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import com.photos.photosloaderserver.model.PhotoItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor( onConstructor = @__( @Autowired ) )
public class FileServiceImpl implements FileService {
  
  private final List<PhotoItem> filesOrderedByCreationDate = new ArrayList<>();
  
  private final PhotosProperties photosProperties;
  
  @PostConstruct
  @SneakyThrows
  public void init () {
    
    val rootPath = photosProperties.getPhotosPath();
    log.info("Loading items from: {}", rootPath);
    
    try (Stream<Path> paths = walk(rootPath, 10)) {
      filesOrderedByCreationDate.addAll(paths.filter(Files::isRegularFile)
                                             .map(this::toFileWrapper)
                                             .sorted(comparing(FileWrapper::getCreationDate))
                                             .map(this::toPhotoItem)
                                             .collect(toList()));
    }
    log.info("Items loaded: {}", filesOrderedByCreationDate.size());
  }
  
  private PhotoItem toPhotoItem ( FileWrapper fileWrapper ) {
    
    val creationDate = fileWrapper.getCreationDate();
    
    val pathStr = photosProperties.getPhotosPath()
                                  .relativize(fileWrapper.getPath())
                                  .toString();
    
    return PhotoItem.builder()
                    .creationDate(creationDate)
                    .path(pathStr)
                    .build();
  }
  
  @Override
  public Resource toResource ( Path photoPath ) {
    return new FileSystemResource(photoPath.toFile());
  }
  
  @Override
  public List<PhotoItem> files () {
    return filesOrderedByCreationDate;
  }
  
  @Override
  public Path thumbnailPath ( String relativePath ) {
    return photosProperties.getThumbnailsPath()
                           .resolve(relativePath);
  }
  
  @Override
  public Path photoPath ( String relativePath ) {
    return photosProperties.getThumbnailsPath()
                           .resolve(relativePath);
  }
  
  
  @SneakyThrows
  private FileWrapper toFileWrapper ( Path path ) {
    return FileWrapper.builder()
                      .path(path)
                      .creationDate(collectFileCreationDate(path))
                      .build();
  }
  
  
  private long collectFileCreationDate ( Path path ) throws IOException {
    return readAttributes(path, BasicFileAttributes.class).creationTime()
                                                          .toInstant()
                                                          .toEpochMilli();
  }
  
}

