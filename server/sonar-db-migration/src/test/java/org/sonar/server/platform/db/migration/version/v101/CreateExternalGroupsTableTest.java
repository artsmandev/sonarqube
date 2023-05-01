/*
 * SonarQube
 * Copyright (C) 2009-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.platform.db.migration.version.v101;

import java.sql.SQLException;
import java.sql.Types;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.db.CoreDbTester;
import org.sonar.server.platform.db.migration.step.DdlChange;

import static org.sonar.server.platform.db.migration.def.VarcharColumnDef.UUID_SIZE;
import static org.sonar.server.platform.db.migration.version.v101.CreateExternalGroupsTable.EXTERNAL_GROUP_ID_COLUMN_NAME;
import static org.sonar.server.platform.db.migration.version.v101.CreateExternalGroupsTable.EXTERNAL_IDENTITY_PROVIDER_COLUMN_NAME;
import static org.sonar.server.platform.db.migration.version.v101.CreateExternalGroupsTable.GROUP_UUID_COLUMN_NAME;
import static org.sonar.server.platform.db.migration.version.v101.CreateExternalGroupsTable.TABLE_NAME;

public class CreateExternalGroupsTableTest {

  @Rule
  public final CoreDbTester db = CoreDbTester.createEmpty();

  private final DdlChange createExternalGroupsTable = new CreateExternalGroupsTable(db.database());

  @Test
  public void migration_should_create_a_table() throws SQLException {
    db.assertTableDoesNotExist(TABLE_NAME);

    createExternalGroupsTable.execute();

    db.assertTableExists(TABLE_NAME);
    db.assertColumnDefinition(TABLE_NAME, GROUP_UUID_COLUMN_NAME, Types.VARCHAR, UUID_SIZE, false);
    db.assertColumnDefinition(TABLE_NAME, EXTERNAL_GROUP_ID_COLUMN_NAME, Types.VARCHAR, 255, false);
    db.assertColumnDefinition(TABLE_NAME, EXTERNAL_IDENTITY_PROVIDER_COLUMN_NAME, Types.VARCHAR, 100, false);
    db.assertPrimaryKey(TABLE_NAME, "pk_external_groups", GROUP_UUID_COLUMN_NAME);
  }

  @Test
  public void migration_should_be_reentrant() throws SQLException {
    db.assertTableDoesNotExist(TABLE_NAME);

    createExternalGroupsTable.execute();
    // re-entrant
    createExternalGroupsTable.execute();

    db.assertTableExists(TABLE_NAME);
  }

}
