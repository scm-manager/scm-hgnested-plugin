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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sonia.scm.util.Util;
import sonia.scm.xml.XmlMapStringAdapter;

import java.util.Collections;
import java.util.Map;

@XmlRootElement(name = "hgnested")
@XmlAccessorType(XmlAccessType.FIELD)
public class HgNestedConfiguration {

  @XmlJavaTypeAdapter(XmlMapStringAdapter.class)
  private final Map<String, String> nestedRepositoryMap;

  public HgNestedConfiguration() {
    this(Collections.emptyMap());
  }

  public HgNestedConfiguration(Map<String, String> nestedRepositoryMap) {
    this.nestedRepositoryMap = nestedRepositoryMap;
  }

  public String getNestedRepositoryUrl(String path) {
    return nestedRepositoryMap.get(path);
  }

  public boolean isNestedRepositoryConfigured() {
    return Util.isNotEmpty(nestedRepositoryMap);
  }

  public Map<String, String> getNestedRepositoryMap() {
    return Collections.unmodifiableMap(nestedRepositoryMap);
  }
}
