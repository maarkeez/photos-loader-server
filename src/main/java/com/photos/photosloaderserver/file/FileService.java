package com.photos.photosloaderserver.file;

import com.photos.photosloaderserver.model.PhotoItem;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.Resource;

public interface FileService {
  
  Resource toResource ( Path photoPath );
  
  List<PhotoItem> files ();
  
  Path thumbnailPath ( String relativePath );
  
  Path photoPath ( String relativePath );
}
