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
import com.google.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.FileObjectPreProcessor;
import sonia.scm.repository.FileObjectPreProcessorFactory;
import sonia.scm.repository.Repository;

@Extension
public class HgNestedFileObjectPreProcessorFactory
  implements FileObjectPreProcessorFactory {

  private static final Logger logger =
    LoggerFactory.getLogger(HgNestedFileObjectPreProcessorFactory.class);
  private final Provider<HttpServletRequest> requestProvider;
  private final HgNestedConfigurationStore hgNestedConfigurationStore;


  @Inject
  public HgNestedFileObjectPreProcessorFactory(
    Provider<HttpServletRequest> requestProvider, HgNestedConfigurationStore hgNestedConfigurationStore) {
    this.requestProvider = requestProvider;
    this.hgNestedConfigurationStore = hgNestedConfigurationStore;
  }

  @Override
  public FileObjectPreProcessor createPreProcessor(Repository repository) {
    if (logger.isTraceEnabled()) {
      logger.trace("create file object pre processor for repository {}", repository);
    }

    HgNestedConfiguration config = hgNestedConfigurationStore.loadConfiguration(repository);

    return new HgNestedFileObjectPreProcessor(repository, config, requestProvider.get());
  }
}
