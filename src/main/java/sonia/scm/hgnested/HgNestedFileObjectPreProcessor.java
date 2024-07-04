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

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.FileObjectPreProcessor;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SubRepository;

public class HgNestedFileObjectPreProcessor implements FileObjectPreProcessor {

  private static final Logger logger =
    LoggerFactory.getLogger(HgNestedFileObjectPreProcessor.class);

  private final HgNestedConfiguration configuration;
  private final HttpServletRequest request;
  private final Repository repository;

  public HgNestedFileObjectPreProcessor(Repository repository, HgNestedConfiguration configuration,
                                        HttpServletRequest request) {
    this.repository = repository;
    this.configuration = configuration;
    this.request = request;
  }

  @Override
  public void process(FileObject fileObject) {
    if (configuration.isNestedRepositoryConfigured()) {
      if (logger.isTraceEnabled()) {
        logger.trace("check file object {} for nested repository",
          fileObject.getPath());
      }

      SubRepository sub = fileObject.getSubRepository();

      if (sub != null) {
        if (logger.isTraceEnabled()) {
          logger.trace("check sub repository {} for nested repository",
            fileObject.getPath());
        }

        String nestedRepositoryUrl =
          configuration.getNestedRepositoryUrl(fileObject.getPath());

        if (nestedRepositoryUrl != null) {
          String url = HgNestedUtil.createUrl(request, repository, nestedRepositoryUrl);

          if (logger.isDebugEnabled()) {
            logger.debug("set sub repository url to {}", url);
          }

          sub.setRepositoryUrl(url);
        }
      }
    } else if (logger.isTraceEnabled()) {
      logger.trace(
        "skip nested repository check, because no nested repository is configured");
    }
  }
}
