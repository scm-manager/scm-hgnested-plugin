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
