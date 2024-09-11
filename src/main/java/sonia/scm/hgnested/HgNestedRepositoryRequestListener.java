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

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryRequestListener;
import sonia.scm.util.HttpUtil;

import java.io.IOException;

@Extension
public class HgNestedRepositoryRequestListener
  implements RepositoryRequestListener, HgNested {

  private static final Logger logger =
    LoggerFactory.getLogger(HgNestedRepositoryRequestListener.class);

  private final HgNestedConfigurationStore hgNestedConfigurationStore;

  @Inject
  public HgNestedRepositoryRequestListener(HgNestedConfigurationStore hgNestedConfigurationStore) {
    this.hgNestedConfigurationStore = hgNestedConfigurationStore;
  }

  @Override
  public boolean handleRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               Repository repository)
    throws IOException {
    boolean process = true;
    String uri = request.getRequestURI();

    if (TYPE.equals(repository.getType())) {
      String repoPath = getRepositoryPath(repository);

      if (!uri.endsWith(repoPath)) {
        if (logger.isTraceEnabled()) {
          logger.trace("check repository {} for nested request at uri {}",
            repository.getName(), uri);
        }

        HgNestedConfiguration config = hgNestedConfigurationStore.loadConfiguration(repository);

        if (config.isNestedRepositoryConfigured()) {
          String module = uri.substring(uri.indexOf(repoPath)
            + repoPath.length());

          module = HttpUtil.getUriWithoutStartSeperator(module);
          module = HttpUtil.getUriWithoutEndSeperator(module);

          String nestedRepositoryUrl = config.getNestedRepositoryUrl(module);

          if (nestedRepositoryUrl != null) {
            String url = HgNestedUtil.createUrl(request, repository, nestedRepositoryUrl);

            if (logger.isDebugEnabled()) {
              logger.debug("send redirect to {}", url);
            }

            response.sendRedirect(url);
            process = false;
          } else if (logger.isDebugEnabled()) {
            logger.debug("no nested repository configured for module {} at {}",
              module, repository.getName());
          }
        } else if (logger.isDebugEnabled()) {
          logger.debug("no nested repository defined for {}",
            repository.getName());
        }
      }
    }

    return process;
  }

  private String getRepositoryPath(Repository repository) {
    return HttpUtil.SEPARATOR_PATH +
      repository.getNamespace() + HttpUtil.SEPARATOR_PATH +
      repository.getName();
  }
}
