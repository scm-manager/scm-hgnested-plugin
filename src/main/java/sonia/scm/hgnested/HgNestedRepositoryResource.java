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

import com.google.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sebastian Sdorra
 */
@Path("plugins/hgnested/repositories")
public class HgNestedRepositoryResource implements HgNested {

  /**
   * Constructs ...
   *
   * @param repositoryManager
   */
  @Inject
  public HgNestedRepositoryResource(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @param currentRepository
   * @param query
   * @return
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response getRepositories(
    @QueryParam("repository") String currentRepository,
    @QueryParam("query") String query) {
    List<HgNestedRepository> nestedRepositories =
      new ArrayList<HgNestedRepository>();
    Collection<Repository> repositories = repositoryManager.getAll();
    SearchRequest request = null;

    if (Util.isNotEmpty(query)) {
      request = new SearchRequest(query, true);
    }

    for (Repository repository : repositories) {
      if (TYPE.equals(repository.getType())
        && (Util.isEmpty(currentRepository)
        || !currentRepository.equals(repository.getName()))) {
        if (request != null) {
          if (SearchUtil.matchesOne(request, repository.getName(),
            repository.getDescription(),
            repository.getContact())) {
            nestedRepositories.add(
              new HgNestedRepository(repository.getName()));
          }
        } else {
          nestedRepositories.add(new HgNestedRepository(repository.getName()));
        }
      }
    }

    return Response.ok(new HgNestedRepositories(nestedRepositories)).build();
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * Field description
   */
  private final RepositoryManager repositoryManager;
}
