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

import com.google.inject.Inject;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("plugins/hgnested/repositories")
public class HgNestedRepositoryResource implements HgNested
{

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   */
  @Inject
  public HgNestedRepositoryResource(RepositoryManager repositoryManager)
  {
    this.repositoryManager = repositoryManager;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param currentRepository
   * @param query
   * @return
   */
  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Response getRepositories(
          @QueryParam("repository") String currentRepository,
          @QueryParam("query") String query)
  {
    List<HgNestedRepository> nestedRepositories =
      new ArrayList<HgNestedRepository>();
    Collection<Repository> repositories = repositoryManager.getAll();
    SearchRequest request = null;

    if (Util.isNotEmpty(query))
    {
      request = new SearchRequest(query, true);
    }

    for (Repository repository : repositories)
    {
      if (TYPE.equals(repository.getType())
          && (Util.isEmpty(currentRepository)
              ||!currentRepository.equals(repository.getName())))
      {
        if (request != null)
        {
          if (SearchUtil.matchesOne(request, repository.getName(),
                                    repository.getDescription(),
                                    repository.getContact()))
          {
            nestedRepositories.add(
                new HgNestedRepository(repository.getName()));
          }
        }
        else
        {
          nestedRepositories.add(new HgNestedRepository(repository.getName()));
        }
      }
    }

    return Response.ok(new HgNestedRepositories(nestedRepositories)).build();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RepositoryManager repositoryManager;
}
