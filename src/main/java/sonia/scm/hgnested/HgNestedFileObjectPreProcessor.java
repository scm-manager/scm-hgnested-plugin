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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.FileObject;
import sonia.scm.repository.FileObjectPreProcessor;
import sonia.scm.repository.SubRepository;

//~--- JDK imports ------------------------------------------------------------

import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgNestedFileObjectPreProcessor implements FileObjectPreProcessor
{

  /** the logger for HgNestedFileObjectPreProcessor */
  private static final Logger logger =
    LoggerFactory.getLogger(HgNestedFileObjectPreProcessor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param request
   */
  public HgNestedFileObjectPreProcessor(HgNestedConfiguration configuration,
          HttpServletRequest request)
  {
    this.configuration = configuration;
    this.request = request;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param fo
   */
  @Override
  public void process(FileObject fo)
  {
    if (configuration.isNestedRepositoryConfigured())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("check file object {} for nested repository",
                     fo.getPath());
      }

      SubRepository sub = fo.getSubRepository();

      if (sub != null)
      {
        if (logger.isTraceEnabled())
        {
          logger.trace("check sub repository {} for nested repository",
                       fo.getPath());
        }

        HgNestedRepository repository =
          configuration.getNestedRepository(fo.getPath());

        if (repository != null)
        {
          String url = HgNestedUtil.createUrl(request, repository);

          if (logger.isDebugEnabled())
          {
            logger.debug("set sub repsoitory url to {}", url);
          }

          sub.setRepositoryUrl(url);
        }
      }
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace(
          "skip nested repository check, because no nested repository is configured");
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgNestedConfiguration configuration;

  /** Field description */
  private HttpServletRequest request;
}
