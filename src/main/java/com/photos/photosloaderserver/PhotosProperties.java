package com.photos.photosloaderserver;

import java.nio.file.Path;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PhotosProperties {
  
  @Getter
  @Value( "#{ T(java.nio.file.Paths).get('${photos.path}')}" )
  private Path photosPath;
  
}
