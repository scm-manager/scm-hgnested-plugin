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
