package com.ssk.kiosk.appearance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AppearanceMediaService {
  private static final Set<String> TYPES = Set.of("image/png", "image/jpeg", "image/webp", "image/svg+xml");
  private final AppearanceMediaRepository repository;
  private final Path root;
  private final long maximumBytes;

  public AppearanceMediaService(AppearanceMediaRepository repository,
      @Value("${app.media.directory:./data/uploads}") String directory,
      @Value("${app.media.max-bytes:5242880}") long maximumBytes) throws IOException {
    this.repository = repository;
    this.root = Path.of(directory).toAbsolutePath().normalize();
    this.maximumBytes = maximumBytes;
    Files.createDirectories(root);
  }

  public Map<String, Object> store(MultipartFile file, String actor) throws Exception {
    byte[] bytes = file.getBytes();
    if (bytes.length == 0 || bytes.length > maximumBytes) throw new IllegalArgumentException("Image must be between 1 byte and " + maximumBytes + " bytes");
    String detected = detect(bytes);
    if (!TYPES.contains(detected)) throw new IllegalArgumentException("Unsupported or invalid image content");
    if ("image/svg+xml".equals(detected)) validateSvg(bytes);
    String extension = Map.of("image/png","png","image/jpeg","jpg","image/webp","webp","image/svg+xml","svg").get(detected);
    String storedName = UUID.randomUUID() + "." + extension;
    Path target = root.resolve(storedName).normalize();
    if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid upload path");
    Files.write(target, bytes);
    AppearanceMedia media = new AppearanceMedia();
    media.setStoredName(storedName); media.setContentType(detected); media.setSizeBytes(bytes.length);
    media.setChecksum(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))); media.setCreatedBy(actor);
    repository.save(media);
    return response(media);
  }

  public Path resolve(UUID id) {
    AppearanceMedia media = repository.findById(id).orElseThrow();
    Path path = root.resolve(media.getStoredName()).normalize();
    if (!path.startsWith(root) || !Files.isRegularFile(path)) throw new IllegalArgumentException("Image is unavailable");
    return path;
  }

  public AppearanceMedia metadata(UUID id) { return repository.findById(id).orElseThrow(); }
  private Map<String,Object> response(AppearanceMedia m){return Map.of("id",m.getId(),"url","/uploads/appearance/"+m.getId(),"contentType",m.getContentType(),"size",m.getSizeBytes(),"version",m.getChecksum());}
  private String detect(byte[] b){if(b.length>8&&b[0]==(byte)0x89&&b[1]==0x50&&b[2]==0x4e&&b[3]==0x47)return "image/png";if(b.length>3&&b[0]==(byte)0xff&&b[1]==(byte)0xd8&&b[2]==(byte)0xff)return "image/jpeg";if(b.length>12&&new String(b,0,4,StandardCharsets.US_ASCII).equals("RIFF")&&new String(b,8,4,StandardCharsets.US_ASCII).equals("WEBP"))return "image/webp";String s=new String(b,StandardCharsets.UTF_8).stripLeading().toLowerCase();if(s.startsWith("<svg")||s.startsWith("<?xml")&&s.contains("<svg"))return "image/svg+xml";return "application/octet-stream";}
  private void validateSvg(byte[] b){String s=new String(b,StandardCharsets.UTF_8).toLowerCase();for(String forbidden:Set.of("<script","javascript:","onload=","onerror=","<!entity","<foreignobject","<?xml-stylesheet","@import","url(http","href="))if(s.contains(forbidden))throw new IllegalArgumentException("Unsafe SVG content");}
}
