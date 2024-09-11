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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.util.Providers;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.JsonMockHttpRequest;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(ShiroExtension.class)
class HgNestedRepositoryResourceTest {

  RestDispatcher dispatcher = new RestDispatcher();
  JsonMockHttpResponse response = new JsonMockHttpResponse();
  String domain = "http://localhost:8080";

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private HgNestedConfigurationStore nestedConfigurationStore;
  private ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();

  @BeforeEach
  void initialize() {
    HgNestedRepositoryResource resource = new HgNestedRepositoryResource(
      repositoryManager,
      nestedConfigurationStore,
      Providers.of(scmPathInfoStore));
    dispatcher.addSingletonResource(resource);
    scmPathInfoStore.set(() -> URI.create(domain));
  }

  @Nested
  class WithRepository {
    Repository repository;

    @BeforeEach
    void createRepository() {
      repository = RepositoryTestData.createHeartOfGold("hg");
      when(repositoryManager.get(repository.getNamespaceAndName())).thenReturn(repository);
    }

    private void setupConfiguration(Map<String, String> nestedRepositoryMap) {
      HgNestedConfiguration configuration = new HgNestedConfiguration(nestedRepositoryMap);
      when(nestedConfigurationStore.loadConfiguration(repository)).thenReturn(configuration);
    }

    @Nested
    @SubjectAware(value = "Trillian", permissions = "repository:hgNestedConfiguration:*")
    class WithPermission {
      @Test
      void shouldReturnNestedRepository() throws URISyntaxException {
        setupConfiguration(Map.of("nestedRepoPath", "nestedRepoUrl"));

        MockHttpRequest request = MockHttpRequest.get("/v2/hgnested/hitchhiker/HeartOfGold");
        dispatcher.invoke(request, response);

        JsonNode contentAsJson = response.getContentAsJson();
        assertThat(contentAsJson.get("subRepositoryEntries").get(0).get("url").asText())
          .isEqualTo("nestedRepoUrl");
        assertThat(contentAsJson.get("subRepositoryEntries").get(0).get("path").asText())
          .isEqualTo("nestedRepoPath");
        assertThat(response.getStatus()).isEqualTo(200);
      }

      @Test
      void shouldContainLinks() throws URISyntaxException {
        setupConfiguration(Map.of("nestedRepoPath", "nestedRepoUrl"));

        MockHttpRequest request = MockHttpRequest.get("/v2/hgnested/hitchhiker/HeartOfGold");
        dispatcher.invoke(request, response);

        JsonNode contentAsJson = response.getContentAsJson();
        assertThat(contentAsJson.get("_links").get("self").get("href").asText())
          .isEqualTo(domain + "/v2/hgnested/hitchhiker/HeartOfGold");
        assertThat(contentAsJson.get("_links").get("update").get("href").asText())
          .isEqualTo(domain + "/v2/hgnested/hitchhiker/HeartOfGold");
      }

      @Test
      void shouldReturnEmptyListWhenNothingIsConfigured() throws URISyntaxException {
        setupConfiguration(Collections.emptyMap());

        MockHttpRequest request = MockHttpRequest.get("/v2/hgnested/hitchhiker/HeartOfGold");
        dispatcher.invoke(request, response);

        JsonNode contentAsJson = response.getContentAsJson();
        assertThat(contentAsJson.get("subRepositoryEntries")).isEmpty();
      }

      @Test
      void shouldHandleEmptyConfiguration() throws URISyntaxException {
        JsonMockHttpRequest request = JsonMockHttpRequest
          .put("/v2/hgnested/hitchhiker/HeartOfGold")
          .json("{'subRepositoryEntries':[]}")
          .contentType("application/json");
        dispatcher.invoke(request, response);

        verify(nestedConfigurationStore)
          .storeConfiguration(
            eq(repository),
            argThat((configuration) -> {
                assertThat(configuration.isNestedRepositoryConfigured()).isFalse();
                return true;
              }
            ));
      }

