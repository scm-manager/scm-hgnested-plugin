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

//~--- non-JDK imports --------------------------------------------------------

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryRequestListener;
import sonia.scm.util.HttpUtil;

import java.io.IOException;

/**
 * @author Sebastian Sdorra
 */
@Extension
public class HgNestedRepositoryRequestListener
  implements RepositoryRequestListener, HgNested {

  /**
   * the logger for HgNestedRepositoryRequestListener
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HgNestedRepositoryRequestListener.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @param request
   * @param response
   * @param repository
   * @return
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public boolean handleRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               Repository repository)
    throws IOException, RepositoryException {
    boolean process = true;
    String uri = request.getRequestURI();

    if (TYPE.equals(repository.getType())) {
      String repoPath = getRepositoryPath(repository);

      if (!uri.endsWith(repoPath)) {
        if (logger.isTraceEnabled()) {
          logger.trace("check repository {} for nested request at uri {}",
            repository.getName(), uri);
        }

        HgNestedConfiguration config = new HgNestedConfiguration(repository);

        if (config.isNestedRepositoryConfigured()) {
          String module = uri.substring(uri.indexOf(repoPath)
            + repoPath.length());

          module = HttpUtil.getUriWithoutStartSeperator(module);
          module = HttpUtil.getUriWithoutEndSeperator(module);

          HgNestedRepository r = config.getNestedRepository(module);

          if (r != null) {
            String url = HgNestedUtil.createUrl(request, r);

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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @param repository
   * @return
   */
  private String getRepositoryPath(Repository repository) {
    return String.valueOf(HttpUtil.SEPARATOR_PATH) +
      repository.getType() + HttpUtil.SEPARATOR_PATH +
      repository.getName();
  }
}
