package com.photos.photosloaderserver.api;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

@Service
public class ContentTypeService {
  
  private final Tika tika = new Tika();
  
  public String detect ( Path photoPath ) throws IOException {
    return tika.detect(photoPath);
  }
}
