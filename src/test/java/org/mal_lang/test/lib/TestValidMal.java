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
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.json.Json;
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
        assertEquals(jsonLang, lang.toJson());
      }
    } catch (URISyntaxException | IOException | CompilerException e) {
      fail(e);
    }
  }
}
