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

/**
 * Provides classes for compiling MAL specifications.
 *
 * <p>Example compiling a {@code .mal} file into a {@code .mar} file:
 *
 * <pre>{@code
 * var input = new File("/path/to/file.mal");
 * var ast = Parser.parse(input);
 * Analyzer.analyze(ast);
 * var lang = LangConverter.convert(ast);
 * try (var out = Files.newOutputStream(Path.of("/path/to/file.mar"));
 *     var writer = new LangWriter(out)) {
 *   writer.write(lang);
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
package org.mal_lang.lib;
