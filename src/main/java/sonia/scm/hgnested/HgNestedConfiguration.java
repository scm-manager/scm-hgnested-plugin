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

import sonia.scm.repository.Repository;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgNestedConfiguration
{

  /** Field description */
  public static final String PROPERTY_HGNESTED = "hgnested.repositories";

  /** the logger for HgNestedConfiguration */
  private static final Logger logger =
    LoggerFactory.getLogger(HgNestedConfiguration.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repository
   */
  public HgNestedConfiguration(Repository repository)
  {
    String value = repository.getProperty(PROPERTY_HGNESTED);

    if (Util.isNotEmpty(value))
    {
      parseNestedRepositories(value);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("no nested repositories are defined for repository {}",
                   repository.getName());
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  public HgNestedRepository getNestedRepository(String path)
  {
    return nestedRepositoryMap.get(path);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isNestedRepositoryConfigured()
  {
    return Util.isNotEmpty(nestedRepositoryMap);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param value
   */
  private void parseNestedRepositories(String value)
  {
    String[] values = value.split(";");

    for (String v : values)
    {
      parseNestedRepository(v);
    }
  }

  /**
   * Method description
   *
   *
   * @param value
   */
  private void parseNestedRepository(String value)
  {
    value = value.trim();

    String[] values = value.split("=");

    if (values.length != 2)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("wrong length detected");
      }
    }
    else
    {
      if (nestedRepositoryMap == null)
      {
        nestedRepositoryMap = new HashMap<String, HgNestedRepository>();
      }

      String name = values[0].trim();
      String url = values[1].trim();
      HgNestedRepository repo = new HgNestedRepository(name, url);

      if (logger.isDebugEnabled())
      {
        logger.debug("append nested repository: {}", repo);
      }

      nestedRepositoryMap.put(name, repo);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, HgNestedRepository> nestedRepositoryMap;
}
