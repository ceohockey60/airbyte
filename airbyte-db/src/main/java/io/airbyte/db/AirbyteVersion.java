/*
 * MIT License
 *
 * Copyright (c) 2020 Airbyte
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

package io.airbyte.db;

import static org.jooq.impl.DSL.field;

import java.sql.SQLException;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

/**
 * The AirbyteVersion identifies the version of the database used internally by Airbyte services.
 */
public class AirbyteVersion {

  public static final String AIRBYTE_VERSION_KEY_NAME = "airbyte_version";

  public static String get(final Database database) throws SQLException {
    return database.query(AirbyteVersion::get);
  }

  public static String get(final DSLContext ctx) {
    Result<Record> result = ctx.select().from("airbyte_metadata").where(field("key").eq(AIRBYTE_VERSION_KEY_NAME)).fetch();
    Optional<String> first = result.stream().findFirst().map(r -> r.getValue("value", String.class));

    if (first.isEmpty()) {
      throw new IllegalStateException(String.format("Undefined airbyte_metadata for key = '%s'", AIRBYTE_VERSION_KEY_NAME));
    } else {
      return first.get();
    }
  }

  public static void check(final String airbyteVersion, final Database database) throws SQLException {
    database.query(ctx -> AirbyteVersion.check(airbyteVersion, ctx));
  }

  public static String check(String airbyteVersion, final DSLContext ctx) throws SQLException {
    final String dbVersion = AirbyteVersion.get(ctx);
    if (checkVersion(airbyteVersion, dbVersion)) {
      throw new IllegalStateException(String.format(
          "Version mismatch using %s while Database version is %s.\n" +
              "Please Upgrade or Reset your Airbyte Database, see more at https://docs.airbyte.io/tutorials/tutorials/upgrading-airbyte",
          airbyteVersion.replace("\n", "").strip(), dbVersion.replace("\n", "").strip()));
    }
    return dbVersion;
  }

  public static boolean checkVersion(final String version1, final String version2) {
    final String cleanVersion1 = version1.replace("dev", "").replace("\n", "").strip();
    final String cleanVersion2 = version2.replace("dev", "").replace("\n", "").strip();
    return cleanVersion1.equals(cleanVersion2);
  }

}