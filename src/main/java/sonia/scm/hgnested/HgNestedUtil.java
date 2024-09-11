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

import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.repository.Repository;

public class HgNestedUtil {

  private HgNestedUtil() {
  }

  public static String createUrl(HttpServletRequest request,
                                 Repository repository, String nestedRepositoryUrl) {

    if (!nestedRepositoryUrl.startsWith("http")) {
      StringBuilder buffer = new StringBuilder(request.getScheme());

      buffer
        .append("://")
        .append(request.getServerName())
        .append(":")
        .append(request.getServerPort())
        .append(request.getContextPath())
        .append("/repo/");

      if (!nestedRepositoryUrl.contains("/")) {
          buffer.append(repository.getNamespace());
          buffer.append("/");
      }

      buffer.append(nestedRepositoryUrl);

      String query = request.getQueryString();

      if (query != null) {
        buffer.append("?").append(query);
      }

      nestedRepositoryUrl = buffer.toString();
    }

    return nestedRepositoryUrl;
  }
}
