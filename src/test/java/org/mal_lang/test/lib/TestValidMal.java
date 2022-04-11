/*
 * Copyright 2019-2022 Foreseeti AB <https://foreseeti.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mal_lang.test.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mal_lang.lib.Analyzer;
import org.mal_lang.lib.CompilerException;
import org.mal_lang.lib.LangConverter;
import org.mal_lang.lib.Parser;

class TestValidMal {
  private static void assertJsonValueEquals(JsonValue expected, JsonValue actual, String jPath) {
    var expectedType = expected.getValueType();
    var actualType = actual.getValueType();
    assertEquals(
        expectedType,
        actualType,
        String.format("Expected type %s, but found %s at '%s'", expectedType, actualType, jPath));
    switch (expectedType) {
      case ARRAY:
        assertJsonArrayEquals(expected.asJsonArray(), actual.asJsonArray(), jPath);
        break;
      case OBJECT:
        assertJsonObjectEquals(expected.asJsonObject(), actual.asJsonObject(), jPath);
        break;
      case NUMBER:
        assertEquals(
            expected,
            actual,
            String.format("Expected %s, but found %s at '%s'", expected, actual, jPath));
        break;
      case STRING:
        assertEquals(
            expected,
            actual,
            String.format("Expected \"%s\", but found \"%s\" at '%s'", expected, actual, jPath));
        break;
      case TRUE:
      case FALSE:
      case NULL:
        break;
    }
  }

  private static void assertJsonArrayEquals(JsonArray expected, JsonArray actual, String jPath) {
    assertEquals(
        expected.size(),
        actual.size(),
        String.format(
            "Expected %d items, but found %d items at '%s'",
            expected.size(), actual.size(), jPath));
    for (int i = 0; i < expected.size(); i++) {
      JsonValue expectedValue = expected.get(i);
      JsonValue actualValue = actual.get(i);
      assertJsonValueEquals(expectedValue, actualValue, String.format("%s[%d]", jPath, i));
    }
  }

  private static void assertJsonObjectEquals(JsonObject expected, JsonObject actual, String jPath) {
    for (String key : expected.keySet()) {
      assertTrue(
          actual.containsKey(key), String.format("Expected '%s' to be a key at '%s'", key, jPath));
    }
    for (String key : actual.keySet()) {
      assertTrue(
          expected.containsKey(key), String.format("Unexpected key '%s' at '%s'", key, jPath));
    }
    for (String key : expected.keySet()) {
      JsonValue expectedValue = expected.get(key);
      JsonValue actualValue = actual.get(key);
      assertJsonValueEquals(expectedValue, actualValue, String.format("%s.%s", jPath, key));
    }
  }

  @Test
  void testTest() {
    try {
      var input = new File(this.getClass().getResource("test.mal").toURI());
      var ast = Parser.parse(input);
      Analyzer.analyze(ast);
      var lang = LangConverter.convert(ast);
      try (var jsonIn = this.getClass().getResourceAsStream("test.json");
          var jsonReader =
              Json.createReaderFactory(null).createReader(jsonIn, StandardCharsets.UTF_8)) {
        var jsonLang = jsonReader.readObject();
        assertJsonObjectEquals(jsonLang, lang.toJson(), "");
      }
    } catch (URISyntaxException | IOException | CompilerException e) {
      fail(e);
    }
  }
}
