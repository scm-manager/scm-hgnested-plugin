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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationForm, Subtitle, Form, useDocumentTitleForRepository } from "@scm-manager/ui-core";
import { Repository } from "@scm-manager/ui-types";

type Props = {
  link: string;
  repository: Repository;
};

const HgNestedConfigurationForm: FC<Props> = ({ link, repository }) => {
  const [t] = useTranslation("plugins");
  useDocumentTitleForRepository(repository, t("scm-hgnested-plugin.config.title"));

  return (
    <ConfigurationForm link={link} translationPath={["plugins", "scm-hgnested-plugin.config"]}>
      <Subtitle>{t("scm-hgnested-plugin.config.title")}</Subtitle>
      <p>{t("scm-hgnested-plugin.config.description")}</p>
      <Form.ListContext name="subRepositoryEntries">
        <Form.Table withDelete>
          <Form.Table.Column name="path"></Form.Table.Column>
          <Form.Table.Column name="url"></Form.Table.Column>
        </Form.Table>
        <Form.AddListEntryForm defaultValues={{ path: "", url: "" }}>
          <Form.Row>
            <Form.Input name="path" rules={{ required: true }} />
            <Form.Input name="url" rules={{ required: true }} />
          </Form.Row>
        </Form.AddListEntryForm>
      </Form.ListContext>
    </ConfigurationForm>
  );
};

export default HgNestedConfigurationForm;
