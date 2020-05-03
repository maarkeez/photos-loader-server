package com.photos.photosloaderserver.file;

import java.nio.file.Path;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PhotosProperties {
  
  @Getter
  @Value( "#{ T(java.nio.file.Paths).get('${photos.path}')}" )
  private Path photosPath;
  
  @Getter
  @Value( "#{ T(java.nio.file.Paths).get('${thumbnails.path}')}" )
  private Path thumbnailsPath;
}
