package com.photos.photosloaderserver.api;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.ResponseEntity.ok;

import com.photos.photosloaderserver.file.FileService;
import com.photos.photosloaderserver.model.PhotoItem;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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
  
  private final FileService fileService;
  private final ContentTypeService contentTypeService;
  
  @GetMapping( "/photos" )
  @SneakyThrows
  public List<PhotoItem> findAll () {
    return fileService.files();
  }
  
  @GetMapping( "/photos/{photoName}/download" )
  @ResponseBody
  public ResponseEntity<Resource> downloadPhotoByName ( @PathVariable String photoName ) {
    Path photoPath = fileService.photoPath(photoName);
    Resource photoResource = fileService.toResource(photoPath);
    return ok().header(CONTENT_DISPOSITION, "attachment; filename=\"" + photoResource.getFilename() + "\"")
               .body(photoResource);
  }
  
  @GetMapping( value = "/photos/name/**" )
  @SneakyThrows
  public ResponseEntity<Resource> findPhotoByName ( HttpServletRequest request ) {
    
    val requestURL = request.getRequestURL()
                            .toString();
    val photoName = requestURL.split("/photos/name/")[1];
    val photoPath = fileService.photoPath(photoName);
    
    return pathToResourceResponse(photoPath);
  }
  
  @GetMapping( value = "/thumbnail/name/**" )
  @SneakyThrows
  public ResponseEntity<Resource> findPhotoThumbnailByName ( HttpServletRequest request ) {
    
    val requestURL = request.getRequestURL()
                            .toString();
    val photoName = requestURL.split("/thumbnail/name/")[1];
    val photoPath = fileService.thumbnailPath(photoName);
    
    return pathToResourceResponse(photoPath);
  }
  
  @PostMapping( "/photos" )
  @SneakyThrows
  public ResponseEntity savePhoto ( @RequestParam( "file" ) MultipartFile file ) {
    
    val fileName = file.getOriginalFilename();
    val destinationFilePath = fileService.photoPath(fileName);
    file.transferTo(destinationFilePath);
    
    return ok().build();
  }
  
  private ResponseEntity<Resource> pathToResourceResponse ( Path photoPath ) throws IOException {
    val photoResource = fileService.toResource(photoPath);
    val contentType = contentTypeService.detect(photoPath);
    
    return ok().header(CONTENT_TYPE, contentType)
               .header(CONTENT_DISPOSITION, "filename=\"" + photoPath.getFileName() + "\"")
               .body(photoResource);
  }
  
}
