package com.photos.photosloaderserver;

import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.ok;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping( "/api" )
@RequiredArgsConstructor( onConstructor = @__( @Autowired ) )
public class PhotosController {
  
  private final PhotosProperties photosProperties;
  
  @GetMapping( "/photos" )
  @SneakyThrows
  public List<String> findAll () {
    val rootPath = photosProperties.getPhotosPath();
    
    try (Stream<Path> paths = walk(rootPath, 1)) {
      return paths.filter(Files::isRegularFile)
                  .map(Path::getFileName)
                  .map(Path::toString)
                  .collect(toList());
    }
  }
  
  @GetMapping( "/photos/{photoName}" )
  @ResponseBody
  public ResponseEntity<Resource> downloadPhoto ( @PathVariable String photoName ) {
    Resource file = loadAsResource(photoName);
    return ResponseEntity.ok()
                         .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                         .body(file);
  }
  
  
  @PostMapping( "/photos" )
  @SneakyThrows
  public ResponseEntity uploadPhoto ( @RequestParam( "file" ) MultipartFile file ) {
    
    val fileName = file.getOriginalFilename();
    val destinationFilePath = photosProperties.getPhotosPath()
                                              .resolve(fileName);
    file.transferTo(destinationFilePath);
    
    return ok().build();
  }
  
  @SneakyThrows
  private Resource loadAsResource ( String photoName ) {
    val rootPath = photosProperties.getPhotosPath();
    val photoFile = rootPath.resolve(photoName)
                            .toFile();
    return new FileSystemResource(photoFile);
  }
}
