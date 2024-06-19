/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.hgnested;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryRequestListener;
import sonia.scm.util.HttpUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class HgNestedRepositoryRequestListener
        implements RepositoryRequestListener, HgNested
{

  /** the logger for HgNestedRepositoryRequestListener */
  private static final Logger logger =
    LoggerFactory.getLogger(HgNestedRepositoryRequestListener.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param repository
   *
   *
   * @return
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public boolean handleRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               Repository repository)
          throws IOException, RepositoryException
  {
    boolean process = true;
    String uri = request.getRequestURI();

    if (TYPE.equals(repository.getType()))
    {
      String repoPath = getRepositoryPath(repository);

      if (!uri.endsWith(repoPath))
      {
        if (logger.isTraceEnabled())
        {
          logger.trace("check repository {} for nested request at uri {}",
                       repository.getName(), uri);
        }

        HgNestedConfiguration config = new HgNestedConfiguration(repository);

        if (config.isNestedRepositoryConfigured())
        {
          String module = uri.substring(uri.indexOf(repoPath)
                                        + repoPath.length());

          module = HttpUtil.getUriWithoutStartSeperator(module);
          module = HttpUtil.getUriWithoutEndSeperator(module);

          HgNestedRepository r = config.getNestedRepository(module);

          if (r != null)
          {
            String url = HgNestedUtil.createUrl(request, r);

            if (logger.isDebugEnabled())
            {
              logger.debug("send redirect to {}", url);
            }

            response.sendRedirect(url);
            process = false;
          }
          else if (logger.isDebugEnabled())
          {
            logger.debug("no nested repository configured for module {} at {}",
                        module, repository.getName());
          }
        }
        else if (logger.isDebugEnabled())
        {
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
   *
   * @param repository
   *
   * @return
   */
  private String getRepositoryPath(Repository repository)
  {
    return new StringBuilder(HttpUtil.SEPARATOR_PATH).append(
        repository.getType()).append(HttpUtil.SEPARATOR_PATH).append(
        repository.getName()).toString();
  }
}