      @Test
      void shouldHandleConfiguration() throws URISyntaxException {
        JsonMockHttpRequest request = JsonMockHttpRequest
          .put("/v2/hgnested/hitchhiker/HeartOfGold")
          .json("{'subRepositoryEntries':[{'path':'nestedRepoPath','url':'nestedRepoUrl'}]}")
          .contentType("application/json");
        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(204);
        verify(nestedConfigurationStore)
          .storeConfiguration(
            eq(repository),
            argThat((configuration) -> {
                assertThat(configuration.getNestedRepositoryUrl("nestedRepoPath"))
                  .isEqualTo("nestedRepoUrl");
                return true;
              }
            ));
      }
    }

    @Nested
    class WithoutPermission {
      @Test
      void shouldOnlyContainSelfLinkWithoutPermission() throws URISyntaxException {
        setupConfiguration(Map.of("nestedRepoPath", "nestedRepoUrl"));

        MockHttpRequest request = MockHttpRequest.get("/v2/hgnested/hitchhiker/HeartOfGold");
        dispatcher.invoke(request, response);

        JsonNode contentAsJson = response.getContentAsJson();
        assertThat(contentAsJson.get("_links").get("self").get("href").asText())
          .isEqualTo(domain + "/v2/hgnested/hitchhiker/HeartOfGold");
        assertThat(contentAsJson.get("_links").asText()).doesNotContain("update");
      }

      @Test
      void shouldHandleForbiddenAction() throws URISyntaxException {
        JsonMockHttpRequest request = JsonMockHttpRequest
          .put("/v2/hgnested/hitchhiker/HeartOfGold")
          .json("{'subRepositoryEntries':[{'path':'nestedRepoPath','url':'nestedRepoUrl'}]}")
          .contentType("application/json");
        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(403);
      }
    }
  }

  @Nested
  class WithoutRepository {
    @Test
    void shouldHandleMissingRepository() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/v2/hgnested/hitchhiker/HeartOfGold");
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void shouldHandleMissingRepositoryForConfiguration() throws URISyntaxException {
      JsonMockHttpRequest request = JsonMockHttpRequest
        .put("/v2/hgnested/hitchhiker/HeartOfGold")
        .json("{'subRepositoryEntries':[]}")
        .contentType("application/json");
      dispatcher.invoke(request, response);

      verify(nestedConfigurationStore, never()).storeConfiguration(any(), any());
      assertThat(response.getStatus()).isEqualTo(404);
    }
  }


  @Test
  void shouldHandleMissingPath() throws URISyntaxException {
    JsonMockHttpRequest request = JsonMockHttpRequest
      .put("/v2/hgnested/hitchhiker/HeartOfGold")
      .json("{'subRepositoryEntries':[{'url':'nestedRepoUrl'}]}")
      .contentType("application/json");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  void shouldHandleEmptyPath() throws URISyntaxException {
    JsonMockHttpRequest request = JsonMockHttpRequest
      .put("/v2/hgnested/hitchhiker/HeartOfGold")
      .json("{'subRepositoryEntries':[{'path': '','url':'nestedRepoUrl'}]}")
      .contentType("application/json");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  void shouldHandleEmptyUrl() throws URISyntaxException {
    JsonMockHttpRequest request = JsonMockHttpRequest
      .put("/v2/hgnested/hitchhiker/HeartOfGold")
      .json("{'subRepositoryEntries':[{'path': 'nestedRepoPath','url':''}]}")
      .contentType("application/json");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  void shouldHandleMissingUrl() throws URISyntaxException {
    JsonMockHttpRequest request = JsonMockHttpRequest
      .put("/v2/hgnested/hitchhiker/HeartOfGold")
      .json("{'subRepositoryEntries':[{'path': 'nestedRepoPath'}]}")
      .contentType("application/json");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  void shouldHandleNullSubRepositoryEntries() throws URISyntaxException {
    JsonMockHttpRequest request = JsonMockHttpRequest
      .put("/v2/hgnested/hitchhiker/HeartOfGold")
      .json("{}")
      .contentType("application/json");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }
}
