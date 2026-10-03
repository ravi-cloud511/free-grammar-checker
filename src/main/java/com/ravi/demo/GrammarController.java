package com.ravi.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class GrammarController {

  private static final String DOCX =
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

  private final GrammarService service;

  public GrammarController(GrammarService service) {
    this.service = service;
  }

  @PostMapping("/check")
  public Map<String, Object> check(@RequestBody Map<String, String> body) {
    return service.check(
        body.getOrDefault("text", ""),
        body.getOrDefault("lang", "en-US"));
  }

  @PostMapping("/upload")
  public ResponseEntity<byte[]> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(defaultValue = "en-US") String lang) throws IOException {

    String name = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();

    if (name.endsWith(".txt")) {
      String text = new String(file.getBytes(), StandardCharsets.UTF_8);
      String fixed = (String) service.check(text, lang).get("corrected");
      return download(fixed.getBytes(StandardCharsets.UTF_8), "corrected-" + name, "text/plain");
    }

    if (name.endsWith(".docx")) {
      try (XWPFDocument in = new XWPFDocument(file.getInputStream())) {
        String text = in.getParagraphs().stream()
            .map(XWPFParagraph::getText)
            .collect(Collectors.joining("\n"));
        String fixed = (String) service.check(text, lang).get("corrected");

        try (XWPFDocument out = new XWPFDocument();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
          for (String line : fixed.split("\n", -1)) {
            out.createParagraph().createRun().setText(line);
          }
          out.write(bos);
          return download(bos.toByteArray(), "corrected-" + name, DOCX);
        }
      }
    }

    return ResponseEntity.badRequest().build();
  }

  private ResponseEntity<byte[]> download(byte[] data, String name, String type) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
        .contentType(MediaType.parseMediaType(type))
        .body(data);
  }
}
