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
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

public class HgNestedConfigurationStore {

  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public HgNestedConfigurationStore(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public HgNestedConfiguration loadConfiguration(Repository repository) {
    ConfigurationStore<HgNestedConfiguration> store = createStore(repository);
    return store.getOptional().orElseGet(HgNestedConfiguration::new);
  }

  private ConfigurationStore<HgNestedConfiguration> createStore(Repository repository) {
    return storeFactory.withType(HgNestedConfiguration.class).withName("hgnested").forRepository(repository).build();
  }

  public void storeConfiguration(Repository repository, HgNestedConfiguration configuration) {
    ConfigurationStore<HgNestedConfiguration> store = createStore(repository);
    store.set(configuration);
  }
}
