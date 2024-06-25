/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.hgnested;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.util.Util;

import java.util.HashMap;
import java.util.Map;

public class HgNestedConfiguration {

  public static final String PROPERTY_HGNESTED = "hgnested.repositories";

  private Map<String, HgNestedRepository> nestedRepositoryMap;

  private static final Logger logger =
    LoggerFactory.getLogger(HgNestedConfiguration.class);

  public HgNestedConfiguration(Repository repository) {
    String value = repository.getProperty(PROPERTY_HGNESTED);

    if (Util.isNotEmpty(value)) {
      parseNestedRepositories(value);
    } else if (logger.isDebugEnabled()) {
      logger.debug("no nested repositories are defined for repository {}",
        repository.getName());
    }
  }

  public HgNestedRepository getNestedRepository(String path) {
    return nestedRepositoryMap.get(path);
  }

  public boolean isNestedRepositoryConfigured() {
    return Util.isNotEmpty(nestedRepositoryMap);
  }

  private void parseNestedRepositories(String value) {
    String[] values = value.split(";");

    for (String v : values) {
      parseNestedRepository(v);
    }
  }

  private void parseNestedRepository(String value) {
    value = value.trim();

    String[] values = value.split("=");

    if (values.length != 2) {
      if (logger.isWarnEnabled()) {
        logger.warn("wrong length detected");
      }
    } else {
      if (nestedRepositoryMap == null) {
        nestedRepositoryMap = new HashMap<>();
      }

      String name = values[0].trim();
      String url = values[1].trim();
      HgNestedRepository repo = new HgNestedRepository(name, url);

      if (logger.isDebugEnabled()) {
        logger.debug("append nested repository: {}", repo);
      }

      nestedRepositoryMap.put(name, repo);
    }
  }
}
