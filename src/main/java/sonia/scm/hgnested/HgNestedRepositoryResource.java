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

import com.google.inject.Inject;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Provider;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import sonia.scm.ContextEntry;
import sonia.scm.NotFoundException;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("v2/hgnested")
public class HgNestedRepositoryResource implements HgNested {

  private final RepositoryManager repositoryManager;
  private final HgNestedConfigurationStore configurationStore;
  private final Provider<ScmPathInfoStore> scmPathInfoStore;


  @Inject
  public HgNestedRepositoryResource(RepositoryManager repositoryManager, HgNestedConfigurationStore configurationStore, Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.repositoryManager = repositoryManager;
    this.configurationStore = configurationStore;
    this.scmPathInfoStore = scmPathInfoStore;
  }


  @GET
  @Path("{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public HgNestedConfigurationDto getConfiguration(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name) {

    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);

    Repository repository = repositoryManager.get(namespaceAndName);

    if (repository == null) {
      throw NotFoundException.notFound(ContextEntry.ContextBuilder.entity(namespaceAndName));
    }

    HgNestedConfiguration configuration = configurationStore.loadConfiguration(repository);

    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), HgNestedRepositoryResource.class);
    String selfLinkUrl = linkBuilder.method("getConfiguration").parameters(repository.getNamespace(), repository.getName()).href();

    Links.Builder builder = new Links.Builder();
    builder.self(selfLinkUrl);

    if (RepositoryPermissions.custom("hgNestedConfiguration", repository).isPermitted()) {
      String updateLinkUrl = linkBuilder.method("updateConfiguration").parameters(repository.getNamespace(), repository.getName()).href();
      builder.single(Link.link("update", updateLinkUrl));
    }

    return new HgNestedConfigurationDto(configuration, builder.build());
  }

  @PUT
  @Path("{namespace}/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  public void updateConfiguration(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid HgNestedConfigurationDto configurationDto) {
    List<HgNestedConfigurationEntryDto> entryDtoList = configurationDto.getSubRepositoryEntries();
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);

    Repository repository = repositoryManager.get(namespaceAndName);

    if (repository == null) {
      throw NotFoundException.notFound(ContextEntry.ContextBuilder.entity(namespaceAndName));
    }

    RepositoryPermissions.custom("hgNestedConfiguration", repository).check();

    Map<String, String> nestedRepositories = new HashMap<>();

    entryDtoList.forEach((entryDto) -> nestedRepositories.put(entryDto.getPath(), entryDto.getUrl()));
    HgNestedConfiguration configuration = new HgNestedConfiguration(nestedRepositories);
    configurationStore.storeConfiguration(repository, configuration);
  }
}
