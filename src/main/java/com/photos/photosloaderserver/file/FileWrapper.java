package com.photos.photosloaderserver.file;

import java.nio.file.Path;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder( toBuilder = true )
public class FileWrapper {
  
  Path path;
  long creationDate;
}
