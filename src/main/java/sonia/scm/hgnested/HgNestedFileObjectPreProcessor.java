/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
