package com.ravi.demo;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class GrammarService {

  private final RestClient client;

  public GrammarService(@Value("${languagetool.url}") String url) {
    this.client = RestClient.create(url);
  }

  public Map<String, Object> check(String text, String lang) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("text", text);
    form.add("language", lang);

    JsonNode res = client.post()
        .uri("/v2/check")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .body(JsonNode.class);

    List<JsonNode> matches = new ArrayList<>();
    res.get("matches").forEach(matches::add);
    matches.sort(Comparator.comparingInt(m -> m.get("offset").asInt()));

    List<Map<String, Object>> issues = new ArrayList<>();
    for (JsonNode m : matches) {
      int off = m.get("offset").asInt();
      int len = m.get("length").asInt();
      String suggestion = m.get("replacements").size() > 0
          ? m.get("replacements").get(0).get("value").asText()
          : "";
      issues.add(Map.of(
          "message", m.get("message").asText(),
          "original", text.substring(off, off + len),
          "suggestion", suggestion,
          "offset", off));
    }

    StringBuilder sb = new StringBuilder(text);
    int limit = Integer.MAX_VALUE;
    for (int i = matches.size() - 1; i >= 0; i--) {
      JsonNode m = matches.get(i);
      int off = m.get("offset").asInt();
      int len = m.get("length").asInt();
      if (m.get("replacements").size() == 0 || off + len > limit) {
        continue;
      }
      sb.replace(off, off + len, m.get("replacements").get(0).get("value").asText());
      limit = off;
    }

    return Map.of("corrected", sb.toString(), "issues", issues);
  }
}
