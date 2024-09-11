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

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class HgNestedConfigurationDto extends HalRepresentation {
  @NotNull
  private List<@Valid HgNestedConfigurationEntryDto> subRepositoryEntries;

  public HgNestedConfigurationDto(HgNestedConfiguration configuration, Links links) {
    super(links);
    this.subRepositoryEntries = new ArrayList<>();
    configuration.getNestedRepositoryMap().forEach((path, url) -> {
      HgNestedConfigurationEntryDto entry = new HgNestedConfigurationEntryDto(path, url);
      subRepositoryEntries.add(entry);
    });
  }
}
