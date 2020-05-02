package com.photos.photosloaderserver;

import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.ResponseEntity.ok;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping( "/api" )
@RequiredArgsConstructor( onConstructor = @__( @Autowired ) )
public class PhotosController {
  
  private final PhotosProperties photosProperties;
  private final Tika tika = new Tika();
  
  @GetMapping( "/photos" )
  @SneakyThrows
  public List<String> findAll () {
    val rootPath = photosProperties.getPhotosPath();
    
    try (Stream<Path> paths = walk(rootPath, 10)) {
      return paths.filter(Files::isRegularFile)
                  .map(rootPath::relativize)
                  .map(Path::toString)
                  .collect(toList());
    }
  }
  
  @GetMapping( "/photos/{photoName}/download" )
  @ResponseBody
  public ResponseEntity<Resource> downloadPhotoByName ( @PathVariable String photoName ) {
    Resource file = loadAsResource(photoName);
    return ok().header(CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
               .body(file);
  }
  
  @GetMapping( value = "/photos/name/**" )
  @SneakyThrows
  public ResponseEntity<Resource> findPhotoByName ( HttpServletRequest request ) {
  
    val requestURL = request.getRequestURL().toString();
    val photoName = requestURL.split("/photos/name/")[1];
    
    val photoPath = loadPhotoPath(photoName);
    val photoResource = toResource(photoPath);
    val contentType = parseContentType(photoPath);
    
    return ok().header(CONTENT_TYPE, contentType)
               .header(CONTENT_DISPOSITION, "filename=\"" + photoPath.getFileName() + "\"")
               .body(photoResource);
  }
  
  
  @PostMapping( "/photos" )
  @SneakyThrows
  public ResponseEntity savePhoto ( @RequestParam( "file" ) MultipartFile file ) {
    
    val fileName = file.getOriginalFilename();
    val destinationFilePath = photosProperties.getPhotosPath()
                                              .resolve(fileName);
    file.transferTo(destinationFilePath);
    
    return ok().build();
  }
  
  @SneakyThrows
  private Resource loadAsResource ( String photoName ) {
    Path photoPath = loadPhotoPath(photoName);
    return toResource(photoPath);
  }
  
  private Resource toResource ( Path photoPath ) {
    return new FileSystemResource(photoPath.toFile());
  }
  
  private Path loadPhotoPath ( String photoName ) {
    val rootPath = photosProperties.getPhotosPath();
    return rootPath.resolve(photoName);
  }
  
  private String parseContentType ( Path photoPath ) throws IOException {
    return tika.detect(photoPath);
  }
}
